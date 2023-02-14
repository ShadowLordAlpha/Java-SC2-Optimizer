package com.shadowcs.optimizer.random;

import java.util.Random;

/**
 * An implementation of the XORShift random number generator that extends the
 * {@link java.util.Random} class.
 */
public class XORShiftRandom extends Random {

    private long seed;

    /**
     * Constructs a new XORShift random number generator using a default seed
     * value of the current nanosecond time.
     */
    public XORShiftRandom() {
        this(System.nanoTime());
    }

    /**
     * Constructs a new XORShift random number generator using the specified
     * seed value.
     *
     * @param seed the seed value for the random number generator
     */
    public XORShiftRandom(long seed) {
        this.seed = seed;
    }

    /**
     * Generates the next pseudorandom number.
     *
     * @param bits the number of random bits to generate
     * @return the next pseudorandom value from this random number generator's sequence
     */
    protected int next(int bits) {
        seed ^= (seed << 21);
        seed ^= (seed >>> 35);
        seed ^= (seed << 4);
        return (int)(seed & ((1L << bits) -1));
    }
}
