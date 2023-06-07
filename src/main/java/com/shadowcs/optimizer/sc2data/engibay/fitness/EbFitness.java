package com.shadowcs.optimizer.sc2data.engibay.fitness;

public abstract class EbFitness {

    public abstract double score();

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
    protected double augmentScore(double score, boolean currentHas, boolean goalHas, int mula, double mulb, boolean waypoint) {
        return augmentScore(score, currentHas ? 1 : 0, goalHas ? 1 : 0, mula, mulb, waypoint);
    }

    private double augmentScore(double score, int currentHas, int goalHas, double mula, double mulb, boolean waypoint) {
        score += Math.max(Math.min(currentHas, goalHas), 0) * mula; // TODO: we should be able to remove max, we should never be below 0 anyway
        if (!waypoint)
            score += Math.max(currentHas - goalHas, 0) * mulb;
        return score;
    }
}
