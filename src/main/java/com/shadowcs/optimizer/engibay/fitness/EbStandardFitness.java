package com.shadowcs.optimizer.engibay.fitness;

import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.engibay.EbBuildOrder;
import com.shadowcs.optimizer.engibay.EbState;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import lombok.Data;

@Data
public class EbStandardFitness extends EbFitness {

    private double badGenePenalty = -1000;
    private double notSatisfiedPenalty = -200000;
    private double noopGenePenalty = 15;
    private double goodGenePenalty = 5;

    private double wantedUnit = 100;
    private double droneBonus = 5;
    private double validUnit = 10;
    private double workintWorkerBonus = 50;
    private double workintWorkerBonusMax = 50 * 80;

    private double resourceBonus = 0.1;

    @Override
    public double score(EbBuildOrder candidate) {

        double score = (1 - (candidate.currentFrame() / candidate.maxTime())) * 200000.0;

        score += goal().isSatisfied(candidate) ? 0: -2 * candidate.maxTime();
        score += candidate.badGenes() * badGenePenalty;
        score += candidate.validActions().size() * goodGenePenalty;
        score += candidate.noop() * noopGenePenalty;
        // score += (candidate.workersOnGas() + candidate.workersOnMinerals()) * workerGoodGenePenalty;

        for(int unitId: candidate.unitCountMap().keySet()) {
            score += logarithmDecay(candidate.unitCountMap().getOrDefault(unitId, 0), goal().unitCountMap().getOrDefault(unitId, 0)) * wantedUnit;
        }

       //score += (candidate.unitCountMap().getOrDefault(Units.ZERG_DRONE.getUnitTypeId(), 0) + candidate.unitCountMap().getOrDefault(Units.PROTOSS_PROBE.getUnitTypeId(), 0) + candidate.unitCountMap().getOrDefault(Units.TERRAN_SCV.getUnitTypeId(), 0)) * droneBonus;

        for(int upgradeId: candidate.upgradeSet().toIntArray()) {
            score += candidate.upgradeSet().contains(upgradeId) == goal().upgradeSet().contains(upgradeId) ? wantedUnit: 0;
        }

        return score;
    }

    protected double logarithmDecay(int currentUnits, int desiredUnits) {
        if (currentUnits <= desiredUnits) {
            // Give full score for each unit up to the desired amount.
            return currentUnits;
        } else {
            // For units beyond the desired amount, give a diminishing score.
            // In this example, we use the natural logarithm for the decay.
            return desiredUnits + Math.log(currentUnits - desiredUnits + 1);
        }
    }
}
