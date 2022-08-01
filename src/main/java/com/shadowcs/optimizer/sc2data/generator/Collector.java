package com.shadowcs.optimizer.sc2data.generator;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.*;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.Race;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shadowcs.optimizer.sc2data.models.CostS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Collector {

    public static void main(String...args) {

        S2Coordinator s2Coordinator = S2Coordinator.setup()
                .loadSettings(args)
                .setMultithreaded(true)
                //.setUseGeneralizedAbilityId(true)
                //.setFeatureLayers(builder.build())
                //.setRawAffectsSelection(true)
                .setShowBurrowed(true)
                .setShowCloaked(true)
                .setTimeoutMS(240000)
                .setParticipants(
                        S2Coordinator.createParticipant(Race.TERRAN, new CollectorBot(), "Collector"))

                .launchStarcraft()
                .startGame(LocalMap.of(Paths.get("Melee/Empty128.SC2MAP")));

        while(s2Coordinator.update()) {

        }

        s2Coordinator.quit();
    }

    private static class CollectorBot extends S2Agent {

        @Override
        public void onGameStart() {
            super.onGameStart();

            System.out.println(control().proto().getBaseBuild());
            System.out.println(control().proto().getDataVersion());

            var unData = CollectData.collectUnitBuildData(this);
            var aData = CollectData.collectAbilityBuildData(unData,  this);
            var uData = CollectData.collectUpgradeBuildData(this);

            debug().debugShowMap();
            debug().debugGodMode();
            debug().debugIgnoreFood();
            debug().debugEndGame(true);
            //for(var data: unitTypeData) {
                //debug().debugCreateUnit(Units.from(data.id()), observation().getGameInfo().findCenterOfMap(), observation().getPlayerId(), 1);
            //}

            debug().sendDebug();
        }

        @Override
        public void onUnitCreated(UnitInPool unitInPool) {
            super.onUnitCreated(unitInPool);


        }

        @Override
        public void onStep() {
            super.onStep();
        }
    }
}
