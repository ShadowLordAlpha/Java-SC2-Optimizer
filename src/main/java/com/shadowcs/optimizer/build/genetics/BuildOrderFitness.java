package com.shadowcs.optimizer.build.genetics;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.google.common.util.concurrent.AtomicDouble;
import com.shadowcs.optimizer.build.genetics.info.BaseInfo;
import com.shadowcs.optimizer.build.genetics.info.BuildUnitInfo;
import com.shadowcs.optimizer.build.genetics.info.MiningInfo;
import com.shadowcs.optimizer.build.state.BuildState;
import com.shadowcs.optimizer.genetics.Gene;
import com.shadowcs.optimizer.pojo.LoadingHashMap;
import com.shadowcs.optimizer.pojo.Pair;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * In order to vastly simplify the Fitness code we should not be doing really any validation checks that we don't need
 * to do. So we do need to validate the materials, but we don't need to validate technology or supply as we should
 * have both of those always as it is baked into the build order. Same with max supply as that should never go over what
 * we have access to.
 */
@Data
@Slf4j
public class BuildOrderFitness implements Function<Gene[], Double> {

    private static final Gene WAIT = new Gene();
    private static final Gene FOOD = new Gene();
    private static final Gene GAS = new Gene();
    private static final Gene WORKER = new Gene();

    private final BuildState state;
    private final Map<Integer, UnitS2Data> unitS2DataMap;
    private final Pair<UnitType, Integer>[] output;

    public BuildOrderFitness(BuildState state, Map<Integer, UnitS2Data> unitS2DataMap, Pair<UnitType, Integer>[] output) {
        this.state = state;
        this.output = output;

        this.unitS2DataMap = unitS2DataMap;
    }

    @Override
    public Double apply(Gene[] genes) {

        List<BaseInfo> baseInfos = new ArrayList<>(); // TODO: we always assume a full base for now... will need to fix later
        for (int i = 0; i < 8; i++) {
            // By default, we assume that we are able to take 8 full normal bases, though let's be honest here if we can we are taking all of them
            baseInfos.add(new BaseInfo());
        }

        LoadingHashMap<Integer, BuildUnitInfo> idToBuildUnit = new LoadingHashMap<>((key) -> new BuildUnitInfo(Units.from(key)));

        double food = 0;
        double maxFood = 0;

        // Make a deep copy of the units list... its dumb but probably for the best, couldn't we make an init type part that does this once for everything?
        for (var key : state.unitInfoMap().keySet()) {

            var value = state.unitInfoMap().get(key);

            BuildUnitInfo boi = new BuildUnitInfo(value.type()).units(value.units()).addonReactor(value.addonReactor()).addonTechlab(value.addonTechlab());

            var unit = unitS2DataMap.get(value.type().getUnitTypeId());

            food += unit.food() * value.units();
            if (unit.food() > 0) {
                maxFood += unit.food() * value.units();
            }

            idToBuildUnit.put(value.type().getUnitTypeId(), boi);
        }

        // This calculation is to get to the final result as fast as possible with no regard for econ. Others can take
        //  econ into account though.
        MiningInfo speed = new MiningInfo().minerals(state.resources().minerals()).vespene(state.resources().vespene());
        speed.calculateMiningSpeed(idToBuildUnit, baseInfos);

        int maxFrame = 80640; // This is one hour in frames, games are a tie after one hour so we should never go over that
        int delta = 0; // The change in frames
        int currentFrame = 0; // The frame we are currently doing things on

        int currentGene = 0;
        boolean run = true;

        // A gene simply representing that we need to wait
        Gene wait = new Gene();

        // NOTE: must remove when the value gets to zero
        HashMap<Gene, Integer> finishedUnits = new HashMap<>();

        Set<Gene> pendingGenes = new HashSet<>();
        List<Pair<Integer, Gene>> pendingUnits = new ArrayList<>();

        while (run) {

            // If we have moved forward any frames we need to get the mining and other time parts that are needed
            if (delta > 0) {
                // Simulate mining resources
                speed.simulateMining(baseInfos, delta);

                // TODO: Simulate Energy

                // TODO: Simulate Larva

            }

            // Check the gene
            for (; currentGene < genes.length; currentGene++) {
                var gene = genes[currentGene];

                if(!checkUnit(gene, food, maxFood, speed, finishedUnits, pendingGenes, pendingUnits)) {
                    break;
                }
            }

            if(pendingUnits.isEmpty()) {
                break;
            }

            // Check if we are over our frame time
            if (currentFrame >= maxFrame) {
                break;
            }



            // TODO: sort genes
            int nextFrame = pendingUnits.get(0).first();
            delta = currentFrame - nextFrame;
            currentFrame = nextFrame;
        }

        double timeScore = maxFrame - currentFrame;

        double unitScore = 0;


        return timeScore + unitScore;
    }


    /**
     * Check if a unit can be created, This will return true IFF the unit can be created right then
     *
     * @param gene
     * @param food
     * @param maxFood
     * @param speed
     * @param finishedUnits
     * @param pendingGenes
     * @param pendingUnits
     * @return
     */
    private boolean checkUnit(Gene gene, double food, double maxFood, MiningInfo speed, HashMap<Gene, Integer> finishedUnits, Set<Gene> pendingGenes, List<Pair<Integer, Gene>> pendingUnits) {

        BuildOrderGene bog = gene.data();

        // Check that we have the food needed to make the unit
        double neededFood = bog.unitCreated().food();
        if (neededFood < 0 && (food + neededFood) < 0) {
            for (var pending : pendingUnits) {
                var pFood = ((BuildOrderGene) pending.second().data()).unitCreated().food();
                if (pFood > 0 && (pFood + food) > neededFood) {
                    // We still can't start ourselves but we can wait for the food needed to be done
                    return false;
                }
            }

            // Lets start up a food unit. We still will return false though as we are not trying to start the next unit because we need this one done first
            checkUnit(FOOD, food, maxFood, speed, finishedUnits, pendingGenes, pendingUnits);
            return false;
        } else if (neededFood > 0 && (maxFood + neededFood) > BuildConstants.maxFood) {
            // We can't continue as we cannot make more food which is needed for this unit, the build will finish its queue in the hopes that that does it
            return false;
        }

        // Now we check that we have all the requirements in order to build
        var needed = gene.getNeeded(finishedUnits.keySet());
        if (needed != null) {
            // Remove all the genes we already have pending from the needed list, we don't need another one started
            needed.removeAll(pendingGenes);
            if (!needed.isEmpty()) {
                // For everything in the list if we are able to start it up we should
                for (var dep : needed) {
                    checkUnit(dep, food, maxFood, speed, finishedUnits, pendingGenes, pendingUnits);
                }
                return false;
            }
        }

        // Now we check the cost of the unit
        var timeCost = speed.timeToGetResources(bog.unitCreated().cost().minerals(), bog.unitCreated().cost().vespene());
        int frameTime = (int) Math.ceil(timeCost);
        if (timeCost == Float.POSITIVE_INFINITY) {

            // We are missing gas production
            if (bog.unitCreated().cost().vespene() > 0 && speed.vespenePerFrame() == 0) {

                // Check if we even have gas for them to be mining
                if (speed.vespeneList().size() == 0) {
                    checkUnit(GAS, food, maxFood, speed, finishedUnits, pendingGenes, pendingUnits);
                    return false;
                } else {
                    // they have gas but not workers to do the work
                    checkUnit(WORKER, food, maxFood, speed, finishedUnits, pendingGenes, pendingUnits);
                    return false;
                }
            }

            if (bog.unitCreated().cost().minerals() > 0 && speed.mineralsPerFrame() == 0 && speed.harvistList().size() + speed.muleList().size() > 0) {
                // we will only auto create workers. MULE is left up to the code
                checkUnit(WORKER, food, maxFood, speed, finishedUnits, pendingGenes, pendingUnits);
                return false;
            }

        } else if (timeCost > 0.0) {
            // Add the needed wait time
            pendingUnits.add(new Pair<>((int) Math.ceil(frameTime + timeCost), WAIT));
            return false;
        }

        // We have everything we need apparently, so we need the unit in the queue now
        int cancel = 0; // TODO: if we are a cancel type build then we need to do this as well

        pendingUnits.add(new Pair<>((int) Math.ceil(frameTime + bog.unitCreated().cost().buildTime() - cancel), gene));
        pendingGenes.add(gene);

        // Remove the ones we need to remove from the list
        if (bog.ability().name().contains("Morph") || bog.ability().name().contains("Burrow") || bog.ability().name().contains("Lift") || bog.ability().name().contains("Land")) {
            // The command is a morph command so we need to kill off the unit that is creating the unit as it is getting replaced
            int count = finishedUnits.get(bog.caster());
            if (count > 1) {
                finishedUnits.put(bog.caster(), count - 1);
            } else {
                finishedUnits.remove(bog.caster());
            }

            // TODO: if lift/land then we need to check for addons as well

            // TODO: we need to care about food changes as well
        }
        return true;
    }
}
