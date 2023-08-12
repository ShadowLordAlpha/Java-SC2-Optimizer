package com.shadowcs.optimizer;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.game.Race;
import com.shadowcs.optimizer.engibay.EbState;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StateTest {

    /**
     * Make sure a goal state and an init state of the same units list correctly meats that satisfied metric
     */
    @Test
    public void testStateSatisfiedZerg1() {

        EbState init = new EbState();
        init.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        init.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);

        EbState goal = new EbState();
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);

        assertTrue(goal.isSatisfied(init));
    }

    /**
     * Make sure that if our init state has more units and upgrades then our goal state needs it also meets the
     * satisfied metric
     */
    @Test
    public void testStateSatisfiedZerg2() {

        EbState init = new EbState();
        init.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 15);
        init.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 4);
        init.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 2);
        init.unitCountMap().put(Units.ZERG_EXTRACTOR.getUnitTypeId(), 2);
        init.unitCountMap().put(Units.ZERG_RAVAGER.getUnitTypeId(), 2);
        init.upgradeSet().add(Upgrades.BURROW.getUpgradeId());

        EbState goal = new EbState();
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);

        assertTrue(goal.isSatisfied(init));
    }

    @Test
    public void testStateSatisfiedZerg3() {

        EbState init = new EbState();
        init.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        init.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);
        init.upgradeSet().add(Upgrades.BURROW.getUpgradeId());

        EbState goal = new EbState();
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);
        goal.upgradeSet().add(Upgrades.BURROW.getUpgradeId());

        assertTrue(goal.isSatisfied(init));
    }

    @Test
    public void testStateFailedZerg1() {
        EbState init = new EbState();
        init.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        init.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);

        EbState goal = new EbState();
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 15);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 2);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 3);

        assertFalse(goal.isSatisfied(init));
    }

    @Test
    public void testStateFailedZerg2() {

        EbState init = new EbState();
        init.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        init.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);

        EbState goal = new EbState();
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);
        goal.upgradeSet().add(Upgrades.BURROW.getUpgradeId());

        assertFalse(goal.isSatisfied(init));
    }

    @Test
    public void testStateSupplyZerg1() {

        var tree = S2DataUtil.loadData();
        var actions = S2DataUtil.generateActions(tree, Race.ZERG);

        EbState goal = new EbState();
        goal.techTree(tree);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);

        assertEquals(14, goal.supplyCap());
        assertEquals(14, goal.supplyAvailable());
        assertEquals(0, goal.supplyUsed());
    }

    @Test
    public void testStateSupplyZerg2() {

        var tree = S2DataUtil.loadData();
        var actions = S2DataUtil.generateActions(tree, Race.ZERG);

        EbState goal = new EbState();
        goal.techTree(tree);
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_LARVA.getUnitTypeId(), 3);
        goal.upgradeSet().add(Upgrades.BURROW.getUpgradeId());

        assertEquals(14, goal.supplyCap());
        assertEquals(2, goal.supplyAvailable());
        assertEquals(12, goal.supplyUsed());
    }

    @Test
    public void testStateSupplyZerg3() {

        var tree = S2DataUtil.loadData();
        var actions = S2DataUtil.generateActions(tree, Race.ZERG);

        EbState goal = new EbState();
        goal.techTree(tree);
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 2);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 2);
        goal.unitCountMap().put(Units.ZERG_LARVA.getUnitTypeId(), 3);
        goal.upgradeSet().add(Upgrades.BURROW.getUpgradeId());

        assertEquals(28, goal.supplyCap());
        assertEquals(16, goal.supplyAvailable());
        assertEquals(12, goal.supplyUsed());
    }

    @Test
    public void testStateSatisfiedFutureZerg1() {

        var tree = S2DataUtil.loadData();
        var actions = S2DataUtil.generateActions(tree, Race.ZERG);

        EbState goal = new EbState();
        goal.techTree(tree);
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 2);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 2);
        goal.unitCountMap().put(Units.ZERG_LARVA.getUnitTypeId(), 3);
        goal.upgradeSet().add(Upgrades.BURROW.getUpgradeId());

        assertEquals(28, goal.supplyCap());
        assertEquals(16, goal.supplyAvailable());
        assertEquals(12, goal.supplyUsed());
    }
}
