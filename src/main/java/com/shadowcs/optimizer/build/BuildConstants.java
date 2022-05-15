package com.shadowcs.optimizer.build;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BuildConstants {

    public static final float secondsToFrame = 22.4f;

    public boolean isTownHall(UnitType type) {
        return type == Units.TERRAN_COMMAND_CENTER || type == Units.TERRAN_ORBITAL_COMMAND || type == Units.TERRAN_PLANETARY_FORTRESS
                || type == Units.PROTOSS_NEXUS
                || type == Units.ZERG_HATCHERY || type == Units.ZERG_HIVE || type == Units.ZERG_LAIR;
    }

    public boolean isBasicHarvester(UnitType type) {
        return type == Units.ZERG_DRONE || type == Units.PROTOSS_PROBE || type == Units.TERRAN_SCV;
    }

    public boolean isVespeneHarvester(UnitType type) {
        return type == Units.ZERG_EXTRACTOR || type == Units.ZERG_EXTRACTOR_RICH
                || type == Units.TERRAN_REFINERY || type == Units.TERRAN_REFINERY_RICH
                || type == Units.PROTOSS_ASSIMILATOR || type == Units.PROTOSS_ASSIMILATOR_RICH;
    }

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
