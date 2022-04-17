package com.shadowcs.optimizer.genetics;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class GeneticAlgorithm<E> {

    /**
     * The equation we will use to evaluate each possible solution to see if it matches or is what we are looking for
     */
    private Fitness<E> fitness;

    /**
     * Describes the Genes that we are able to use at that point in time or are usable in the future of the gene
     */
    private Genetics<E> genetics;

    /**
     * What fitness should we stop at and say we are good enough if we are at or above, 0 to disable
     */
    private double solutionFitness;

    /**
     * How many generations should pass before we return the best one we have found, 0 to disable
     */
    private int maxGenerations;

    /**
     * How many times should we allow the same solution before we take it as the best match we are going to get
     */
    private int sameSolution;


    private double uniformRate = 0.5;
    private double mutationRate = 0.028;
    private int tournamentSize = 5;
    private int elitism = 1;

    private boolean shouldContinue(double fitness, int generation, int same) {
        if(solutionFitness > 0 && fitness >= solutionFitness) {
            return false;
        }

        if(sameSolution > 0 && same >= sameSolution) {
            return false;
        }

        return maxGenerations <= 0 || generation < maxGenerations;
    }

    public Chromosome<E> runAlgorithm(int populationSize, int genes) {

        // Generate initial population
        List<Chromosome<E>> population = new ArrayList<>(populationSize);

        for(int i = 0; i < populationSize; i++) {
            population.add(new Chromosome<>(genes, genetics()));
        }

        Chromosome<E> mostFit = null;
        double fitness = Double.NEGATIVE_INFINITY;
        int generation = 0;
        int same = 0;
        do {
            if(generation > 0) {
                population = evolvePopulation(population);
            }

            same++;
            generation++;

            // calculate fitness for each Chromosome in a population
            population.parallelStream().forEach(chromo -> chromo.fitness(fitness().calculate(chromo)));
            population.sort(Comparator.comparingDouble(Chromosome::fitness));
            Collections.reverse(population);

            if(fitness < population.get(0).fitness()) {
                fitness = population.get(0).fitness();
                if (!population.get(0).equals(mostFit)) {
                    mostFit = population.get(0);
                    same = 0;
                }
            }

        } while(shouldContinue(fitness, generation, same));

        System.out.println("Algorithm Finished on Generation: " + generation);

        return mostFit;
    }

    private List<Chromosome<E>> evolvePopulation(List<Chromosome<E>> population) {

        List<Chromosome<E>> duplicate = new ArrayList<>(population);
        List<Chromosome<E>> newPopulation = new ArrayList<>(population.size());

        int size = population.size() - elitism;
        /*double totalPoints = 0;
        for(var pop: population) {
            totalPoints += pop.fitness();
        }*/


        for(int i = 0; i < elitism; i++) {
            newPopulation.add(population.get(i));
        }

        // Duplicate so we can mutate them
        newPopulation.addAll(newPopulation);

        // This actually helps a lot with tournament selection
        Collections.shuffle(duplicate);

        // TODO: see if we can do this parallel
        for (int i = elitism * 2; i < size; i++) {

            float value = ThreadLocalRandom.current().nextFloat();
            if (value <= 0.5) {
                // Fitest in subset
                Chromosome<E> indiv1 = tournamentSelection(duplicate);
                Chromosome<E> indiv2 = tournamentSelection(duplicate);
                Chromosome<E> newIndiv = crossover(indiv1, indiv2);
                newPopulation.add(newIndiv);
            } else if(value <= 0.90) {
                // random
                Chromosome<E> indiv1 = randomSelection(duplicate);
                Chromosome<E> indiv2 = randomSelection(duplicate);
                Chromosome<E> newIndiv = crossover(indiv1, indiv2);
                newPopulation.add(newIndiv);
            } else {
                // Cross two most fit
                Chromosome<E> indiv1 = population.get(ThreadLocalRandom.current().nextInt(elitism, elitism * 2));
                Chromosome<E> indiv2 = population.get(ThreadLocalRandom.current().nextInt(elitism, elitism * 2));
                Chromosome<E> newIndiv = crossover(indiv1, indiv2);
                newPopulation.add(newIndiv);
            }
        }

        // This should leave us elitism number for new random generations

        newPopulation.parallelStream().skip(elitism).forEach(this::mutate);

        newPopulation.parallelStream().skip(elitism).forEach(chrom -> chrom.validate(genetics()));

        return newPopulation;
    }

    private Chromosome<E> crossover(Chromosome<E> indiv1, Chromosome<E> indiv2) {
        Chromosome<E> newSol = new Chromosome<>(indiv1.geneList().size());

        float chanc = ThreadLocalRandom.current().nextFloat();

        if(chanc < 0.33) {
            int i1 = ThreadLocalRandom.current().nextInt(indiv1.geneList().size());
            int i2 = ThreadLocalRandom.current().nextInt(indiv1.geneList().size());

            if (i1 > i2) {
                int t = i1;
                i1 = i2;
                i2 = t;
            }

            // We use two pass crossover instead of randomly picking from one or the other
            newSol.crossover(indiv1, indiv2, i1, i2);
        } else if(chanc < 0.66) {
            int i1 = ThreadLocalRandom.current().nextInt(indiv1.geneList().size());
            newSol.crossover(indiv1, indiv2, i1);
        } else {
            newSol.crossover(indiv1, indiv2, uniformRate);
        }

        // TODO: add randomly picking corossover as well

        return newSol;
    }

    private void mutate(Chromosome<E> indiv) {
        for (int i = 0; i < indiv.geneList().size(); i++) {
            if (ThreadLocalRandom.current().nextFloat() <= mutationRate) {
                indiv.mutate(i, genetics());
            }
        }
    }

    private Chromosome<E> tournamentSelection(List<Chromosome<E>> pop) {
        List<Chromosome<E>> tournament = new ArrayList<>(tournamentSize);
        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(i, pop.get(ThreadLocalRandom.current().nextInt(pop.size())));
        }

        tournament.sort(Comparator.comparingDouble(Chromosome::fitness));
        Collections.reverse(tournament);

        return tournament.get(0);
    }

    private Chromosome<E> randomSelection(List<Chromosome<E>> pop) {

        return pop.get(ThreadLocalRandom.current().nextInt(pop.size()));
    }
}
