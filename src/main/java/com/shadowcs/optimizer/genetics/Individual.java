package com.shadowcs.optimizer.genetics;

import com.shadowcs.optimizer.random.XORShiftRandom;
import lombok.Data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents a single individual that is a collection of Genes in a population of candidates for a genetic algorithm.
 */
@Data
public class Individual {

    private Random random = new XORShiftRandom();
    private Function<Gene[], Double> fitnessFunction;
    private Function<Set<Gene>, Gene> geneFunction;

    private final Gene[] genes;
    // Cache
    private double fitness = 0;



    /**
     * Constructs a new individual with a random set of genes.
     */
    public Individual(int geneLength, Function<Set<Gene>, Gene> geneFunction, Function<Gene[], Double> fitnessFunction) {
        this.geneFunction = geneFunction;
        this.fitnessFunction = fitnessFunction;

        this.genes = new Gene[geneLength];

        // Generate individual
        // generateIndividual();
    }

    /**
     * Generates a random set of genes for this individual.
     */
    public Individual generateIndividual() {

        // Generate the hash set to be the full length at the start as it will at most be that long
        Set<Gene> geneSet = new HashSet<>(genes.length);
        // TODO: keep a count of the genes triggered as we may need to remove genes over time if a future gene consumes the older one

        for (int i = 0; i < size(); i++) {
            // Generate a random gene (implementation will depend on the specific type of T)
            Gene gene = geneFunction().apply(geneSet);
            setGene(i, gene);

            geneSet.add(gene);
        }

        return this;
    }

    public Individual mutate(double rate) {

        // Generate the hash set to be the full length at the start as it will at most be that long
        Set<Gene> geneSet = new HashSet<>(genes.length);
        // TODO: keep a count of the genes triggered as we may need to remove genes over time if a future gene consumes the older one

        // Loop through genes
        for (int i = 0; i < size(); i++) {

            Gene current = getGene(i);
            if (random.nextDouble() <= rate || !current.valid(geneSet)) {
                // Get a random gene, note that we will need to validate all genes after a mutated one
                Gene gene = geneFunction().apply(geneSet);
                setGene(i, gene);
            }

            geneSet.add(current);
        }

        return this;
    }

    // Applies crossover to a set of parents and creates offspring
    public Individual crossover(Individual parent1, Individual parent2, double rate) {

        // Generate the hash set to be the full length at the start as it will at most be that long
        Set<Gene> geneSet = new HashSet<>(genes.length);
        // TODO: keep a count of the genes triggered as we may need to remove genes over time if a future gene consumes the older one

        // Loop through genes
        for (int i = 0; i < genes.length; i++) {
            // Crossover
            Gene gene = null;

            if(Math.min(parent1.size(), parent2.size()) > i) {
                if (random.nextDouble() <= rate) {
                    gene = parent1.getGene(i);
                } else {
                    gene = parent2.getGene(i);
                }
            }

            // Validate the gene at this position
            if(gene == null || !gene.valid(geneSet)) {
                // Gene is not valid, get a random one that is
                gene = geneFunction().apply(geneSet);
            }

            // Set the gene
            setGene(i, gene);

            // Add to the current set
            geneSet.add(gene);
        }

        return this;
    }

    /**
     * Returns the gene at the specified index in this individual's gene array.
     *
     * @param index the index of the gene to return
     * @return the gene at the specified index
     */
    public Gene getGene(int index) {
        return genes[index];
    }

    /**
     * Sets the gene at the specified index in this individual's gene array.
     *
     * @param index the index of the gene to set
     * @param value the new value for the gene
     */
    public void setGene(int index, Gene value) {
        genes[index] = value;
        fitness = 0;
    }

    /**
     * Returns the length of this individual's gene array.
     *
     * @return the length of the gene array
     */
    public int size() {
        return genes.length;
    }

    /**
     * Returns the fitness of this individual, as calculated by some fitness function.
     *
     * @return the fitness of this individual
     */
    public double getFitness() {
        if (fitness == 0) {
            fitness = fitnessFunction.apply(genes);
        }
        return fitness;
    }

    @Override
    public String toString() {
        return Arrays.toString(genes);
    }
}
