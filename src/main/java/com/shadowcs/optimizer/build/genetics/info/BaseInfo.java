package com.shadowcs.optimizer.build.genetics.info;

import com.shadowcs.optimizer.pojo.IntPair;
import lombok.Data;

@Data
public class BaseInfo {

    private static final IntPair maxSlots = new IntPair(16, 8);
    private static final IntPair threeSlots = new IntPair(12, 6);
    private static final IntPair twoSlots = new IntPair(8, 4);
    private static final IntPair oneSlots = new IntPair(2, 1);
    private static final IntPair empty = new IntPair(0, 0);

    private boolean richMinerals;
    private boolean richVespene;
    private float remainingMinerals = 0;
    private float remainingVespene1 = 0;
    private float remainingVespene2 = 0;

    public BaseInfo() {
        this(false, false, 10800, 2250, 2250);
    }

    public BaseInfo(boolean richMinerals, boolean richVespene) {
        this(richMinerals, richVespene, richMinerals ? 8100: 10800, 2250, richVespene ? 0: 2250);
    }

    public BaseInfo(boolean richMinerals, boolean richVespene, float minerals, float vespene1, float vespene2) {
        richMinerals(richMinerals);
        richVespene(richVespene);
        remainingMinerals(minerals);
        remainingVespene1(vespene1);
        remainingVespene2(vespene2);
    }

    public void mineMinerals(float amount) {
        remainingMinerals = Math.max(0.0f, remainingMinerals - amount);
    }

    public void gatherVespene1(float amount) {
        remainingVespene1 = Math.max(0.0f, remainingVespene1 - amount);
    }

    /**
     * Returns (high yield, low yield) mineral slots on this expansion.
     */
    public IntPair mineralSlots() {
        if(richMinerals) {
            // Max is 8100 for a gold expansion with 6 patches
            if (remainingMinerals > 4000) return threeSlots;
            if (remainingMinerals > 100) return twoSlots;
            if (remainingMinerals > 0) return oneSlots;
        } else {
            // Max is 10800 for an expansion with 8 patches
            if (remainingMinerals > 4800) return maxSlots;
            if (remainingMinerals > 4000) return threeSlots;
            if (remainingMinerals > 100) return twoSlots;
            if (remainingMinerals > 0) return oneSlots;
        }

        return empty;
    }
}
