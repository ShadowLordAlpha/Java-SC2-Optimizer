package com.shadowcs.optimizer.genetics;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The {@link Genetics} class handles {@link #available(Chromosome, int) generation} and
 * {@link #validate(Chromosome) validation} of the different "genes" or actions that we are able to take.
 *
 * @param <E> What kind of Gene are we generating or validating
 */
@FunctionalInterface
public interface Genetics<E> {

    /**
     * Check what Genes are available at the given index.
     *
     * @param chromo The Chromosome we want to check
     * @param index What gene are we trying to get a valid set for
     *
     * @return A set of genes that are valid for this chromosome at this position
     */
    Set<E> available(Chromosome<E> chromo, int index);

    /**
     * Validate that the given chromosome is composed of all valid genes at their given positions. If not this method
     * will correct the invalid genes and replace them with valid ones.
     *
     * @param chromo The Chromosome we want to check
     */
    default void validate(Chromosome<E> chromo) {
        for(int i = 0; i < chromo.geneList().size(); i++) {
            Set<E> validGenes = available(chromo, i);
            // If the gene is not valid we have to replace it with one that is
            var gene = chromo.geneList().get(i);
            if(gene == null || !validGenes.contains(gene)) {
                chromo.geneList().set(i, validGenes.stream().skip(ThreadLocalRandom.current().nextInt(validGenes.size())).findFirst().orElse(null));
            }
        }
    }
}
