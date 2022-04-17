package com.shadowcs.optimizer.genetics;

/**
 * Having a good and functioning Fitness equation is INSANELY IMPORTANT. Even if you have one that works and will give
 * you the correct value the difference between one that works and a good one can be thousands of loops. From 10-20]
 * loops to find a simple value to 300000 to find the same value. With even larder search spaces like RTS games this
 * gets even worse as there are more possibilities.
 *
 * @param <E>
 */
@FunctionalInterface
public interface Fitness<E> {

    /**
     * Calculate the fitness of a given Chromosome. It is expected that external data might need to be used, that is
     * fine
     *
     * @param chromo
     * @return
     */
    double calculate(Chromosome<E> chromo);
}
