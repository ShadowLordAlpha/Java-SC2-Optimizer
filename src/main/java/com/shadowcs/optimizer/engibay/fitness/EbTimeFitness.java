package com.shadowcs.optimizer.engibay.fitness;

import com.shadowcs.optimizer.engibay.EbBuildOrder;
import lombok.Data;

/**
 * This fitness function attempts to satisfy the goal state in as little game time as possible. This function attempt to
 * maximize the score value, bad genes are penalized by a constant amount as is not satisfying the goal state but good
 * genes are not rewarded in this fitness function.
 *
 * This does not work well and is not recommended
 */
@Data
public class EbTimeFitness extends EbFitness {

    private double badGenePenalty = -1000;
    private double goodGenePenalty = 1;
    private double workerGoodGenePenalty = 2;
    private double noopGenePenalty = 0.5;

    @Override
    public double score(EbBuildOrder candidate) {

        double score = candidate.maxTime() - candidate.currentFrame();

        score += goal().isSatisfied(candidate) ? 0: -2 * candidate.maxTime();
        score += candidate.badGenes() * badGenePenalty;
        score += candidate.validActions().size() * goodGenePenalty;
        score += candidate.noop() * noopGenePenalty;
        score += (candidate.workersOnGas() + candidate.workersOnMinerals()) * workerGoodGenePenalty;

        return score;
    }
}
