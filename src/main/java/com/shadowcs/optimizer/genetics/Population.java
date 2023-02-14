package com.shadowcs.optimizer.genetics;

import com.shadowcs.optimizer.random.XORShiftRandom;

import java.util.Random;
import java.util.function.Supplier;

public class Population {

    private Individual[] individuals;

    // Constructors
    public Population(int populationSize, boolean initialise, Supplier<Individual> individualSupplier) {
        individuals = new Individual[populationSize];
        // If we need to initialise a population of individuals
        if (initialise) {
            // Loop and create individuals
            for (int i = 0; i < size(); i++) {
                Individual newIndividual = individualSupplier.get();
                newIndividual.generateIndividual();
                saveIndividual(i, newIndividual);
            }
        }
    }

    // Saves an individual
    public void saveIndividual(int index, Individual indiv) {
        individuals[index] = indiv;
    }

    // Gets an individual from the population
    public Individual getIndividual(int index) {
        return individuals[index];
    }

    // Gets the fittest individual in the population
    public Individual getFittest() {
        Individual fittest = individuals[0];
        // Loop through individuals to find fittest
        for (int i = 0; i < size(); i++) {
            if (fittest.getFitness() <= getIndividual(i).getFitness()) {
                fittest = getIndividual(i);
            }
        }
        return fittest;
    }

    // Gets population size
    public int size() {
        return individuals.length;
    }

    // Shuffles the population in-place
    public void shuffle() {
        Random rnd = new XORShiftRandom();
        for (int i = individuals.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Individual a = individuals[index];
            individuals[index] = individuals[i];
            individuals[i] = a;
        }
    }
}
