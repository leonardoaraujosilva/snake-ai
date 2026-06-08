package com.snakeai.genetic.algorithm;

import com.snakeai.config.EliteSelectionMode;
import com.snakeai.config.EvolutionMode;
import com.snakeai.genetic.crossover.BlendedCrossover;
import com.snakeai.genetic.mutation.UniformMutation;
import com.snakeai.genetic.selection.TournamentSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {
    private final int populationSize;
    private final int elitismCount;
    private final TournamentSelection selection;
    private final BlendedCrossover crossover;
    private final UniformMutation mutation;
    private final EvolutionMode evolutionMode;
    private final EliteSelectionMode eliteSelectionMode;
    private final Random random;

    public GeneticAlgorithm(
            int populationSize,
            int elitismCount,
            int tournamentSize,
            double mutationRate,
            double mutationAmplitude,
            EvolutionMode evolutionMode,
            EliteSelectionMode eliteSelectionMode,
            Random random
    ) {
        this.populationSize = populationSize;
        this.elitismCount = elitismCount;
        this.selection = new TournamentSelection(tournamentSize, random);
        this.crossover = new BlendedCrossover(random);
        this.mutation = new UniformMutation(mutationRate, mutationAmplitude, random);
        this.evolutionMode = evolutionMode;
        this.eliteSelectionMode = eliteSelectionMode;
        this.random = random;
    }

    public Population evolve(Population currentPopulation) {
        currentPopulation.sortByFitnessDescending();

        List<Individual> nextGeneration = new ArrayList<>(populationSize);
        List<Individual> elites = new ArrayList<>();

        // 1. Elitismo
        for (int i = 0; i < Math.min(elitismCount, currentPopulation.getIndividuals().size()); i++) {
            Individual elite = currentPopulation.getIndividuals().get(i);
            // Copy elite genome to avoid modifying it during mutations of subsequent generations
            double[] eliteGenome = elite.getGenome().clone();
            Individual eliteCopy = new Individual(eliteGenome);
            eliteCopy.setFitness(elite.getFitness());
            nextGeneration.add(eliteCopy);
            elites.add(eliteCopy);
        }

        // 2. Crossover & Mutación
        while (nextGeneration.size() < populationSize) {
            Individual child;

            if (evolutionMode == EvolutionMode.PURE_MUTATION) {
                // Mutate only from the elite pool (or full population if no elitism)
                List<Individual> pool = elites.isEmpty() ? currentPopulation.getIndividuals() : elites;
                Individual parent = selectFromElitePool(pool);
                child = new Individual(parent.getGenome().clone());
                mutation.mutate(child);
            } else {
                Individual parent1 = selection.select(currentPopulation);
                Individual parent2 = selection.select(currentPopulation);

                child = crossover.crossover(parent1, parent2);
                mutation.mutate(child);
            }

            nextGeneration.add(child);
        }

        return new Population(nextGeneration);
    }

    /**
     * Selects a parent from the elite pool.
     * UNIFORM: every elite has the same probability of being selected.
     * WEIGHTED: probability is proportional to fitness — better elites generate more offspring.
     */
    private Individual selectFromElitePool(List<Individual> pool) {
        if (eliteSelectionMode == EliteSelectionMode.UNIFORM) {
            return pool.get(random.nextInt(pool.size()));
        }

        // WEIGHTED: roulette wheel proportional to fitness
        double totalFitness = pool.stream().mapToDouble(Individual::getFitness).sum();

        // Fallback to uniform if all fitnesses are zero
        if (totalFitness <= 0.0) {
            return pool.get(random.nextInt(pool.size()));
        }

        double threshold = random.nextDouble() * totalFitness;
        double accumulated = 0.0;
        for (Individual candidate : pool) {
            accumulated += candidate.getFitness();
            if (accumulated >= threshold) {
                return candidate;
            }
        }

        // Safety fallback
        return pool.getLast();
    }
}
