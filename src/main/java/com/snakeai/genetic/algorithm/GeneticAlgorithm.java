package com.snakeai.genetic.algorithm;

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
    private final Random random;

    public GeneticAlgorithm(
            int populationSize,
            int elitismCount,
            int tournamentSize,
            double mutationRate,
            double mutationAmplitude,
            EvolutionMode evolutionMode,
            Random random
    ) {
        this.populationSize = populationSize;
        this.elitismCount = elitismCount;
        this.selection = new TournamentSelection(tournamentSize, random);
        this.crossover = new BlendedCrossover(random);
        this.mutation = new UniformMutation(mutationRate, mutationAmplitude, random);
        this.evolutionMode = evolutionMode;
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
                // Mutate only from the elite pool (or population if no elitism)
                List<Individual> pool = elites.isEmpty() ? currentPopulation.getIndividuals() : elites;
                Individual parent = pool.get(random.nextInt(pool.size()));
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
}
