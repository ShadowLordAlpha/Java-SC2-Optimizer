package com.shadowcs.optimizer.genetics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class Chromosome<E> {

    private transient double fitness = Double.NaN;
    private final List<E> geneList;
    private String extra;

    public Chromosome(int size) {
        geneList = new ArrayList<>(size); // Dumb...
        for(int i = 0; i < size; i++) {
            geneList.add(null);
        }
    }

    public Chromosome(int size, Genetics<E> genetics) {
        this(size);
        genetics.validate(this);
    }

    public Chromosome(Chromosome<E> eChromosome) {
        geneList = new ArrayList<>(eChromosome.geneList());
        fitness = eChromosome.fitness();
        extra = eChromosome.extra();
    }

    public void mutate(int i, Genetics<E> genetics) {
        List<E> validGenes = new ArrayList<>(genetics.available(this, i));
        geneList().set(i, validGenes.get(ThreadLocalRandom.current().nextInt(validGenes.size())));
    }

    public void crossover(Chromosome<E> indiv1, Chromosome<E> indiv2, double rate) {
        for(int i = 0; i < geneList.size(); i++) {
            if (ThreadLocalRandom.current().nextFloat() < rate) {
                geneList().set(i, indiv1.geneList().get(i));
            } else {
                geneList().set(i, indiv2.geneList().get(i));
            }
        }
    }

    public void crossover(Chromosome<E> indiv1, Chromosome<E> indiv2, int i1) {
        for(int i = 0; i < geneList.size(); i++) {
            if (i < i1) {
                geneList().set(i, indiv1.geneList().get(i));
            } else {
                geneList().set(i, indiv2.geneList().get(i));
            }
        }
    }

    public void crossover(Chromosome<E> indiv1, Chromosome<E> indiv2, int i1, int i2) {
        for(int i = 0; i < geneList.size(); i++) {
            if (i < i1 && i <= i2) {
                geneList().set(i, indiv1.geneList().get(i));
            } else {
                geneList().set(i, indiv2.geneList().get(i));
            }
        }
    }
}
