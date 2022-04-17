package com.shadowcs.optimizer.genetics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class Chromosome<E> {

    private double fitness = Double.NaN;
    private final List<E> geneList;

    public Chromosome(int size) {
        geneList = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            geneList.add(null);
        }
    }

    public Chromosome(int size, Genetics genetics) {
        geneList = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            List<E> validGenes = genetics.available(this, i);
            geneList().add(validGenes.get(ThreadLocalRandom.current().nextInt(validGenes.size())));
        }
    }

    public void mutate(int i, Genetics<E> genetics) {
        List<E> validGenes = genetics.available(this, i);
        geneList().set(i, validGenes.get(ThreadLocalRandom.current().nextInt(validGenes.size())));
    }

    public void validate(Genetics<E> genetics) {
        for(int i = 0; i < geneList.size(); i++) {
            List<E> validGenes = genetics.available(this, i);
            // If the gene is not valid we have to replace it with one that is
            if(!validGenes.contains(geneList.get(i))) {
                geneList().add(validGenes.get(ThreadLocalRandom.current().nextInt(validGenes.size())));
            }
        }
    }

    public void crossover(Chromosome<E> indiv1, Chromosome<E> indiv2, double rate) {
        for(int i = 0; i < geneList.size(); i++) {
            if(ThreadLocalRandom.current().nextFloat() < rate) {
                geneList().set(i, indiv1.geneList().get(i));
            } else {
                geneList().set(i, indiv2.geneList().get(i));
            }
        }
    }

    public void crossover(Chromosome<E> indiv1, Chromosome<E> indiv2, int i1) {
        for(int i = 0; i < geneList.size(); i++) {
            if(i < i1) {
                geneList().set(i, indiv1.geneList().get(i));
            } else {
                geneList().set(i, indiv2.geneList().get(i));
            }
        }
    }

    public void crossover(Chromosome<E> indiv1, Chromosome<E> indiv2, int i1, int i2) {
        for(int i = 0; i < geneList.size(); i++) {
            if(i < i1 && i <= i2) {
                geneList().set(i, indiv1.geneList().get(i));
            } else {
                geneList().set(i, indiv2.geneList().get(i));
            }
        }
    }
}
