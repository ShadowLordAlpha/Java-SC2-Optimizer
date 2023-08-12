package com.shadowcs.optimizer.engibay.fitness;

import com.shadowcs.optimizer.engibay.EbBuildOrder;
import com.shadowcs.optimizer.engibay.EbState;
import com.shadowcs.optimizer.engibay.build.EbAction;
import io.jenetics.AnyGene;
import io.jenetics.Genotype;
import io.jenetics.Optimize;
import lombok.Data;

import java.util.List;

@Data
public abstract class EbFitness {

    @Deprecated
    private double maxTime = 2.0 * 60.0 * 60.0 * 22.4;

    private EbState initial;
    private EbState goal;

    public Optimize optimize() {
        return Optimize.MAXIMUM;
    }

    public double simulateOrderGt(Genotype<AnyGene<EbAction>> gt) {

        return score(simulateOrderOrder(gt));
    }

    public EbBuildOrder simulateOrderOrder(Genotype<AnyGene<EbAction>> gt) {

        var list = gt.chromosome().stream().toList();

        return simulateOrder(list, false);
    }

    public EbBuildOrder simulateOrder(List<AnyGene<EbAction>> genes, boolean order) {

        EbBuildOrder candidate = EbBuildOrder.create(initial);
        candidate.workersOnMinerals(12);

        mainloop: for(var gene: genes) {

            EbAction action = gene.allele();

            if(!action.isValid(candidate)) {
                // TODO: mark invalid action?
                candidate.badGenes(candidate.badGenes() + 1);
                continue;
            }

            int time;
            while((time = action.canExecute(candidate)) > 0) {
                time = Math.min(time, candidate.getNextActionFrame() - candidate.currentFrame());
                candidate.executeFutureActions(time);

                if(maxTime < candidate.currentFrame()) {
                    candidate.badGenes(candidate.badGenes() + 1);
                    continue mainloop;
                }
            }

            candidate.validActions().add(action);
            action.execute(candidate);

            // As we basically always want to get the fastest in some manner we can end right after we meat the goal
            // we just need this if to be off if we don't want this for some reason
            // This if Check if we will be satisfied in the future based off the current actions
            if(goal.isSatisfiedFuture(candidate)) {
                // TODO: can probably replace this loop now with a last action and the loop for all the future actions... i'll do it later
                while(candidate.futureActions().size() > 0) {
                    candidate.executeNextAction();
                }

                if(goal.isSatisfied(candidate)) {
                    break;
                }
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
