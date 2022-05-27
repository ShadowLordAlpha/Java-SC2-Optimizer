package com.shadowcs.optimizer.build;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.shadowcs.optimizer.build.genetics.BuildOrderFitness;
import com.shadowcs.optimizer.build.genetics.BuildOrderGene;
import com.shadowcs.optimizer.build.genetics.BuildOrderGenetics;
import com.shadowcs.optimizer.build.state.BuildState;
import com.shadowcs.optimizer.genetics.Chromosome;
import com.shadowcs.optimizer.genetics.GeneticAlgorithm;
import com.shadowcs.optimizer.pojo.Pair;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        var units = S2DataUtil.loadUnitData();

        Map<Integer, UnitS2Data> unitS2DataMap = new HashMap<>();
        unitS2DataMap.putAll(units.stream().collect(Collectors.toMap(UnitS2Data::id, x -> x)));

        Map<Integer, UnitS2Data> abilityToUnitS2DataMap = new HashMap<>();
        abilityToUnitS2DataMap.putAll(units.stream().collect(Collectors.toMap(UnitS2Data::buildAbility, x -> x)));

        BuildOrderGenetics bog = new BuildOrderGenetics(state, unitS2DataMap, abilityToUnitS2DataMap, S2DataUtil.loadUpgradeData(), S2DataUtil.loadAbilityData());
        BuildOrderFitness bof = new BuildOrderFitness(state, unitS2DataMap, abilityToUnitS2DataMap, output);

        GeneticAlgorithm<BuildOrderGene> algo = new GeneticAlgorithm<>();
        algo.genetics(bog);
        algo.fitness(bof);
        algo.maxGenerations(100000);
        algo.sameSolution(100);

        Chromosome<BuildOrderGene> solution = algo.runAlgorithm(100, 64);

        return new BuildOrder(solution);
    }
}
