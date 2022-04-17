package com.shadowcs.optimizer.build;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.pojo.Pair;
import lombok.Data;

import java.util.ArrayList;
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
    private final LoadingCache<UnitType, List<BuildUnitInfo>> unitInfoMap = Caffeine.newBuilder().build(key -> new ArrayList<>());

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
        return addUnit(type, Units.INVALID, delta);
    }

    /**
     *
     * @param type
     * @param addon
     * @param delta
     * @return
     */
    public BuildState addUnit(UnitType type, UnitType addon, int delta) {

        if(delta != 0) {
            var typeList = unitInfoMap.get(type);
            if(!typeList.isEmpty()) {
                for (var buildInfo : typeList) {
                    // We already know that the type is the same, now we need to check the addon
                    if (addon.equals(buildInfo.addon())) {
                        int unitCount = buildInfo.units() + delta;
                        if (unitCount < 0) {
                            throw new RuntimeException("Cannot remove more units then we have (negative units)!");
                        } else if(unitCount == 0) {
                            // FIXME: probably a concurrent modification exception here.
                            typeList.remove(buildInfo);
                        } else {
                            buildInfo.units(unitCount);
                        }
                    }
                }
            } else {
                if(delta < 0) {
                    throw new RuntimeException("Cannot create negative units!");
                }

                typeList.add(new BuildUnitInfo(type, addon).units(delta));
            }
        }

        return this;
    }
}
