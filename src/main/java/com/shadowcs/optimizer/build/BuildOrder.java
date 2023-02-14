package com.shadowcs.optimizer.build;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.game.Race;
import com.shadowcs.optimizer.build.genetics.BuildOrderFitness;
import com.shadowcs.optimizer.build.genetics.BuildOrderGene;
import com.shadowcs.optimizer.build.genetics.BuildOrderGenetics;
import com.shadowcs.optimizer.build.state.BuildState;
import com.shadowcs.optimizer.genetics.GeneticAlgorithm;
import com.shadowcs.optimizer.genetics.Individual;
import com.shadowcs.optimizer.pojo.Pair;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class BuildOrder {

    private List<BuildOrderGene> orders;

    private BuildOrder(Individual solution) {

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

        var ability = S2DataUtil.loadAbilityData();
        Map<Integer, AbilityS2Data> abilityS2DataMap = new HashMap<>();
        abilityS2DataMap.putAll(ability.stream().collect(Collectors.toMap(AbilityS2Data::id, x -> x)));

        BuildOrderGenetics bog = new BuildOrderGenetics(units, S2DataUtil.loadUpgradeData(), abilityS2DataMap, abilityToUnitS2DataMap, Race.ZERG);
        BuildOrderFitness bof = new BuildOrderFitness(state, unitS2DataMap, output);

        GeneticAlgorithm algo = new GeneticAlgorithm();

        algo.geneFunction(bog);
        algo.fitnessFunction(bof);

        algo.maxGenerations(5000);
        algo.sameSolution(200);
        algo.geneLength(64);

        Individual solution = algo.runAlgorithm(100);

        return new BuildOrder(solution);
    }
}
