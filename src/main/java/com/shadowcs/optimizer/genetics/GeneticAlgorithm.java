package com.shadowcs.optimizer.genetics;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

@Data
@Slf4j
public class GeneticAlgorithm<E> {

    private ExecutorService threadPool;

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

    private int genes;


    private double uniformRate = 0.5; // Crossover rate
    //private double uniformRate = 0.85;
    //private double mutationRate = 0.028;
    private double mutationRate = 0.075; // mutation rate
    private int tournamentSize = 5;
    private int elitism = 2;






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
        this.genes = genes;

        for(int i = 0; i < populationSize; i++) {
            population.add(new Chromosome<>(genes, genetics()));
        }

        Chromosome<E> mostFit = population.get(0);
        int generation = 0;
        int same = 0;
        do {
            //long startEvo = System.currentTimeMillis();
            if(generation > 0) {
                population = evolvePopulation(population);
            }
            //long endEvo = System.currentTimeMillis();

            same++;
            generation++;

            // calculate fitness for each Chromosome in a population
            //long startF = System.currentTimeMillis();
            Semaphore sema = new Semaphore(-population.size());
            population.forEach(chromo -> {
                if(Double.isNaN(chromo.fitness())) {
                    threadPool.submit(() -> {
                        chromo.fitness(fitness().calculate(chromo));
                        sema.release();
                    });
                } else {
                    sema.release();
                }
            });
            sema.release();
            sema.acquireUninterruptibly();
            //long endF = System.currentTimeMillis();
            //log.info("Timeings Fit {}", endF - startF);

            population.sort((d1, d2) -> {
                if (d1.fitness() < d2.fitness())
                    return 1;           // Neither val is NaN, thisVal is smaller, so we don't want it and return +
                if (d1.fitness() > d2.fitness())
                    return -1;            // Neither val is NaN, thisVal is larger, so we want it and return -

                // Cannot use doubleToRawLongBits because of possibility of NaNs.
                long thisBits    = Double.doubleToLongBits(d1.fitness());
                long anotherBits = Double.doubleToLongBits(d2.fitness());

                // Just use long compare...
                return (Long.compare(anotherBits, thisBits));
            });
            //population.sort(Comparator.comparingDouble(Chromosome::fitness));
            //Collections.reverse(population);

            if(mostFit.fitness() < population.get(0).fitness()) {
                mostFit = population.get(0);
                same = 0;
            }
            //log.info("Algorithm Running on Generation: {} with fitness {}", generation, mostFit.fitness());
        } while(shouldContinue(mostFit.fitness(), generation, same));

        log.info("Algorithm Finished on Generation: {} with fitness {}", generation, mostFit.fitness());

        return mostFit;
    }

    private List<Chromosome<E>> evolvePopulation(List<Chromosome<E>> population) {

        //long startE = System.currentTimeMillis();

        List<Chromosome<E>> newPopulation = new ArrayList<>(population.size());

        int size = population.size();

        for(int i = 0; i < elitism; i++) {
            // Make a copy of ourselves to add to the list...
            // This needs to be a copy as we mutate and do things to the original ones
            var copy = new Chromosome<>(population.get(i));
            newPopulation.add(population.remove(i));
            population.add(i, copy);
        }

        List<Chromosome<E>> duplicate = new ArrayList<>(population);

        // Duplicate so we can mutate them
        for(int i = 0; i < elitism; i++) {
            newPopulation.add(population.get(i));
        }

        // This actually helps a lot with tournament selection
        Collections.shuffle(duplicate);

        // TODO: see if we can do this parallel
        for (int i = elitism * 2; i < size; i++) {

            float value = ThreadLocalRandom.current().nextFloat();

            Chromosome<E> indiv1;
            Chromosome<E> indiv2;

            if (value <= 0.5) {
                // Fitest in subset
                indiv1 = tournamentSelection(duplicate);
                indiv2 = tournamentSelection(duplicate);
            } else if(value <= 0.90) {
                // random
                indiv1 = randomSelection(duplicate);
                indiv2 = randomSelection(duplicate);
            } else {
                // Cross two most fit
                indiv1 = population.get(ThreadLocalRandom.current().nextInt(elitism, elitism * 2));
                indiv2 = population.get(ThreadLocalRandom.current().nextInt(elitism, elitism * 2));

            }

            Chromosome<E> newIndiv = crossover(indiv1, indiv2);
            this.mutate(newIndiv);

            newPopulation.add(newIndiv);
        }

        //long endE = System.currentTimeMillis();
        //log.info("Timeings Evo {}", endE - startE);

        //long startT = System.currentTimeMillis();

        // This should leave us elitism number for new random generations
        Semaphore sema = new Semaphore(-newPopulation.size());
        for(int i = 0; i < newPopulation.size(); i++) {
            int finalI = i;
            threadPool.submit(() -> {
                var chrom = newPopulation.get(finalI);
                if(finalI >= elitism) {
                    genetics.validate(chrom);
                }
                sema.release();
            });
        }
        sema.release();
        sema.acquireUninterruptibly();

        //long endT = System.currentTimeMillis();
        //log.info("Timeings Val {}", endT - startT);

        return newPopulation;
    }

    private Chromosome<E> crossover(Chromosome<E> indiv1, Chromosome<E> indiv2) {
        Chromosome<E> newSol = new Chromosome<>(genes);

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
                break;
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
