package com.shadowcs.optimizer.build;

import com.shadowcs.optimizer.genetics.Chromosome;
import com.shadowcs.optimizer.genetics.Fitness;

public class BuildOrderFitness implements Fitness<BuildOrderGene> {

    @Override
    public double calculate(Chromosome<BuildOrderGene> chromo) {
        return 0;
    }
}
