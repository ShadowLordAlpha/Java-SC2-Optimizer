package com.shadowcs.optimizer;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.shadowcs.optimizer.genetics.GeneticAlgorithm;
import com.shadowcs.optimizer.random.XORShiftRandom;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import com.shadowcs.optimizer.sc2data.genetics.S2BaseInfo;
import com.shadowcs.optimizer.sc2data.genetics.S2Fitness;
import com.shadowcs.optimizer.sc2data.genetics.S2GameState;
import com.shadowcs.optimizer.sc2data.models.TechTree;

import java.util.Random;

public class Test {

    public static void main(String...args) {
        TechTree data = S2DataUtil.loadData();

        var genes = S2DataUtil.generateGenes(data, Race.TERRAN);

        Random random = new XORShiftRandom();
        GeneticAlgorithm ga = new GeneticAlgorithm().geneFunction(set -> genes.get(random.nextInt(genes.size())));

        var state = new S2GameState().minerals(50).supply(3).totalSupply(12);
        for(int i = 0; i < 8; i++) {
            state.baseInfo().add(new S2BaseInfo());
        }
        state.unitsIdle().put(Units.TERRAN_COMMAND_CENTER.getUnitTypeId(), 1);
        state.unitsIdle().put(Units.TERRAN_SCV.getUnitTypeId(), 12);
        state.techTree(data);

        ga.fitnessFunction(new S2Fitness().gameState(state));

        ga.geneLength(128);
        ga.maxGenerations(5000);
        ga.sameSolution(32);

        data.ability().forEach(u -> {
            //System.out.println("Unit: (" + u.name() + ")" + u.target().getClass().getSimpleName());
        });

        //ga.runAlgorithm(128);

        for(var pri: state.DFBB(Units.TERRAN_SCV.getUnitTypeId(), Units.TERRAN_STARPORT.getUnitTypeId(), 0)) {
            System.out.println(data.abilityMap().get(pri.action()).name());
        }
    }
}
