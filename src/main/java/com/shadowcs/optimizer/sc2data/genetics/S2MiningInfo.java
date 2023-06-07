package com.shadowcs.optimizer.sc2data.genetics;

import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.sc2data.models.TechTree;

import java.util.List;
import java.util.Map;

// TODO: this probably doesn't need to be its own class anymore and could be merged into gamestate
public class S2MiningInfo {

    private float mineralsPerFrame;
    private float vespenePerFrame;

    /**
     * Simulate mining with the given speed for the given amount of time and store the results. We use per frame as
     * while it is technically less accurate than per second it is good enough while reducing the math we need to do
     */
    public float[] simulateMining(List<S2BaseInfo> baseInfos, float dt) {

        float totalWeight = 0;
        for (S2BaseInfo base : baseInfos) {
            S2BaseInfo.IntPair slots = base.mineralSlots();
            totalWeight += slots.first() * 1.5f + slots.second();
        }

        float normalizationFactor = 1.0f / (totalWeight + 0.0001f);
        float deltaMineralsPerWeight = mineralsPerFrame * dt * normalizationFactor;
        for (S2BaseInfo base : baseInfos) {
            S2BaseInfo.IntPair slots = base.mineralSlots();
            float weight = slots.first() * 1.5f + slots.second();
            base.mineMinerals(deltaMineralsPerWeight * weight);
        }

        // TODO: gas reduction stuff

        // Return the resources mined during the simulated time
        return new float[]{mineralsPerFrame * dt, vespenePerFrame * dt};
    }

    /**
     * How long in frames will it take us to get the given amount of minerals.
     *
     * @param mineralCost How many minerals are we trying to get
     * @return The time in frames it will take us to get the needed gas
     */
    public double timeToGetMinerals(double mineralCost) {
        double time = 0;

        if (mineralCost > 0) {
            if (mineralsPerFrame <= 0)
                return Float.POSITIVE_INFINITY;
            time = mineralCost / mineralsPerFrame;
        }

        return time;
    }

    /**
     * How long in frames will it take us to get the given amount of gas.
     *
     * @param gasCost How much gas are we trying to get
     * @return The time in frames it will take us to get the needed gas
     */
    public double timeToGetGas(double gasCost) {
        double time = 0;

        if (gasCost > 0) {
            if (vespenePerFrame <= 0)
                return Float.POSITIVE_INFINITY;
            time = Math.max(time, gasCost / vespenePerFrame);
        }

        return time;
    }

    public void calculateMiningSpeed(Map<Integer, Integer> units, TechTree techTree, List<S2BaseInfo> baseInfos) {

        int harvesters = 0;
        int mules = 0;
        int bases = 0;
        int geysers = 0;

        for(var entry: units.entrySet()) {
            var unit = techTree.unitMap().get(entry.getKey());
            if(unit.worker()) {
                harvesters += entry.getValue();
            }

            if(unit.townhall()) {
                bases += entry.getValue();
            }

            if(unit.id() == Units.TERRAN_MULE.getUnitTypeId()) {
                mules += entry.getValue();
            }

            if(unit.needsGeyser()) {
                geysers += entry.getValue();
            }
        }

        int highYieldMineralHarvestingSlots = 0;
        int lowYieldMineralHarvestingSlots = 0;
        for (int i = 0; i < Math.min(bases, baseInfos.size()); i++) {
            var b = baseInfos.get(i);
            var t = b.mineralSlots();
            highYieldMineralHarvestingSlots += t.first();
            lowYieldMineralHarvestingSlots += t.second();

            if(b.remainingVespene1() <= 0) {
                geysers--;
            }
            if(b.remainingVespene2() <= 0 && !b.richVespene()) {
                geysers--;
            }
        }

        //System.out.printf("%d %d %d\n", harvesters, geysers * 3, Math.min((int) (harvesters * BuildConstants.half), geysers * 3));
        int vespeneMining = Math.min((int) (harvesters * S2GameConstants.half), geysers * 3);
        int mineralMining = harvesters - vespeneMining;

        // Maximum effective harvesters (todo: account for more things)
        // First 2 harvesters per mineral field yield more minerals than the 3rd one.
        int highYieldHarvesters = Math.min(highYieldMineralHarvestingSlots, mineralMining);
        int lowYieldHarvesters = Math.min(lowYieldMineralHarvestingSlots, mineralMining - highYieldHarvesters);
        int muleYieldHarvesters = Math.min(lowYieldMineralHarvestingSlots, mules);

        // cout << mineralMining << " " << highYieldHarvesters << " " << lowYieldHarvesters << " " << foodAvailable() << endl;
        mineralsPerFrame = lowYieldHarvesters * S2GameConstants.lowYieldMineralsPerFrame + highYieldHarvesters * S2GameConstants.highYieldMineralsPerFrame + muleYieldHarvesters * S2GameConstants.muleMineralsPerFrame;
        vespenePerFrame = vespeneMining * S2GameConstants.vespenePerFrame;
    }
}
