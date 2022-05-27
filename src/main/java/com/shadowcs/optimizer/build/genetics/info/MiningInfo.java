package com.shadowcs.optimizer.build.genetics.info;

import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.build.genetics.BuildConstants;
import com.shadowcs.optimizer.pojo.LoadingHashMap;
import com.shadowcs.optimizer.pojo.Pair;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
public class MiningInfo {

    private float mineralsPerFrame;
    private float vespenePerFrame;

    private float minerals;
    private float vespene;

    private boolean unitDetection;
    private List<BuildUnitInfo> harvistList = new ArrayList<>();
    private List<BuildUnitInfo> townList = new ArrayList<>();
    private List<BuildUnitInfo> vespeneList = new ArrayList<>();
    private List<BuildUnitInfo> muleList = new ArrayList<>();

    /**
     * Simulate mining with the given speed for the given amount of time and store the results. We use per frame as
     * while it is technically less accurate than per second it is good enough while reducing the math we need to do
     */
    public void simulateMining (List<BaseInfo> baseInfos, float dt) {

        float totalWeight = 0;
        for (BaseInfo base : baseInfos) {
            Pair<Integer, Integer> slots = base.mineralSlots();
            float weight = slots.first() * 1.5f + slots.second();
            totalWeight += weight;
        }

        float normalizationFactor = 1.0f / (totalWeight + 0.0001f);
        float deltaMineralsPerWeight = mineralsPerFrame * dt * normalizationFactor;
        for (BaseInfo base : baseInfos) {
            Pair<Integer, Integer>  slots = base.mineralSlots();
            float weight = slots.first() * 1.5f + slots.second();
            base.mineMinerals(deltaMineralsPerWeight * weight);
        }

        // Add the mined resources to our resource set
        minerals += mineralsPerFrame * dt;
        vespene += vespenePerFrame * dt;
    }

    /**
     * Time is in seconds for whatever reason... frames may have been better but whatever...
     *
     * TODO: see if getting rid of the division here actually helps any, it may or it may not
     *
     * @param mineralCost
     * @param vespeneCost
     *
     * @return
     */
    public float timeToGetResources(float mineralCost, float vespeneCost) {

        // How much more do we need
        mineralCost -= minerals;
        vespeneCost -= vespene;

        float time = 0;
        if (mineralCost > 0) {
            if (mineralsPerFrame <= 0)
                return Float.POSITIVE_INFINITY;
            time = mineralCost / mineralsPerFrame;
        }
        if (vespeneCost > 0) {
            if (vespenePerFrame <= 0)
                return Float.POSITIVE_INFINITY;
            time = Math.max(time, vespeneCost / vespenePerFrame);
        }

        return time;
    }

    public void calculateMiningSpeed(LoadingHashMap<Integer, BuildUnitInfo> units, List<BaseInfo> baseInfos) {

        if(!unitDetection) {
            unitDetection = true;
            for (int i = 0; i < BuildConstants.townHall.size(); i++) {
                townList.add(units.get(BuildConstants.townHall.get(i)));
            }

            muleList.add(units.get(Units.TERRAN_MULE.getUnitTypeId()));

            for (int i = 0; i < BuildConstants.basicHarvester.size(); i++) {
                harvistList.add(units.get(BuildConstants.basicHarvester.get(i)));
            }

            for (int i = 0; i < BuildConstants.vespeneHarvester.size(); i++) {
                vespeneList.add(units.get(BuildConstants.vespeneHarvester.get(i)));
            }
        }

        int harvesters = 0;
        int mules = 0;
        int bases = 0;
        int geysers = 0;

        for(int i = 0; i < harvistList.size(); i++) {
            harvesters += harvistList.get(i).availableUnits();
        }

        for(int i = 0; i < townList.size(); i++) {
            bases += townList.get(i).units();
        }

        for(int i = 0; i < muleList.size(); i++) {
            mules += muleList.get(i).availableUnits();
        }

        for(int i = 0; i < vespeneList.size(); i++) {
            geysers += vespeneList.get(i).units();
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

        int vespeneMining = Math.min((int) (harvesters * BuildConstants.half), geysers * 3);
        int mineralMining = harvesters - vespeneMining;

        // Maximum effective harvesters (todo: account for more things)
        // First 2 harvesters per mineral field yield more minerals than the 3rd one.
        int highYieldHarvesters = Math.min(highYieldMineralHarvestingSlots, mineralMining);
        int lowYieldHarvesters = Math.min(lowYieldMineralHarvestingSlots, mineralMining - highYieldHarvesters);
        int muleYieldHarvesters = Math.min(lowYieldMineralHarvestingSlots, mules);

        // cout << mineralMining << " " << highYieldHarvesters << " " << lowYieldHarvesters << " " << foodAvailable() << endl;
        mineralsPerFrame = lowYieldHarvesters * BuildConstants.lowYieldMineralsPerFrame + highYieldHarvesters * BuildConstants.highYieldMineralsPerFrame + muleYieldHarvesters * BuildConstants.muleMineralsPerFrame;
        vespenePerFrame = vespeneMining * BuildConstants.vespenePerFrame;
    }
}
