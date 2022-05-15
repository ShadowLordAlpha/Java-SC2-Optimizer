package com.shadowcs.optimizer.build;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.shadowcs.optimizer.build.genetics.BuildOrderGenetics;
import com.shadowcs.optimizer.genetics.Chromosome;
import com.shadowcs.optimizer.genetics.GeneticAlgorithm;
import com.shadowcs.optimizer.pojo.Pair;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import lombok.Data;

import java.util.List;

@Data
public class BuildOrder {

    private List<BuildOrderGene> orders;

    private BuildOrder(Chromosome<BuildOrderGene> solution) {

        try {
            int orders = Integer.parseInt(solution.extra());

            this.orders = solution.geneList().subList(0, orders);
        } catch (Exception e) {

        }
    }

    public static BuildOrder findFastestBuildOrder(BuildState state, Pair<UnitType, Integer>...output) {

        // TODO: validate that the state does not already satisfy the output...

        BuildOrderGenetics bog = new BuildOrderGenetics(state, S2DataUtil.loadUnitData(), S2DataUtil.loadUpgradeData(), S2DataUtil.loadAbilityData());
        BuildOrderFitness bof = new BuildOrderFitness(state, S2DataUtil.loadUnitData(), output);

        GeneticAlgorithm<BuildOrderGene> algo = new GeneticAlgorithm<>();
        algo.genetics(bog);
        algo.fitness(bof);
        algo.maxGenerations(100000);
        algo.sameSolution(100);

        Chromosome<BuildOrderGene> solution = algo.runAlgorithm(100, 64);

        return new BuildOrder(solution);
    }
}
