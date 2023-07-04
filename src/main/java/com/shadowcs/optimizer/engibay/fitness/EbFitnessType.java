package com.shadowcs.optimizer.engibay.fitness;

import lombok.Getter;

/**
 * Each of these values represent a slightly different fitness function that calculates the final score of a build order
 * slightly differently for each.
 *
 * @author Josh "Shadow"
 */
public enum EbFitnessType {

    /**
     * A fitness type for a fitness function that is roughly standard in how it prioritizes building economy units vs
     * army units.
     */
    STANDARD(new EbStandardFitness()),

    /**
     * A fitness type that puts slightly more focus on economy than standard
     */
    ECON(new EbEconFitness()),

    /**
     * A fitness type that puts slightly more focus on army than standard
     */
    ARMY(new EbArmyFitness());

    @Getter private final EbFitness fitness;

    EbFitnessType(EbFitness fitness) {
        this.fitness = fitness;
    }
}
