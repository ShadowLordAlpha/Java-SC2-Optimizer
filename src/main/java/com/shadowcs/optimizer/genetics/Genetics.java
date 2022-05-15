package com.shadowcs.optimizer.genetics;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@FunctionalInterface
public interface Genetics<E> {

    // TODO: see if this can be a set instead of a list, might make access faster. Not really as we need to be able to randomlly access
    //  and we can't do that with a set
    Set<E> available(Chromosome<E> chromo, int index);

    default void validate(Chromosome<E> chromo) {
        for(int i = 0; i < chromo.geneList().size(); i++) {
            Set<E> validGenes = available(chromo, i);
            // If the gene is not valid we have to replace it with one that is
            if(!validGenes.contains(chromo.geneList().get(i))) {
                chromo.geneList().add(new ArrayList<>(validGenes).get(ThreadLocalRandom.current().nextInt(validGenes.size())));
            }
        }
    }
}
