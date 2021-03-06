package com.shadowcs.optimizer.build.genetics;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.Gson;
import com.shadowcs.optimizer.build.genetics.info.BaseInfo;
import com.shadowcs.optimizer.build.genetics.info.BuildUnitInfo;
import com.shadowcs.optimizer.build.genetics.info.MiningInfo;
import com.shadowcs.optimizer.build.state.BuildState;
import com.shadowcs.optimizer.genetics.Chromosome;
import com.shadowcs.optimizer.genetics.Fitness;
import com.shadowcs.optimizer.pojo.LoadingHashMap;
import com.shadowcs.optimizer.pojo.Pair;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j
public class BuildOrderFitness implements Fitness<BuildOrderGene> {

    private final BuildState state;
    private final Map<Integer, UnitS2Data> unitS2DataMap;
    private final Map<Integer, UnitS2Data> abilityToUnitS2DataMap;
    private final Pair<UnitType, Integer>[] output;

    public BuildOrderFitness(BuildState state, Map<Integer, UnitS2Data> unitS2DataMap, Map<Integer, UnitS2Data> abilityToUnitS2DataMap, Pair<UnitType, Integer>[] output) {
        this.state = state;
        this.output = output;

        this.unitS2DataMap = unitS2DataMap;
        this.abilityToUnitS2DataMap = abilityToUnitS2DataMap;
    }

    @Override
    public double calculate(Chromosome<BuildOrderGene> chromo) {

        List<BaseInfo> baseInfos = new ArrayList<>(); // TODO: we always assume a full base for now... will need to fix later
        for(int i = 0; i < 8; i++) {
            // By default we assume that we are able to take 8 full normal bases
            baseInfos.add(new BaseInfo());
        }

        LoadingHashMap<Integer, BuildUnitInfo> idToBuildUnit = new LoadingHashMap<>((key) -> {
            if(BuildConstants.isAddonFor(Units.from(key)) != null) {
                UnitType type = BuildConstants.isAddonFor(Units.from(key));
                return new BuildUnitInfo(type, Units.from(key));
            }
            return new BuildUnitInfo(Units.from(key), Units.INVALID);
        });

        Map<Integer, AtomicInteger> techCache = new LoadingHashMap<>((key) -> new AtomicInteger(0));

        AtomicDouble food = new AtomicDouble();
        AtomicDouble foodUsed = new AtomicDouble();

        // Make a deep copy of the units list... its dumb but probably for the best
        state.unitInfoMap().forEach((key, value) -> {
            value.forEach(bui -> {
                BuildUnitInfo boi = new BuildUnitInfo(bui.type(), bui.addon()).units(bui.units());

                var unit = unitS2DataMap.get(bui.type().getUnitTypeId());

                food.addAndGet(unit.food() * bui.units());
                if(unit.food() < 0) {
                    foodUsed.addAndGet(Math.abs(unit.food()) * bui.units());
                }

                techCache.get(unit.id()).incrementAndGet();
                unit.techAliases().forEach(tech -> techCache.get(tech).incrementAndGet());

                if(bui.addon() != Units.INVALID) {
                    idToBuildUnit.put(bui.addon().getUnitTypeId(), boi);
                } else {
                    idToBuildUnit.put(bui.type().getUnitTypeId(), boi);
                }
            });
        });

        // This calculation is to get to the final result as fast as possible with no regard for econ. Others can take
        //  econ into account though.
        MiningInfo speed = new MiningInfo().minerals(state.resources().minerals()).vespene(state.resources().vespene());
        speed.calculateMiningSpeed(idToBuildUnit, baseInfos);

        // Each loop is one or more steps, we use remove so this lets us remove without affecting the chromosome
        List<BuildOrderGene> buildList = new ArrayList<>(chromo.geneList());
        if(buildList.contains(null)) {
            return -Double.MAX_VALUE;
        }

        int compleatedSteps = 0;
        boolean comp = false;
        int maxFrame = 80640; // This is one hour in frames, games are a tie after one hour so we should never go over that
        int lastFrame = 0;
        int currentFrame = 0;
        List<Pair<Integer, BuildOrderGene>> nextEventStep = new ArrayList<>();

        // Make sure we still have orders to complete and we have not gone over time
        while(buildList.size() > 0 && currentFrame < maxFrame) {

            var deltaFrame = currentFrame - lastFrame;

            // Only do mining if we have actually changed steps... we don't always change steps when we do another loop
            if(deltaFrame > 0) {
                // Simulate mining resources
                speed.simulateMining(baseInfos, deltaFrame);

                // TODO: Simulate Energy

                // TODO: Simulate Larva
            }

            // We do this after mining as we could break the mining equation if we don't because of new units added
            if(nextEventStep.size() > 0 && currentFrame >= nextEventStep.get(0).first()) {

                // Get the unit we are creating
                BuildOrderGene bog = nextEventStep.remove(0).second();
                compleatedSteps++;

                // Check for the unit and add it to our lists
                UnitS2Data unit = abilityToUnitS2DataMap.get(bog.ability().id());
                if(unit != null) {

                    // Properly add food data to our list
                    food.addAndGet(unit.food());
                    if(unit.food() < 0) {
                        foodUsed.addAndGet(Math.abs(unit.food()));
                    }

                    // Get the unit info for the unit
                    BuildUnitInfo boi = idToBuildUnit.get(unit.id());

                    // Add unit to the tech list
                    techCache.get(unit.id()).incrementAndGet();
                    unit.techAliases().forEach(key -> techCache.get(key).incrementAndGet());

                    if(BuildConstants.isAddonFor(Units.from(unit.id())) == null) {
                        // The unit is not an addon so we can just directly add it
                        boi.units(boi.units() + 1);
                    } else {
                        // We are an addon so we get to do some fun stuff...
                        UnitType type = BuildConstants.isAddonFor(Units.from(unit.id()));
                        BuildUnitInfo temp = idToBuildUnit.get(type.getUnitTypeId());
                        if(temp != null && temp.units() > 0) {
                            temp.units(temp.units() - 1);
                            boi.units(boi.units() + 1);
                        }
                    }
                }

                // We are done with the caster, so we can now mark them as not busy
                UnitS2Data caster = bog.caster();
                if(BuildConstants.isAddonFor(Units.from(unit.techRequirement())) != null) {
                    BuildUnitInfo boi = idToBuildUnit.get(unit.techRequirement());
                    boi.busy(boi.busy() - 1);
                } else if(caster != null && caster.id() != Units.ZERG_DRONE.getUnitTypeId() && caster.id() != Units.PROTOSS_PROBE.getUnitTypeId()) {
                    BuildUnitInfo boi = idToBuildUnit.get(caster.id());
                    boi.busy(boi.busy() - 1);
                }

                // Update our speed calculations for mining
                // TODO: we may want to skip over this for more things
                speed.calculateMiningSpeed(idToBuildUnit, baseInfos);
                //System.out.println(caster.name());

                // Check if we are done and have all needed units and other data (we only need to do this after we make a unit or other object)
                boolean done = true;
                for(var test: output) {
                    BuildUnitInfo bui = idToBuildUnit.get(test.first().getUnitTypeId());
                    if(bui == null || bui.units() < test.second()) {
                        done = false;
                        break;
                    }
                }

                if(done) {
                    comp = true;
                    break; // Leave the while loop early as we are done
                }
            }

            // Get the next unit to build in the order
            UnitS2Data unit = abilityToUnitS2DataMap.get(buildList.get(0).ability().id());
            var caster = buildList.get(0).caster();
            int casterId = caster.id();

            if(unit.requireAttached()) {
                casterId = unit.techRequirement();
            } else if(casterId == Units.TERRAN_STARPORT.getUnitTypeId() && idToBuildUnit.get(Units.TERRAN_STARPORT_REACTOR.getUnitTypeId()).availableUnits() > 0) {
                casterId = Units.TERRAN_STARPORT_REACTOR.getUnitTypeId();
            } else if(casterId == Units.TERRAN_BARRACKS.getUnitTypeId() && idToBuildUnit.get(Units.TERRAN_BARRACKS_REACTOR.getUnitTypeId()).availableUnits() > 0) {
                casterId = Units.TERRAN_BARRACKS_REACTOR.getUnitTypeId();
            } else if(casterId == Units.TERRAN_FACTORY.getUnitTypeId() && idToBuildUnit.get(Units.TERRAN_FACTORY_REACTOR.getUnitTypeId()).availableUnits() > 0) {
                casterId = Units.TERRAN_FACTORY_REACTOR.getUnitTypeId();
            } else if(idToBuildUnit.get(casterId).availableUnits() <= 0) {
                if (casterId == Units.TERRAN_STARPORT.getUnitTypeId() && idToBuildUnit.get(Units.TERRAN_STARPORT_TECHLAB.getUnitTypeId()).availableUnits() > 0) {
                    casterId = Units.TERRAN_STARPORT_TECHLAB.getUnitTypeId();
                } else if (casterId == Units.TERRAN_BARRACKS.getUnitTypeId() && idToBuildUnit.get(Units.TERRAN_BARRACKS_TECHLAB.getUnitTypeId()).availableUnits() > 0) {
                    casterId = Units.TERRAN_BARRACKS_TECHLAB.getUnitTypeId();
                } else if (casterId == Units.TERRAN_FACTORY.getUnitTypeId() && idToBuildUnit.get(Units.TERRAN_FACTORY_TECHLAB.getUnitTypeId()).availableUnits() > 0) {
                    casterId = Units.TERRAN_FACTORY_TECHLAB.getUnitTypeId();
                }
            }

            var builder = idToBuildUnit.get(casterId);

            // How many frames until we have the resource to build it?
            float frames = (float) Math.ceil(speed.timeToGetResources(unit.cost().minerals(), unit.cost().vespene()));

            // What frame are we going to next? can be the current frame if we just want to do more on this frame
            int nextStep = currentFrame;

            // We have the resources and command to do something on this frame so let's try and do it
            // TODO: change to a build constant for max food
            if(frames <= 0 && foodUsed.get() <= 200 && (unit.food() >= 0 || food.get() >= Math.abs(unit.food())) && (unit.techRequirement() == 0 || techCache.get(unit.techRequirement()).get() > 0) && builder.availableUnits() > 0) {

                // make another build busy...
                // TODO: probes don't get busy... and drones just die
                builder.busy(builder.busy() + 1);
                //System.out.println(builder.type());

                // Use resources
                speed.minerals(speed.minerals() - unit.cost().minerals());
                speed.vespene(speed.vespene() - unit.cost().vespene());

                // we add a travel time of 44.8 frames to every action (2 seconds)
                nextEventStep.add(new Pair<>((int) Math.ceil(unit.cost().buildTime() + currentFrame + 44.8), buildList.remove(0)));
                nextEventStep.sort(Comparator.comparingInt(Pair::first));

                // We are using up something, so calculate the speed
                speed.calculateMiningSpeed(idToBuildUnit, baseInfos);

                // The next step should probably be when the next event is done
                // nextStep = (int) Math.ceil(nextEventStep.get(0).first());

                // We can "redo" this step to check on the next order, that way we can do several orders in one step
                //  while still keeping the code simple
            } else if(frames > 0 && nextEventStep.size() > 0) {
                // What comes first, this mining job or
                int check = (int) Math.ceil((float) currentFrame + frames);
                int nextEvent = (int) Math.ceil(nextEventStep.get(0).first());

                // log.info("Comp {} {} {}", check, currentFrame, frames);

                if(check < nextEvent) {
                    // The next step will be a mining step
                    nextStep = check;
                } else {
                    // The next step will be an event step
                    nextStep = nextEvent;
                }
            } else if(nextEventStep.size() > 0) {
                // The next step will be an event step
                nextStep = (int) Math.ceil(nextEventStep.get(0).first());
            } else if(frames > 0) {
                // The next step will be a mining step
                nextStep = (int) Math.ceil((float) currentFrame + frames);
            } else {
                // We have nothing we can do... and as we are not yet done we can just return a massive negative number
                log.info("Impossible... something is wrong with the order");
                log.info("Impossible... {} {} {} {} {}", frames <= 0 , foodUsed.get() <= 200 , (unit.food() >= 0 || food.get() >= Math.abs(unit.food())) , (unit.techRequirement() == 0 || techCache.get(unit.techRequirement()).get() > 0) , builder.availableUnits() > 0);
                log.info("Impossible... {} {} {} {}", compleatedSteps, foodUsed.get(), unit.food(), food.get());
                log.info("Impossible... {}", new Gson().toJson(chromo.geneList()));
                return -Double.MAX_VALUE;
            }
            lastFrame = currentFrame;
            currentFrame = nextStep;
        }

        // Return the lowest value for things that don't finish, we will always prioritize runs that finish


        // log.info("Time Link 9");
        double bonusducks = 0;

        if(currentFrame > maxFrame) {
            // Big demarit for hitting the end of time
            bonusducks -= 100000;
        }

        if(comp) {
            // Big boost for finishing
            bonusducks += 10000;
        } else {
            // small amount of bad for not doing everything
            bonusducks -= 100;
        }

        for(var test: output) {
            BuildUnitInfo bui = idToBuildUnit.get(test.first().getUnitTypeId());
            if(bui != null) {
                if(bui.units() < test.second()) {
                    bonusducks += bui.units();
                } else {
                    // We got them all so now we boost all the ones we wanted and got
                    bonusducks += test.second() * 10;
                }
            }
        }

        chromo.extra("" + compleatedSteps);

        return (maxFrame - currentFrame) + bonusducks;
    }
}
