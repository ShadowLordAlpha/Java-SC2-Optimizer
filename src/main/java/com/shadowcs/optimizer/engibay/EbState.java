package com.shadowcs.optimizer.engibay;

import com.shadowcs.optimizer.sc2data.models.TechTree;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents our current state and is one of several "types" Specifically the starting state, our simulation
 * state, or out goal state. Each state may also have a list of waypoints that they are supposed to hit and are
 * considered to be part of that state instead of their own standalone state.
 */
@Data
public class EbState {

    /**
     * What is the techtree we are working with currently
     */
    private TechTree techTree; // TODO: do i actually need this? I don't think so

    /**
     * How many minerals do we have
     */
    private double minerals = 50;

    /**
     * How much gas do we have
     */
    private double gas = 0;

    // TODO: a target frame?

    /**
     * This contains a list of all our units that are currently completed.
     */
    private final Int2IntOpenHashMap unitCountMap = new Int2IntOpenHashMap(20); // There are 204 unit definitions but we should never be using them all so set to about half

    /**
     * This map contains a list of all our completed upgrades
     */
    private final IntOpenHashSet upgradeSet = new IntOpenHashSet(10); // There are 122 upgrades but normally only a few are actually used, so we prepopulate to a reasonable amount

    /**
     * A list of waypoints for this state in terms of state
     *
     * TODO: something with this...
     */
    private final List<EbState> waypoints = new ArrayList<>();

    public int bases() {
        int bases = 0;

        for(int data: unitCountMap.keySet()) {
            if(techTree().unitMap().get(data).townhall()) {
                bases++;
            }
        }

        return bases;
    }

    public double supplyAvailable() {
        return supplyCap() - supplyUsed();
    }

    public double supplyUsed() {

        double supply = 0.0;

        for(int data: unitCountMap.keySet()) {
            double supp = techTree().unitMap().get(data).supply();
            if(supp > 0) {
                supply += (supp * unitCountMap.getOrDefault(data, 0));
            }
        }

        return supply;
    }

    public double supplyCap() {

        double supply = 0.0;

        for(int data: unitCountMap.keySet()) {
            double supp = techTree().unitMap().get(data).supply();
            if(supp < 0) {
                supply -= (supp * unitCountMap.getOrDefault(data, 0));
            }
        }

        return Math.min(supply, EngineeringBay.MAX_SUPPLY);
    }

    public boolean isSatisfied(EbState candidate) {

        for(var unitId: unitCountMap.keySet()) {
            if(!candidate.unitCountMap.containsKey(unitId)) {
                return false;
            }

            if(candidate.unitCountMap.get(unitId) < unitCountMap.get(unitId)) {
                return false;
            }
        }

        for(var upgradeId: upgradeSet.toIntArray()) {
            if(!candidate.upgradeSet.contains(upgradeId)) {
                return false;
            }
        }

        return true;
    }

    public boolean isSatisfiedFuture(EbBuildOrder candidate) {

        for(var unitId: unitCountMap.keySet()) {
            if(!candidate.unitCountMap().containsKey((int) unitId) && !candidate.unitInProgressMap().containsKey((int) unitId)) {
                return false;
            }

            if(candidate.unitCountMap().getOrDefault((int) unitId, 0) + candidate.unitInProgressMap().getOrDefault((int) unitId, 0)  < unitCountMap.getOrDefault((int) unitId, 0)) {
                return false;
            }
        }

        for(var upgradeId: upgradeSet.toIntArray()) {
            if(!candidate.upgradeSet().contains(upgradeId) && !candidate.upgradesInProgressMap().contains(upgradeId)) {
                return false;
            }
        }

        return true;
    }
}
