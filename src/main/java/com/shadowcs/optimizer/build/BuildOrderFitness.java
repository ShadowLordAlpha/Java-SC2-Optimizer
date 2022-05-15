package com.shadowcs.optimizer.build;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.genetics.Chromosome;
import com.shadowcs.optimizer.genetics.Fitness;
import com.shadowcs.optimizer.pojo.Pair;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Data
@Slf4j
public class BuildOrderFitness implements Fitness<BuildOrderGene> {

    private final BuildState state;
    private final LoadingCache<Integer, UnitS2Data> unitS2DataMap;
    private final LoadingCache<Integer, UnitS2Data> abilityToUnitS2DataMap;
    private final Pair<UnitType, Integer>[] output;

    public BuildOrderFitness(BuildState state, Set<UnitS2Data> units, Pair<UnitType, Integer>[] output) {
        this.state = state;
        this.output = output;

        unitS2DataMap = Caffeine.newBuilder().build(key -> units.stream().filter(w -> w.id() == key).findFirst().orElse(null));
        abilityToUnitS2DataMap = Caffeine.newBuilder().build(key -> units.stream().filter(w -> w.buildAbility() == key).findFirst().orElse(null));
    }

    @Override
    public double calculate(Chromosome<BuildOrderGene> chromo) {

        List<BaseInfo> baseInfos = new ArrayList<>(); // TODO: we always assume a full base for now... will need to fix later
        Cache<Integer, BuildUnitInfo> idToBuildUnit = Caffeine.newBuilder().build();
        // Make a deep copy of the units list... its dumb but probably for the best
        state.unitInfoMap().asMap().forEach((key, value) -> {
            value.forEach(bui -> {
                BuildUnitInfo boi = new BuildUnitInfo(bui.type(), bui.addon()).units(bui.units());
                if(bui.addon() != Units.INVALID) {
                    idToBuildUnit.put(bui.addon().getUnitTypeId(), boi);
                } else {
                    idToBuildUnit.put(bui.type().getUnitTypeId(), boi);
                }
            });
        });

        // This calculation is to get to the final result as fast as possible with no regard for econ. Others can take
        //  econ into account though.
        BuildResources calcResources = new BuildResources(state.resources().minerals(), state.resources().vespene());
        MiningSpeed speed = new MiningSpeed();

        speed.calculateMiningSpeed(idToBuildUnit.asMap().values(), baseInfos);

        // Each loop is one or more steps
        List<BuildOrderGene> buildList = new ArrayList<>(chromo.geneList());
        if(buildList.contains(null)) {
            return -Double.MAX_VALUE;
        }

        int compleatedSteps = 0;
        boolean comp = false;
        int maxStep = 80640;
        int lastStep = 0;
        int currentStep = 0;
        List<Pair<Integer, BuildOrderGene>> nextEventStep = new ArrayList<>();
        while(buildList.size() > 0) {

            // log.info("Time Link 1 Step: {}", currentStep);

            if(currentStep >= maxStep) {
                // log.info("Failed timeout Calculate");
                return -Double.MAX_VALUE;
            }

            // Only do mining if we have actually changed steps... we don't always
            if((currentStep - lastStep) > 0) {
                // log.info("Simulating Mining Delta: {} {}", (currentStep - lastStep), calcResources);
                speed.simulateMining(calcResources, baseInfos, currentStep - lastStep);
                log.trace("Simulating Mining Done: {}", calcResources);
            }

            // log.info("Time Link 2");
            // We do this after mining as we could break the mining equation if we don't because of new units added
            if(nextEventStep.size() > 0 && currentStep >= nextEventStep.get(0).first()) {
                // System.out.println("Event Step " + nextEventStep.get(0).first() + " Current " + currentStep);
                // Get the unit we are creating
                BuildOrderGene bog = nextEventStep.remove(0).second();
                compleatedSteps++;
                UnitS2Data unit = abilityToUnitS2DataMap.get(bog.ability().id());
                // log.info("Time Link 3");
                // Get the unit we are using to create
                // If they have a tech requirement that is an addon that makes it very easy to lookup...
                if(BuildConstants.isAddonFor(Units.from(unit.techRequirement())) != null) {
                    BuildUnitInfo boi = idToBuildUnit.getIfPresent(unit.techRequirement());
                    boi.busy(boi.busy() - 1);
                } else {
                    for(var bui: idToBuildUnit.asMap().values()) {
                        if(unitS2DataMap.get(bui.type().getUnitTypeId()).abilities().contains(bog.ability().id()) && bui.busy() > 0) {
                            bui.busy(bui.busy() - 1);
                            break;
                        }
                    }
                }
                // log.info("Time Link 4");
                // Get the unit info for that item
                BuildUnitInfo boi = idToBuildUnit.get(unit.id(), (key) -> {
                    if(BuildConstants.isAddonFor(Units.from(unit.id())) != null) {
                        UnitType type = BuildConstants.isAddonFor(Units.from(unit.id()));
                        return new BuildUnitInfo(type, Units.from(unit.id()));
                    }

                    return new BuildUnitInfo(Units.from(unit.id()), Units.INVALID);
                });

                if(BuildConstants.isAddonFor(Units.from(unit.id())) == null) {
                    // System.out.println("adding unit " + currentStep + " " + unit.id());
                    boi.units(boi.units() + 1);
                } else {
                    UnitType type = BuildConstants.isAddonFor(Units.from(unit.id()));
                    BuildUnitInfo temp = idToBuildUnit.getIfPresent(type.getUnitTypeId());
                    if(temp != null && temp.units() > 0) {
                        temp.units(temp.units() - 1);
                        boi.units(boi.units() + 1);
                    }
                }
                // log.info("Time Link 5");
                speed.calculateMiningSpeed(idToBuildUnit.asMap().values(), baseInfos);
            }

            // Check if we are done or not yet
            boolean done = true;
            for(var test: output) {
                BuildUnitInfo bui = idToBuildUnit.getIfPresent(test.first().getUnitTypeId());
                if(bui == null || bui.units() < test.second()) {
                    done = false;
                    break;
                }
            }
            // log.info("Time Link 6");
            if(done) {
                comp = true;
                break; // Leave the while loop early
            }

            UnitS2Data unit = abilityToUnitS2DataMap.get(buildList.get(0).ability().id());
            float frames = speed.timeToGetResources(calcResources, unit.cost().minerals(), unit.cost().vespene());
            log.trace("Time Link 7");
            // TODO: we need to check food to make sure we have enough to make the unit or that enough is in progress
            //  to make the unit eventually or we may need to add in the command to make some... for now just assume we
            //  can always make it

            int nextStep = currentStep;
            // We have the ability to make the thing, so let's do it!
            if(frames <= 0) {
                // log.info("Time Link 8");
                // log.info("Event");
                // are we able to start this build or if we need to wait for something to free up
                // Need to check for tech as well
                boolean added = false;
                // TODO: take into account tech aliases as well
                var techtest = idToBuildUnit.getIfPresent(abilityToUnitS2DataMap.get(buildList.get(0).ability().id()).techRequirement());
                if(abilityToUnitS2DataMap.get(buildList.get(0).ability().id()).techRequirement() == 0 || (techtest != null && techtest.units() > 0)) {
                    for (BuildUnitInfo test : idToBuildUnit.asMap().values()) {
                        if (unitS2DataMap.get(test.type().getUnitTypeId()).abilities().contains(buildList.get(0).ability().id()) && test.availableUnits() > 0) {
                            test.busy(test.busy() + 1);

                            // Use resources
                            calcResources.minerals(calcResources.minerals() - unit.cost().minerals());
                            calcResources.vespene(calcResources.vespene() - unit.cost().vespene());
                            // we add a travel time of 44 frames to every action
                            nextEventStep.add(new Pair<>((int) Math.ceil(unit.cost().buildTime() + currentStep + 44.8), buildList.remove(0)));
                            nextEventStep.sort(Comparator.comparingInt(Pair::first));

                            // We are using up something, so calculate the speed
                            speed.calculateMiningSpeed(idToBuildUnit.asMap().values(), baseInfos);

                            added = true;
                            break;
                        }
                    }
                }

                // If not added then the next step should be the next action if there is one
                if(!added) {
                    if(nextEventStep.size() > 0) {
                        nextStep = nextEventStep.get(0).first();
                    } else {
                        // log.info("Failed added Calculate");
                        return -Double.MAX_VALUE;
                    }
                }

                // We can "redo" this step to check on the next order, that way we can do several orders in one step
                //  while still keeping the code simple
            } else if(frames > 0 && nextEventStep.size() > 0) {
                // What comes first, this mining job or
                int check = (int) Math.ceil(currentStep + frames);
                if(check < nextEventStep.get(0).first()) {
                    // The next step will be a mining step
                    // log.info("Mining Step 2");
                    nextStep = (int) Math.ceil(currentStep + frames);
                } else {
                    // The next step will be a event step
                    // log.info("Event Step");
                    nextStep = nextEventStep.get(0).first();
                }
            } else {
                // The next step will be a mining step
                // log.info("Mining Step");
                nextStep = (int) Math.ceil(currentStep + frames);
            }

            lastStep = currentStep;
            currentStep = nextStep;
            // log.info("Time Link 8.1");
        }
        // log.info("Time Link 9");
        double bonusducks = 0;

        if(comp) {
            bonusducks += 5;
        } else {
            bonusducks += 1;
        }

        for(var test: output) {
            BuildUnitInfo bui = idToBuildUnit.getIfPresent(test.first().getUnitTypeId());
            if(bui != null) {
                if(bui.units() <= test.second()) {
                    bonusducks += bui.units();
                }
            }
        }
        // log.info("Time Link 10");
        /*if(currentStep <= 10 || compleatedSteps == 0 || !comp) {
            System.out.println("Probably an error " + new Gson().toJson(chromo));
            //return -Double.MAX_VALUE;
        }*/
        // invert time
        // System.out.println("fitness calc Step: " + currentStep + " Value: " + (1.0 / currentStep));
        String debug = "";
        for(var test: idToBuildUnit.asMap().values()) {
            debug += test.type().getUnitTypeId() + " " + test.units() + ": ";
        }
        chromo.extra("Steps: " + compleatedSteps + " Progress: " + nextEventStep.size() + " current " + currentStep + "debug " + debug);
        chromo.extra("" + compleatedSteps);

        // log.info("Ending Calculate");
        return (maxStep - currentStep) + bonusducks;
    }
}
