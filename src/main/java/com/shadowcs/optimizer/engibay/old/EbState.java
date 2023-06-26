package com.shadowcs.optimizer.engibay.old;

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

    /**
     * This is game time in simulation frames, 22.4 frames per second
     */
    private int currentFrame = 0;

    /**
     * This is the supply used up by in active production units
     */
    private int activeSupply = 0;

    /**
     * This map contains a list of all our units by their ID and how many of that unit we currently have available. Even
     * if a unit is in use it should be in this map as a count, if we don't have any of a unit then it should be removed
     * from the list
     */
    private final Int2IntOpenHashMap unitCountMap = new Int2IntOpenHashMap(225); // There are 204 unit definitions so we do slightly larger

    /**
     * This map contains a list of all our upgrades, if the value exists in the list we have it, otherwise we don't
     */
    private final IntOpenHashSet upgradesMap = new IntOpenHashSet(150); // There are 122 upgrades so we do slightly larger just in case

    /**
     * A list of waypoints for this state in terms of state
     *
     * TODO: something with this
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

    public double supply() {
        double supply = 0.0;

        for(int data: unitCountMap.keySet()) {
            supply -= techTree().unitMap().get(data).supply() * unitCountMap.get(data);
        }

        return supply - activeSupply;
    }

    public double supplyUsed() {

        double supply = 0.0;

        for(int data: unitCountMap.keySet()) {
            double supp = techTree().unitMap().get(data).supply();
            if(supp > 0) {
                supply += supp;
            }
        }

        return supply;
    }

    public double supplyCap() {

        double supply = 0.0;

        for(int data: unitCountMap.keySet()) {
            double supp = techTree().unitMap().get(data).supply();
            if(supp < 0) {
                supply -= supp;
            }
        }

        return supply;
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

        for(var upgradeId: upgradesMap.toArray()) {
            if(!candidate.upgradesMap.contains(upgradeId)) {
                return false;
            }
        }

        return true;
    }
}
