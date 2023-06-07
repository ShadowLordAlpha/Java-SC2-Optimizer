package com.shadowcs.optimizer.genetics;

import com.shadowcs.optimizer.random.XORShiftRandom;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import org.junit.jupiter.api.Test;

import java.util.Random;


public class SimpleGATest {

    @Test
    public void loadResTest() {

        TechTree data = S2DataUtil.loadData();

        data.unit().forEach(u -> {
            System.out.println("Unit: (" + u.name() + ")");
        });

        // Start with what units
        /*BuildState state = new BuildState(
                new Pair<>(Units.TERRAN_COMMAND_CENTER, 1),
                new Pair<>(Units.TERRAN_SCV, 12)
        );

        // What resources do we have
        state.resources().minerals(50);
        state.resources().vespene(0);

        // Find the build order
        BuildOrder buildOrder = BuildOrder.findFastestBuildOrder(state);

        System.out.println(S2DataUtil.readResDataFile());*/
    }

    @Test
    public void basicIntegerGATest() {

        String solution = "10110010111100100100110110001101";
        Random random = new XORShiftRandom();

        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm();
        geneticAlgorithm.fitnessFunction(chromo -> {

            double score = 0;
            for(int i = 0; i < chromo.length; i++) {
                String data = chromo[i].data();
                if(data.equalsIgnoreCase(String.valueOf(solution.charAt(i)))) {
                    score++;
                }
            }

            return score;
        });

        Gene gene1 = new Gene().data("1");
        Gene gene2 = new Gene().data("0");

        geneticAlgorithm.geneFunction(set -> {
            if(random.nextDouble() > 0.5) {
                return gene1;
            } else {
                return gene2;
            }
        });

        geneticAlgorithm.geneLength(32);
        geneticAlgorithm.solutionFitness(32);
        geneticAlgorithm.maxGenerations(5000);
        geneticAlgorithm.sameSolution(0);

        var gasolution = geneticAlgorithm.runAlgorithm(64);

        StringBuilder builder = new StringBuilder();
        for(var gene: gasolution.genes()) {
            builder.append((String) gene.data());
        }
        System.out.println("Requested Solu: " + solution);
        System.out.println("Found Solution: " + builder);

        assert solution.equalsIgnoreCase(builder.toString());
    }
}
