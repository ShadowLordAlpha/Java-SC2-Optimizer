package com.shadowcs.optimizer.genetics.sc2;

import com.github.ocraft.s2client.protocol.game.Race;
import com.shadowcs.optimizer.build.genetics.BuildOrderGenetics;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import org.junit.jupiter.api.Test;

/**
 * Test specifically the SC2 BuildOrderGenetics class to make sure that it correctly generates the needed Genes.
 */
public class SC2GeneticsTest {

    @Test
    public void terrainInitializationTest() {

        // TODO: pass in a path ahd have the object load what is needed???
        BuildOrderGenetics bog = new BuildOrderGenetics(S2DataUtil.loadUnitData(), S2DataUtil.loadUpgradeData(), S2DataUtil.loadAbilityData(), Race.TERRAN);


    }

    @Test
    public void protossInitializationTest() {

        // TODO: pass in a path ahd have the object load what is needed???
        BuildOrderGenetics bog = new BuildOrderGenetics(S2DataUtil.loadUnitData(), S2DataUtil.loadUpgradeData(), S2DataUtil.loadAbilityData(), Race.PROTOSS);


    }

    @Test
    public void zergInitializationTest() {

        // TODO: pass in a path ahd have the object load what is needed???
        BuildOrderGenetics bog = new BuildOrderGenetics(S2DataUtil.loadUnitData(), S2DataUtil.loadUpgradeData(), S2DataUtil.loadAbilityData(), Race.ZERG);


    }
}
