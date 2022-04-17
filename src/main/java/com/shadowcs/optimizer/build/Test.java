package com.shadowcs.optimizer.build;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.pojo.Pair;
import com.shadowcs.optimizer.sc2data.S2DataUtil;

public class Test {

    public static void main(String...args) {

        // Start with what units
        BuildState state = new BuildState(
                new Pair<>(Units.TERRAN_COMMAND_CENTER, 1),
                new Pair<>(Units.TERRAN_SCV, 12)
        );

        // What resources do we have
        state.resources().minerals(50);
        state.resources().vespene(0);

        // Find the build order
        BuildOrder buildOrder = BuildOrder.findFastestBuildOrder(state,
                new Pair<>(new BuildOrderItem(Abilities.BUILD_COMMAND_CENTER), 1));

        System.out.println(S2DataUtil.readResDataFile());
    }
}
