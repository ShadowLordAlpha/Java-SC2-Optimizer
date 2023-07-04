package com.shadowcs.optimizer.engibay.fitness;

import com.shadowcs.optimizer.engibay.EbBuildOrder;
import com.shadowcs.optimizer.engibay.EbState;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import lombok.Data;

@Data
public class EbStandardFitness extends EbFitness {

    private double badGenePenalty = -1000;
    private double notSatisfiedPenalty = -200000;
    private double noopGenePenalty = -1;

    private double wantedUnit = 100;
    private double validUnit = 10;
    private double workintWorkerBonus = 50;
    private double workintWorkerBonusMax = 50 * 80;

    private double resourceBonus = 0.1;

    @Override
    public double score(EbBuildOrder candidate) {

        double score = 0;

        score += goal().isSatisfied(candidate) ? 0: notSatisfiedPenalty;
        score += candidate.badGenes() * badGenePenalty;
        score += candidate.noop() * noopGenePenalty;

        score += Math.min((candidate.workersOnGas() + candidate.workersOnMinerals()) * workintWorkerBonus, workintWorkerBonusMax);
        score += candidate.minerals() * resourceBonus;
        score += candidate.gas() * resourceBonus;

        for(int unitId: candidate.unitCountMap().keySet()) {
            score = augmentScore(score, candidate.unitCountMap().getOrDefault(unitId, 0), goal().unitCountMap().getOrDefault(unitId, 0), wantedUnit, validUnit, false);
        }

        for(int upgradeId: candidate.upgradeSet().toIntArray()) {
            score = augmentScore(score, candidate.upgradeSet().contains(upgradeId), goal().upgradeSet().contains(upgradeId), wantedUnit, validUnit, false);
        }

        return score;
    }
}
