package com.shadowcs.optimizer.genetics;

import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.build.BuildOrder;
import com.shadowcs.optimizer.build.BuildState;
import com.shadowcs.optimizer.pojo.Pair;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import org.junit.jupiter.api.Test;

import java.util.List;


public class SimpleGATest {

    @Test
    public void loadResTest() {

        // Start with what units
        BuildState state = new BuildState(
                new Pair<>(Units.TERRAN_COMMAND_CENTER, 1),
                new Pair<>(Units.TERRAN_SCV, 12)
        );

        // What resources do we have
        state.resources().minerals(50);
        state.resources().vespene(0);

        // Find the build order
        BuildOrder buildOrder = BuildOrder.findFastestBuildOrder(state);

        System.out.println(S2DataUtil.readResDataFile());
    }

    @Test
    public void basicIntegerGATest() {

        String solution = "10110010111100100100110110001101";
        GeneticAlgorithm<String> ga = new GeneticAlgorithm<>();
        ga.fitness(chromo -> {
            if(!Double.isNaN(chromo.fitness())) {
                return chromo.fitness();
            }

            float score = 0;
            for(int i = 0; i < chromo.geneList().size(); i++) {
                if(chromo.geneList().get(i).equalsIgnoreCase(String.valueOf(solution.charAt(i)))) {
                    score++;
                }
            }

            return score;
        });
        ga.genetics((gen, idx) -> List.of("0", "1"));

        ga.solutionFitness(32);
        //ga.maxGenerations(200000);
        ga.sameSolution(2000);

        Chromosome<String> gasolution = ga.runAlgorithm(64, 32);

        StringBuilder builder = new StringBuilder();
        for(var gene: gasolution.geneList()) {
            builder.append(gene);
        }
        System.out.println("Requested Solu: " + solution);
        System.out.println("Found Solution: " + builder.toString());

        assert solution.equalsIgnoreCase(builder.toString());
    }
}
