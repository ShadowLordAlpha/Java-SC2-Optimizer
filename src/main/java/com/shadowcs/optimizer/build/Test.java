package com.shadowcs.optimizer.build;

import com.github.ocraft.s2client.protocol.data.Units;
import com.google.gson.Gson;
import com.shadowcs.optimizer.build.state.BuildState;
import com.shadowcs.optimizer.pojo.Pair;

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
                //new Pair<>(Units.TERRAN_COMMAND_CENTER, 2),
                //new Pair<>(Units.TERRAN_BARRACKS, 1),
                //new Pair<>(Units.TERRAN_STARPORT, 1),
                new Pair<>(Units.TERRAN_MARINE, 10),
                new Pair<>(Units.TERRAN_SCV, 20)
                //new Pair<>(Units.TERRAN_FACTORY, 1)
        );

        System.out.println(new Gson().toJson(buildOrder));
    }
}
