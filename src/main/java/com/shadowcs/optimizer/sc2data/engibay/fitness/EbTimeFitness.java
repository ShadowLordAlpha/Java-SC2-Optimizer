package com.shadowcs.optimizer.sc2data.engibay.fitness;

import com.shadowcs.optimizer.sc2data.engibay.EbBuildOrder;
import com.shadowcs.optimizer.sc2data.engibay.EbState;

public class EbTimeFitness extends EbFitness {
    @Override
    public double score(EbBuildOrder candidate) {

        // return 1.0 / (candidate.currentFrame() + 1); // This one is pure time based
        return (1.0 / (candidate.currentFrame() + 1)) * 100.0 + (1.0 / (candidate.validActions().size() + 1)) * 10.0;
    }
}
