package com.shadowcs.optimizer.build.state;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.build.genetics.BuildConstants;
import com.shadowcs.optimizer.build.genetics.info.BuildUnitInfo;
import com.shadowcs.optimizer.pojo.Pair;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The BuildState represents the current state of the build at some point during the build. When created this is the
 * initial state.
 */
@Data
public class BuildState {

    /**
     * Use a cache to get a list of BuildUnitInfo specifically related to that base unit. This is used to increase our
     * access speed of the data. The list is used as we need to keep track of addons for a select few units
     */
    private final HashMap<UnitType, BuildUnitInfo> unitInfoMap = new HashMap<>();

    /**
     * The resources we have available to us.
     */
    private final BuildResources resources = new BuildResources(0, 0);

    public BuildState(Pair<UnitType, Integer>...units) {
        for(var unit: units) {
            addUnit(unit.first(), unit.second());
        }
    }

    /**
     * @param type
     * @param delta
     * @return
     */
    public BuildState addUnit(UnitType type, int delta) {

        // Check if this is an addon and if that addon is a techlab
        boolean addon = BuildConstants.isAddon(type);
        boolean techlab = BuildConstants.isTechlab(type);

        int techCount = 0;
        int reacCount = 0;

        // if we are an addon what is the actual unit we are adding this for
        if(addon) {
            UnitType temp = BuildConstants.isAddonFor(type);
            if(temp != null) {
                type = temp;

                // Addons cannot have addons so we only need to do this if we find that it is an "addon"
                if(techlab) {
                    techCount = delta;
                } else {
                    reacCount = delta;
                }
            }
        }

        var info = unitInfoMap.computeIfAbsent(type, BuildUnitInfo::new);
        info.units(info.units() + delta).addonTechlab(info.addonTechlab() + techCount).addonReactor(info.addonReactor() + reacCount);

        return this;
    }
}
