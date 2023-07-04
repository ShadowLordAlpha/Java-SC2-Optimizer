package com.shadowcs.optimizer;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.game.Race;
import com.shadowcs.optimizer.engibay.build.EbAction;
import com.shadowcs.optimizer.engibay.build.EbBasicAction;
import com.shadowcs.optimizer.engibay.build.macro.EbMineResourceAction;
import com.shadowcs.optimizer.engibay.build.macro.EbNoopAction;
import com.shadowcs.optimizer.engibay.fitness.EbStandardFitness;
import com.shadowcs.optimizer.random.XORShiftRandom;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import com.shadowcs.optimizer.engibay.EbState;
import com.shadowcs.optimizer.engibay.fitness.EbFitness;
import com.shadowcs.optimizer.engibay.fitness.EbTimeFitness;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;
import io.jenetics.util.IntRange;

import java.util.ArrayList;
import java.util.Random;

import static io.jenetics.engine.Limits.bySteadyFitness;

public class Test {

    public static void main(String...args) {

        TechTree data = S2DataUtil.loadData();

        //var genes = S2DataUtil.generateGenes(data, Race.TERRAN, Race.ZERG, Race.PROTOSS);
        var actions = S2DataUtil.generateActions(data, Race.ZERG);

        actions.add(new EbNoopAction());
        actions.add(new EbMineResourceAction(false));
        actions.add(new EbMineResourceAction(true));

        var actionList = new ArrayList<>(actions);

        EbState init = new EbState();
        init.techTree(data);
        init.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        init.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_LARVA.getUnitTypeId(), 3);

        EbState goal = new EbState();
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 14);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 2);
        goal.upgradeSet().add(Upgrades.BURROW.getUpgradeId());

        // We use the Object here to assist us with drastically reducing the search space we need

        Random random = new XORShiftRandom();
        EbFitness fitness = new EbTimeFitness().goal(goal).initial(init);
        //EbFitness fitness = new EbStandardFitness().goal(goal).initial(init);

        Factory<Genotype<AnyGene<EbAction>>> gtf = Genotype.of(AnyChromosome.of(() -> actionList.get(random.nextInt(actionList.size())), 64));

        Engine<AnyGene<EbAction>, Double> engine = Engine
                .builder(
                        fitness::simulateOrderGt,
                        gtf
                )
                .populationSize(50)
                .survivorsSelector(new EliteSelector<>(2))
                .offspringSelector(new RouletteWheelSelector<>())
                .optimize(fitness.optimize())
                .alterers(new Mutator<>(0.25), new SinglePointCrossover<>(0.4))
                .build();

        Genotype<AnyGene<EbAction>> result = engine.stream()
                .limit(bySteadyFitness(1000))
                .collect(EvolutionResult.toBestGenotype());

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("Best solution: " + result);
        System.out.println("Best solution: " + result.geneCount());
        //result.stream().forEach(gene -> System.out.println(gene.gene().allele().name()));

        var order = fitness.simulateOrderOrder(result);
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println(order);
        System.out.println(fitness.score(order));
        System.out.println(order.currentFrame() + " / " + fitness.maxTime());
        System.out.println(order.currentFrame() / 22.4);
        System.out.println("Min prod: " + order.workersOnMinerals());
        System.out.println("Min: " + order.minerals());
        System.out.println("Gas Prod: " + order.workersOnGas());
        System.out.println("Gas: " + order.gas());
        System.out.println("Sup: " + order.supplyUsed());
        System.out.println("bad: " + order.badGenes());
        System.out.println("goo: " + order.validActions().size());
        System.out.println(goal.isSatisfied(order));

        for(var action: order.validActions()) {
            System.out.println(action.name());
        }
    }
}
