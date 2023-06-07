package com.shadowcs.optimizer.sc2data.genetics;

import lombok.experimental.UtilityClass;

@UtilityClass
public class S2GameConstants {
    public static final float half = 0.5f;
    public static final float secondsToFrame = 1 / 22.4f;
    private static float minutesPerSecond = 1 / 60.0f;

    public static float lowYieldMineralsPerFrame = 22 * minutesPerSecond * secondsToFrame;
    public static float highYieldMineralsPerFrame = 40 * minutesPerSecond * secondsToFrame;
    public static float vespenePerFrame = 38 * minutesPerSecond * secondsToFrame;
    public static float muleMineralsPerFrame = highYieldMineralsPerFrame * 3.5f;

    // TODO: eventually we may want to do rich things
    public static float richVespeneMultiplier = 2f;
    public static float richMineralsMultiplier = 1.4f;

    public static final float startingEnergy = 50f;
    public static final float maxEnergy = 200f;

    public static final double maxFood = 200.0;
    public static final float energyPerFrame = 0.5625f * secondsToFrame;
    /**
     * This is so we can use multiplication which is much faster then division on a computer, pointless little improvement
     */
    public static final float energyPerFrameNeeded = 1 / energyPerFrame;

    public static final float chronoEnergy = 50f;
    public static final float chronoDuration = 20f * secondsToFrame;

    public static final float larvaEnergy = 25f; // How much energy to use ability
    public static final float larvaDuration = 29f * secondsToFrame; // How long until more larva are spanwed
    public static final float larvaInjectCooldown = 1.8f * secondsToFrame; // How long until a queen can use the ability again
    public static final float larvaNaturalRate = 11f * secondsToFrame; // How long to spawn one larva
    public static final float naturalMaxLarva = 3f; // How many larva are we able to have before we stop naturally spawning them
    public static final float maxLarva = 19f; // How many larva are we actually allowed to have
}
