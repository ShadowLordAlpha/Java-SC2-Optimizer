package com.shadowcs.optimizer.build;

import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.pojo.Pair;
import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
public class MiningSpeed {

    // TODO: Check units here!
    private static float FasterSpeedMultiplier = 1.4f;
    private static float LowYieldMineralsPerMinute = 22 * FasterSpeedMultiplier;
    private static float HighYieldMineralsPerMinute = 40 * FasterSpeedMultiplier;
    private static float VespenePerMinute = 38 * FasterSpeedMultiplier;
    private static float MinutesPerSecond = 1 / 60.0f;

    private float mineralsPerSecond;
    private float vespenePerSecond;

    /** Simulate mining with the given speed for the given amount of time */
    public void simulateMining (BuildResources resources, List<BaseInfo> baseInfos, float dt) {

        // Convert frames into seconds for math reasons...
        dt = dt / BuildConstants.secondsToFrame;

        float totalWeight = 0;
        for (BaseInfo base : baseInfos) {
            Pair<Integer, Integer> slots = base.mineralSlots();
            float weight = slots.first() * 1.5f + slots.second();
            totalWeight += weight;
        }

        float normalizationFactor = 1.0f / (totalWeight + 0.0001f);
        float deltaMineralsPerWeight = mineralsPerSecond * dt * normalizationFactor;
        for (BaseInfo base : baseInfos) {
            Pair<Integer, Integer>  slots = base.mineralSlots();
            float weight = slots.first() * 1.5f + slots.second();
            base.mineMinerals(deltaMineralsPerWeight * weight);
        }

        // Add the mined resources to our resource set
        resources.minerals(resources.minerals() + mineralsPerSecond * dt);
        resources.vespene(resources.vespene() + vespenePerSecond * dt);
    }

    /**
     * Time is in seconds for whatever reason... frames may have been better but whatever...
     *
     * @param resources
     * @param mineralCost
     * @param vespeneCost
     * @return
     */
    public float timeToGetResources(BuildResources resources, float mineralCost, float vespeneCost) {

        // How much more do we need
        mineralCost -= resources.minerals();
        vespeneCost -= resources.vespene();

        float time = 0;
        if (mineralCost > 0) {
            if (mineralsPerSecond() == 0)
                return Float.POSITIVE_INFINITY;
            time = mineralCost / mineralsPerSecond();
        }
        if (vespeneCost > 0) {
            if (vespenePerSecond() == 0)
                return Float.POSITIVE_INFINITY;
            time = Math.max(time, vespeneCost / vespenePerSecond());
        }

        return time * BuildConstants.secondsToFrame;
    }

    public void calculateMiningSpeed(Collection<BuildUnitInfo> units, List<BaseInfo> baseInfos) {

        int harvesters = 0;
        int mules = 0;
        int bases = 0;
        int geysers = 0;

        for (var unit : units) {
            // TODO: Normalize type?
            if (BuildConstants.isBasicHarvester(unit.type())) {
                harvesters += unit.availableUnits();
            }

            if (unit.type() == Units.TERRAN_MULE) {
                mules += unit.availableUnits();
            }

            if (BuildConstants.isTownHall(unit.type())) {
                bases += unit.availableUnits();
            }

            if (BuildConstants.isVespeneHarvester(unit.type())) {
                geysers += unit.availableUnits();
            }
        }

        int highYieldMineralHarvestingSlots = 0;
        int lowYieldMineralHarvestingSlots = 0;
        for (int i = 0; i < bases; i++) {
            if (i < baseInfos.size()) {
                var t = baseInfos.get(i).mineralSlots();
                highYieldMineralHarvestingSlots += t.first();
                lowYieldMineralHarvestingSlots += t.second();
            } else {
                // Assume lots of minerals
                highYieldMineralHarvestingSlots += 16;
                lowYieldMineralHarvestingSlots += 8;
            }
        }

        int vespeneMining = Math.min(harvesters / 2, geysers * 3);
        int mineralMining = harvesters - vespeneMining;

        // Maximum effective harvesters (todo: account for more things)
        // First 2 harvesters per mineral field yield more minerals than the 3rd one.
        int highYieldHarvesters = Math.min(highYieldMineralHarvestingSlots, mineralMining);
        int lowYieldHarvesters = Math.min(lowYieldMineralHarvestingSlots, mineralMining - highYieldHarvesters);

        // cout << mineralMining << " " << highYieldHarvesters << " " << lowYieldHarvesters << " " << foodAvailable() << endl;
        mineralsPerSecond = (lowYieldHarvesters * LowYieldMineralsPerMinute + highYieldHarvesters * HighYieldMineralsPerMinute) * MinutesPerSecond;
        vespenePerSecond = vespeneMining * VespenePerMinute * MinutesPerSecond;
    }
}
