package com.shadowcs.optimizer;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.game.Race;
import com.google.gson.Gson;
import com.shadowcs.optimizer.engibay.EbState;
import com.shadowcs.optimizer.engibay.build.EbAction;
import com.shadowcs.optimizer.engibay.build.macro.EbMineResourceAction;
import com.shadowcs.optimizer.engibay.fitness.EbFitness;
import com.shadowcs.optimizer.engibay.fitness.EbStandardFitness;
import com.shadowcs.optimizer.random.XORShiftRandom;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;

import static io.jenetics.engine.Limits.byExecutionTime;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class Test {

    public static void main(String...args) {

        TechTree data = S2DataUtil.loadData();

        //var genes = S2DataUtil.generateGenes(data, Race.TERRAN, Race.ZERG, Race.PROTOSS);
        var actions = S2DataUtil.generateActions(data, Race.ZERG);

        //actions.add(new EbNoopAction()); // As we have a cutoff already, we should be able to ignore this for the most part
        //actions.add(new EbMineResourceAction(false));// FIXME: these two resource actions are breaking things...
        //actions.add(new EbMineResourceAction(true));



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
        goal.upgradeSet().add(Upgrades.ZERGLING_MOVEMENT_SPEED.getUpgradeId());

        var actionList = S2DataUtil.filterActions(actions, init, goal);

        // We use the Object here to assist us with drastically reducing the search space we need

        Random random = new XORShiftRandom();
        // EbFitness fitness = new EbTimeFitness().goal(goal).initial(init);
        EbFitness fitness = new EbStandardFitness().goal(goal).initial(init);

        Factory<Genotype<AnyGene<EbAction>>> gtf = Genotype.of(AnyChromosome.of(() -> actionList.get(random.nextInt(actionList.size())), 128));

        Engine<AnyGene<EbAction>, Double> engine = Engine
                .builder(
                        fitness::simulateOrderGt,
                        gtf
                )
                .populationSize(100)
                .survivorsSelector(new EliteSelector<>())
                .offspringSelector(new RouletteWheelSelector<>())
                .optimize(fitness.optimize())
                .alterers(new Mutator<>(0.4), new UniformCrossover<>(0.5))
                .build();

        Genotype<AnyGene<EbAction>> result = engine.stream()
                .limit(bySteadyFitness(1000))
                .limit(byExecutionTime(Duration.ofMinutes(15)))
                .collect(EvolutionResult.toBestGenotype());

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("Best solution: " + result);
        System.out.println("Best solution: " + new Gson().toJson(result.gene()));
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
        System.out.println("Sup use: " + order.supplyUsed());
        System.out.println("Sup ava: " + order.supplyAvailable());
        System.out.println("Sup cap: " + order.supplyCap());
        System.out.println("bad: " + order.badGenes());
        System.out.println("goo: " + order.validActions().size());
        System.out.println(goal.isSatisfied(order));

        for(var action: order.validActions()) {
            System.out.println(action.name());
        }

        // test();
    }

    public static void test() {
        Graph<String, DefaultEdge> gameGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

        gameGraph.addVertex("SCV");
        gameGraph.addVertex("SupplyDepot");
        gameGraph.addVertex("Barracks");
        gameGraph.addVertex("Factory");
        gameGraph.addVertex("Starport");
        gameGraph.addVertex("FusionCore");
        gameGraph.addVertex("Battlecruiser");

        gameGraph.addEdge("SCV", "SupplyDepot");
        gameGraph.addEdge("SupplyDepot", "Barracks");
        gameGraph.addEdge("Barracks", "Factory");
        gameGraph.addEdge("Factory", "Starport");
        gameGraph.addEdge("Starport", "Battlecruiser");
        gameGraph.addEdge("Barracks", "FusionCore");
        gameGraph.addEdge("FusionCore", "Battlecruiser");

        DijkstraShortestPath<String, DefaultEdge> dijkstraShortestPath = new DijkstraShortestPath<>(gameGraph);
        System.out.println("Shortest path from SCV to Battlecruiser: " + dijkstraShortestPath.getPath("SCV", "Battlecruiser"));
    }
}
