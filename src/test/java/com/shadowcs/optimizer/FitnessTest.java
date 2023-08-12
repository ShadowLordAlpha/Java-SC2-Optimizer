package com.shadowcs.optimizer;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.game.Race;
import com.shadowcs.optimizer.engibay.EbState;
import com.shadowcs.optimizer.engibay.build.EbAction;
import com.shadowcs.optimizer.engibay.build.EbBasicAction;
import com.shadowcs.optimizer.engibay.fitness.EbFitness;
import com.shadowcs.optimizer.engibay.fitness.EbStandardFitness;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import io.jenetics.AnyGene;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class FitnessTest {

    @Test
    public void testFitness() {

        TechTree data = S2DataUtil.loadData();

        var actions = S2DataUtil.generateActions(data, Race.ZERG);

        EbState init = new EbState();
        init.techTree(data);
        init.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        init.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_LARVA.getUnitTypeId(), 3);

        EbState goal = new EbState();
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 15);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);

        var drone = actions.stream().filter(a -> ((EbBasicAction) a).name().equalsIgnoreCase("LARVATRAIN_DRONE")).findFirst().get();

        List<AnyGene<EbAction>> list = new ArrayList<>(List.of(AnyGene.of(() -> drone), AnyGene.of(() -> drone), AnyGene.of(() -> drone), AnyGene.of(() -> drone)));

        EbFitness fitness = new EbStandardFitness().goal(goal).initial(init);
        var build = fitness.simulateOrder(list, false);

        System.out.println(goal.isSatisfied(build));
        System.out.println(build.supplyCap());
        System.out.println(build.supplyAvailable());
        System.out.println(build.supplyUsed());
        System.out.println(build);


    }
}
