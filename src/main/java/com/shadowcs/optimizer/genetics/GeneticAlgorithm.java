package com.shadowcs.optimizer.genetics;

import com.shadowcs.optimizer.random.XORShiftRandom;
import lombok.Data;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;

@Data
public class GeneticAlgorithm {

    private Random random = new XORShiftRandom();
    private static final double MUTATION_RATE = 0.075;
    private static final int TOURNAMENT_SIZE = 10;
    private static final boolean ELITISM = true;

    private boolean NEW_BLOOD = true;

    private int geneLength = 64;
    private Function<Gene[], Double> fitnessFunction;
    private Function<Set<Gene>, Gene> geneFunction;

    private double solutionFitness = 0;
    private int maxGenerations = 0;
    private int sameSolution = 100;

    public GeneticAlgorithm() {

    }

    private Population createPopulation(int size, boolean initialize) {
        return new Population(size, initialize, this::createIndividual);
    }

    private Individual createIndividual() {
        return new Individual(geneLength, geneFunction(), fitnessFunction());
    }

    // Selects candidate tourneySize individuals from the population
    // and returns the fittest
    private Individual tournamentSelection(Population pop) {
        // Create a tournament population
        Population tournament = createPopulation(TOURNAMENT_SIZE, false);
        // For each place in the tournament get a random individual
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            tournament.saveIndividual(i, pop.getIndividual(random.nextInt(pop.size())));
        }

        // Get the fittest
        return tournament.getFittest();
    }

    public Individual runAlgorithm(int popSize) {
        Population pop = createPopulation(popSize, true);

        Individual fittest = null;
        boolean findSol = true;
        int generation = 0;
        int sameSol = 0;

        while(findSol) {
            generation++;
            pop = evolvePopulation(pop);

            var tempfit = pop.getFittest();
            if(fittest != tempfit) {
                fittest = tempfit;
                sameSol = 0;
            } else {
                sameSol++;
            }

            if(solutionFitness > 0 && fittest.fitness() >= solutionFitness) {
                System.out.println("break 3: " + fittest.fitness());
                findSol = false;
            }

            if(maxGenerations > 0 && generation > maxGenerations) {
                System.out.println("break 2: " + generation);
                findSol = false;
            }

            if(sameSolution > 0 && sameSol > sameSolution) {
                System.out.println("break 1: " + sameSol);
                findSol = false;
            }

            System.out.println("Progress: g" + generation + " f" + fittest.fitness() + " s" + sameSol);
        }

        return fittest;
    }

    // Evolves a population over one generation
    public Population evolvePopulation(Population pop) {
        Population nextPopulation = createPopulation(pop.size(), false);

        // Keep our best individuals if we are doing Elitism
        // TODO: we may want to save more than just one
        int elitismOffset = 0;
        if(ELITISM) {
            var gitt = pop.getFittest();
            nextPopulation.saveIndividual(elitismOffset, gitt);
            elitismOffset += 1;

            // Mutation of the top one, semi more likely to be more correct in some cases
            Individual individual = createIndividual();
            for(int i = 0; i < gitt.size(); i++) {
                individual.setGene(i, gitt.getGene(i));
            }
            nextPopulation.saveIndividual(elitismOffset, individual.mutate(MUTATION_RATE));
            elitismOffset += 1;
        }

        // New blood for the new set
        if(NEW_BLOOD) {
            nextPopulation.saveIndividual(elitismOffset, createIndividual().generateIndividual());
            elitismOffset += 1;
        }

        // Crossover population
        // Loop over the new population's size and create individuals from
        // Current population
        for (int i = elitismOffset; i < nextPopulation.size(); i++) {
            // Select parents
            Individual parent1 = tournamentSelection(pop);
            Individual parent2 = tournamentSelection(pop);
            // Crossover parents
            Individual child = createIndividual().crossover(parent1, parent2, 0.5);
            // Add child to new population
            nextPopulation.saveIndividual(i, child);
        }

        // Mutate the new population a bit to add some new genetic material
        for (int i = elitismOffset; i < nextPopulation.size(); i++) {
            nextPopulation.getIndividual(i).mutate(MUTATION_RATE);
        }

        return nextPopulation;
    }
}

