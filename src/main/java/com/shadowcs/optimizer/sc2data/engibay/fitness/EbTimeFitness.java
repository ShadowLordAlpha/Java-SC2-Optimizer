package com.shadowcs.optimizer.sc2data.engibay.fitness;

import com.shadowcs.optimizer.sc2data.engibay.EbBuildOrder;
import com.shadowcs.optimizer.sc2data.engibay.EbState;

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
