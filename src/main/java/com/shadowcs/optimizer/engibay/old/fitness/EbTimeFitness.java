package com.shadowcs.optimizer.engibay.old.fitness;

import com.shadowcs.optimizer.engibay.old.EbBuildOrder;

/**
 * Note, for this one we want the LOWEST value, not the highest
 */
public class EbTimeFitness extends EbFitness {
    @Override
    public double score(EbBuildOrder candidate) {

        if(!goal().isSatisfied(candidate)) {
            return candidate.currentFrame() + 2000000;
        }

        // return 1.0 / (candidate.currentFrame() + 1); // This one is pure time based
        int unitcount = 0;
        for(int num: candidate.unitCountMap().values()) {
            unitcount += num;
        }
        return candidate.currentFrame() - unitcount;
    }
}
