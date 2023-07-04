package com.shadowcs.optimizer.engibay;

/**
 * This is a Genetic Algorithm base build order finder with ideas from <a href="https://code.google.com/archive/p/evolutionchamber/">EvolutionChamber</a>
 * mixed with the paper Build Order Optimization in StarCraft by David Churchill and Michael Buro.
 */
public class EngineeringBay {

    public static boolean DEBUG = false;
    public static boolean PULL_THREE_WORKERS = false;

    public static int MAX_SUPPLY = 200;

    /**
     * Roughly how much mining per worker can we get, using these values the math is much faster
     */
    public static double MULE_MINERALS_PER_FRAME = 0.045; // 61.22 / 60.0 / 22.4
    public static double WORKER_MINERALS_PER_FRAME = 0.035; // 143.0 / 3.0 / 60.0 / 22.4
    public static double WORKER_MINERALS_PER_FRAME_RICH = 0.05; // 200.0 / 3.0 / 60.0 / 22.4

    /**
     * Roughly  how much mining per worker can we get for gas, This speeds up the calculations quite a bit
     */
    public static double WORKER_GAS_PER_FRAME = 0.04; // 163.0 / 3.0 / 60.0 / 22.4
    public static double WORKER_GAS_PER_FRAME_RICH = 0.08; // 325.0 / 3.0 / 60.0 / 22.4

    public EngineeringBay() {

    }


}
