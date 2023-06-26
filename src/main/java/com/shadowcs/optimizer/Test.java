package com.shadowcs.optimizer;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.shadowcs.optimizer.random.XORShiftRandom;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import com.shadowcs.optimizer.engibay.old.EbRequirementTree;
import com.shadowcs.optimizer.engibay.old.EbState;
import com.shadowcs.optimizer.engibay.old.action.EbAction;
import com.shadowcs.optimizer.engibay.old.fitness.EbFitness;
import com.shadowcs.optimizer.engibay.old.fitness.EbTimeFitness;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

import java.util.Random;

import static io.jenetics.engine.Limits.bySteadyFitness;

public class Test {

    public static void main(String...args) {

        TechTree data = S2DataUtil.loadData();

        //var genes = S2DataUtil.generateGenes(data, Race.TERRAN, Race.ZERG, Race.PROTOSS);
        var actions = S2DataUtil.generateActions(data, Race.ZERG);

        /*EbState init = new EbState();
        init.techTree(data);
        init.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        init.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_LARVA.getUnitTypeId(), 3);

        EbState goal = new EbState();
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 14);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_EXTRACTOR.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 2);
        //goal.upgradesMap().add(Upgrades.BURROW.getUpgradeId());

        EbRequirementTree requirementTree = new EbRequirementTree(data, goal);

        System.out.println(requirementTree);

        Random random = new XORShiftRandom();
        EbFitness fitness = new EbTimeFitness().goal(goal).initial(init);

        Factory<Genotype<AnyGene<EbAction>>> gtf = Genotype.of(AnyChromosome.of(() -> requirementTree.actionSet().get(random.nextInt(requirementTree.actionSet().size())), 32));

        Engine<AnyGene<EbAction>, Double> engine = Engine
                .builder(
                        fitness::simulateOrderGt,
                        gtf
                )
                .populationSize(50)
                .survivorsSelector(new EliteSelector<>(5))
                .optimize(Optimize.MINIMUM)
                .alterers(new Mutator<>(0.1), new MultiPointCrossover<>(0.6))
                .build();

        Genotype<AnyGene<EbAction>> result = engine.stream()
                .limit(bySteadyFitness(100))
                .collect(EvolutionResult.toBestGenotype());

        System.out.println("Best solution: " + result);
        //result.stream().forEach(gene -> System.out.println(gene.gene().allele().name()));

        var order = fitness.simulateOrderOrder(result);
        System.out.println(order);
        System.out.println(fitness.score(order));
        System.out.println(order.currentFrame() + " / " + fitness.maxTime());
        System.out.println(order.currentFrame() / 22.4);
        System.out.println(order.minerals());
        System.out.println(order.gas());
        System.out.println(order.supply());
        System.out.println(goal.isSatisfied(order));

        for(var action: order.validActions()) {
            System.out.println(action.name());
        }
    }

    /*public static void test(String...args) {
        TechTree data = S2DataUtil.loadData();

        var genes = S2DataUtil.generateGenes(data, Race.TERRAN, Race.ZERG, Race.PROTOSS);

        EbState init = new EbState();
        init.techTree(data);
        init.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 12);
        init.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 1);
        init.unitCountMap().put(Units.ZERG_LARVA.getUnitTypeId(), 3);

        EbState goal = new EbState();
        goal.unitCountMap().put(Units.ZERG_DRONE.getUnitTypeId(), 14);
        goal.unitCountMap().put(Units.ZERG_OVERLORD.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_EXTRACTOR.getUnitTypeId(), 1);
        goal.unitCountMap().put(Units.ZERG_HATCHERY.getUnitTypeId(), 2);
        // goal.upgradesMap().add(Upgrades.BURROW.getUpgradeId());


        System.out.println(goal.isSatisfied(init));

        EbRequirementTree requirementTree = new EbRequirementTree(data, goal);

        System.out.println(requirementTree);

        Random random = new XORShiftRandom();
        GeneticAlgorithm ga = new GeneticAlgorithm().geneFunction(set -> new Gene().data(requirementTree.actionSet().get(random.nextInt(requirementTree.actionSet().size()))));

        EbFitness fitness = new EbStandardFitness().tree(data).goal(goal).initial(init);
        // EbFitness fitness = new EbTimeFitness().goal(goal).initial(init);

        ga.fitnessFunction(fitness::simulateOrder);
        ga.maxGenerations(5000);
        ga.sameSolution(64);
        ga.geneLength(32);
        Individual individual = ga.runAlgorithm(64);

        EngineeringBay.DEBUG = true;

        System.out.println(individual);
        var order = fitness.simulateOrder(individual.genes(), false);
        System.out.println(order);
        System.out.println(order.currentFrame() + " / " + fitness.maxTime());
        System.out.println(order.currentFrame() / 22.4);
        System.out.println(order.minerals());
        System.out.println(order.gas());
        System.out.println(order.supply());
        System.out.println(goal.isSatisfied(order));

        for(var action: order.validActions()) {
            System.out.println(action.name());
        }*/
    }
}
