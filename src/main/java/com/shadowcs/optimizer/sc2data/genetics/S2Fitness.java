package com.shadowcs.optimizer.sc2data.genetics;

import com.shadowcs.optimizer.genetics.Gene;
import lombok.Data;

import java.util.Arrays;
import java.util.function.Function;

@Data
public class S2Fitness implements Function<Gene[], Double> {

    S2GameState gameState;

    @Override
    public Double apply(Gene[] genes) {

        var currState = gameState.clone();

        for(var gene: genes) {
            currState.processAction(gene.data());
        }



        return 0.0;
    }
}
