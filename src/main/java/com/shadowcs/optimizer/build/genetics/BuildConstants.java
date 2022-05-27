package com.shadowcs.optimizer.build.genetics;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BuildConstants {

    public static final float half = 1 / 2f;
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
    public static final float energyPerFrame = 0.5625f * secondsToFrame;
    /**
     * This is so we can use multiplication which is much faster then division on a computer, pointless little improvement
     */
    public static final float energyPerFrameNeeded = 1 / energyPerFrame;

    public static final float chronoEnergy = 50f;
    public static final float chronoDuration = 20f * secondsToFrame;

    public static final float larvaEnergy = 25f; // How much energy to use ability
    public static final float larvaDuration = 29f * secondsToFrame; // How long until more larva are spanwed
    public static final float larvaCooldown = 1.8f * secondsToFrame; // How long until a queen can use the ability again
    public static final float larvaNaturalRate = 11f * secondsToFrame; // How long to spawn one larva
    public static final float naturalMaxLarva = 3f; // How many larva are we able to have before we stop naturally spawning them
    public static final float maxLarva = 19f; // How many larva are we actually allowed to have

    public static final List<Integer> townHall = new ArrayList<>(List.of(Units.TERRAN_COMMAND_CENTER.getUnitTypeId(), Units.TERRAN_ORBITAL_COMMAND.getUnitTypeId(), Units.TERRAN_PLANETARY_FORTRESS.getUnitTypeId(),
            Units.PROTOSS_NEXUS.getUnitTypeId(),
            Units.ZERG_HATCHERY.getUnitTypeId(), Units.ZERG_HIVE.getUnitTypeId(), Units.ZERG_LAIR.getUnitTypeId()));

    public static final List<Integer> basicHarvester = new ArrayList<>(List.of(Units.ZERG_DRONE.getUnitTypeId(), Units.PROTOSS_PROBE.getUnitTypeId(), Units.TERRAN_SCV.getUnitTypeId()));
    public static final List<Integer> vespeneHarvester = new ArrayList<>(List.of(Units.ZERG_EXTRACTOR.getUnitTypeId(), Units.ZERG_EXTRACTOR_RICH.getUnitTypeId(),
            Units.TERRAN_REFINERY.getUnitTypeId(), Units.TERRAN_REFINERY_RICH.getUnitTypeId(),
            Units.PROTOSS_ASSIMILATOR.getUnitTypeId(), Units.PROTOSS_ASSIMILATOR_RICH.getUnitTypeId()));


    public UnitType isAddonFor(UnitType type) {
        if(type == Units.TERRAN_BARRACKS_TECHLAB || type == Units.TERRAN_BARRACKS_REACTOR) {
            return Units.TERRAN_BARRACKS;
        } else if(type == Units.TERRAN_FACTORY_TECHLAB || type == Units.TERRAN_FACTORY_REACTOR) {
            return Units.TERRAN_FACTORY;
        } else if(type == Units.TERRAN_STARPORT_TECHLAB || type == Units.TERRAN_STARPORT_REACTOR) {
            return Units.TERRAN_STARPORT;
        }

        return null;
    }
}
