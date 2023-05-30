package com.shadowcs.optimizer;

import com.github.ocraft.s2client.protocol.game.Race;
import com.shadowcs.optimizer.genetics.GeneticAlgorithm;
import com.shadowcs.optimizer.random.XORShiftRandom;
import com.shadowcs.optimizer.sc2data.S2DataUtil;
import com.shadowcs.optimizer.sc2data.models.TechTree;

import java.util.Random;

public class Test {

    public static void main(String...args) {
        TechTree data = S2DataUtil.loadData();

        var genes = S2DataUtil.generateGenes(data, Race.ZERG);

        Random random = new XORShiftRandom();
        GeneticAlgorithm ga = new GeneticAlgorithm().geneFunction(set -> genes.get(random.nextInt(genes.size())));

        ga.fitnessFunction();

        ga.geneLength(128);
        ga.maxGenerations(5000);
        ga.sameSolution(32);

        System.out.println(data.ability().size());
        System.out.println(data.unit().size());
        System.out.println(data.upgrade().size());
        data.ability().forEach(u -> {
            //System.out.println("Unit: (" + u.name() + ")" + u.target().getClass().getSimpleName());
        });
    }
}
