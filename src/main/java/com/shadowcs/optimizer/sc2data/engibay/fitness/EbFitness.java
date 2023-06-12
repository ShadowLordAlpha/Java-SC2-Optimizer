package com.shadowcs.optimizer.sc2data.engibay.fitness;

import com.google.gson.Gson;
import com.shadowcs.optimizer.genetics.Gene;
import com.shadowcs.optimizer.sc2data.engibay.EbBuildOrder;
import com.shadowcs.optimizer.sc2data.engibay.EbState;
import com.shadowcs.optimizer.sc2data.engibay.action.EbAction;
import io.jenetics.AnyGene;
import io.jenetics.Genotype;
import lombok.Data;

@Data
public abstract class EbFitness {

    private double maxTime = 2.0 * 60.0 * 60.0 * 22.4;

    private EbState initial;
    private EbState goal;

    public double simulateOrderGt(Genotype<AnyGene<EbAction>> gt) {

        var list = gt.chromosome().stream().toList();
        EbAction[] arr = new EbAction[list.size()];
        for(int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i).allele();
        }

        return score(simulateOrder(arr, false));
    }

    public EbBuildOrder simulateOrderOrder(Genotype<AnyGene<EbAction>> gt) {

        var list = gt.chromosome().stream().toList();
        EbAction[] arr = new EbAction[list.size()];
        for(int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i).allele();
        }

        return simulateOrder(arr, false);
    }

    public EbBuildOrder simulateOrder(EbAction[] genes, boolean order) {

        EbBuildOrder candidate = EbBuildOrder.create(initial);

        // TODO: workers to minerals stuff instead of hardcode
        candidate.workersOnMinerals(12);

        for(EbAction action: genes) {

            // EbAction action = gene.data();

            if(!action.isValid(candidate)) {
                // TODO: mark invalid action?
                continue;
            }

            while(!action.canExecute(candidate)) {
                if(maxTime < candidate.currentFrame()) {
                    return candidate;
                }
            }

            candidate.validActions().add(action);
            action.execute(candidate);

            if(goal.isSatisfied(candidate)) {

                // We are done, we just need to finish up any running action
                if(candidate.futureActions().size() > 0) {
                    candidate.executeNextAction();
                }

                break;
            }
        }

        return candidate;
    }

    public abstract double score(EbBuildOrder candidate);

    /**
     * TODO: see if we actually need this kind, as we may just automatically convert everything instead
     *
     * Convert our boolean types for upgrades/research into numbers that can be easily used with normal calculations
     *
     * @param score
     * @param currentHas
     * @param goalHas
     * @param mula
     * @param mulb
     * @param waypoint TODO: do we need this? i don't believe so as its always false atm
     * @return
     */
    protected double augmentScore(double score, boolean currentHas, boolean goalHas, double mula, double mulb, boolean waypoint) {
        return augmentScore(score, currentHas ? 1 : 0, goalHas ? 1 : 0, mula, mulb, waypoint);
    }

    protected double augmentScore(double score, int currentHas, int goalHas, double mula, double mulb, boolean waypoint) {
        score += Math.max(Math.min(currentHas, goalHas), 0) * mula; // TODO: we should be able to remove max, we should never be below 0 anyway
        if (!waypoint)
            score += Math.max(currentHas - goalHas, 0) * mulb;
        return score;
    }
}
