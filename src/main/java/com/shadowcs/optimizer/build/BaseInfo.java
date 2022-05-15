package com.shadowcs.optimizer.build;

import com.shadowcs.optimizer.pojo.Pair;
import lombok.Data;

@Data
public class BaseInfo {
    private float remainingMinerals = 0;
    private float remainingVespene1 = 0;
    private float remainingVespene2 = 0;

    public BaseInfo(float minerals, float vespene1, float vespene2) {
        remainingMinerals(minerals);
        remainingVespene1(vespene1);
        remainingVespene2(vespene2);
    }

    public void mineMinerals(float amount) {
        remainingMinerals = Math.max(0.0f, remainingMinerals - amount);
    }

    /**
     * Returns (high yield, low yield) mineral slots on this expansion.
     *
     * TODO: we may want to change this and allow rich
     *  type values as well. though that is not needed for the initial version
     *
     */
    public Pair<Integer, Integer> mineralSlots() {
        // Max is 10800 for an expansion with 8 patches
        if (remainingMinerals > 4800) return new Pair<>(16, 8);
        if (remainingMinerals > 4000) return new Pair<>(12, 6);
        if (remainingMinerals > 100) return new Pair<>(8, 4);
        if (remainingMinerals > 0) return new Pair<>(2, 1);
        return new Pair<>(0, 0);
    }
}
