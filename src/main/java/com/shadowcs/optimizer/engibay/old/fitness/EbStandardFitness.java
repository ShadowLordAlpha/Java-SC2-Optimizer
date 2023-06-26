package com.shadowcs.optimizer.engibay.old.fitness;

import com.shadowcs.optimizer.engibay.old.EbBuildOrder;
import com.shadowcs.optimizer.engibay.old.EbState;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import lombok.Data;

@Data
public class EbStandardFitness extends EbFitness {

    private TechTree tree;

    @Override
    public double score(EbBuildOrder candidate) {

        double score = 0.0;

        score = augmentScore(score, candidate);

        if (goal().isSatisfied(candidate)) {

            score *= maxTime() / candidate.currentFrame();

            score = augmentScore(score, (int) candidate.minerals(), (int) goal().minerals(), .011, .011, false);
            score = augmentScore(score, (int) candidate.gas(), (int) goal().gas(), .015, .015, false);
        } else {

            score = augmentScore(score, (int) candidate.minerals(), (int) goal().minerals(), .0010, .0010, false);
            score = augmentScore(score, (int) candidate.gas(), (int) goal().gas(), .0014, .0014, false);
        }

        return score;
    }

    private double augmentScore(double score, EbState candidate) {

        for(int unitId: candidate.unitCountMap().keySet()) {

            var unit = tree.unitMap().get(unitId);

            score = augmentScore(score, candidate.unitCountMap().getOrDefault(unitId, 0), goal().unitCountMap().getOrDefault(unitId, 0), unit.minerals(), unit.minerals() / 100.0, false);
        }

        for(int upgradeId: candidate.upgradesMap().stream().toList()) {

            var upgrade = tree.upgrade().stream().filter(up -> up.id() == upgradeId).findFirst().get();

            if(goal().upgradesMap().contains(upgradeId)) {
                score = augmentScore(score, true, true, upgrade.cost().minerals(), upgrade.cost().minerals() / 100.0, false);
            } else {
                score = augmentScore(score, true, false, upgrade.cost().minerals(), upgrade.cost().minerals() / 100.0, false);
            }
        }

        return score;
    }
}
