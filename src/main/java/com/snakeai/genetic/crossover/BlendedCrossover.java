package com.snakeai.genetic.crossover;

import com.snakeai.genetic.algorithm.Individual;

import java.util.Random;

public class BlendedCrossover {
    private final Random random;

    public BlendedCrossover(Random random) {
        this.random = random;
    }

    public Individual crossover(Individual parent1, Individual parent2) {
        double[] p1Genome = parent1.getGenome();
        double[] p2Genome = parent2.getGenome();
        int genomeLength = p1Genome.length;
        double[] childGenome = new double[genomeLength];

        double alpha = random.nextDouble();
        for (int i = 0; i < genomeLength; i++) {
            // Blended crossover: child is a weighted average of parents
            childGenome[i] = (alpha * p1Genome[i]) + ((1.0 - alpha) * p2Genome[i]);
        }
        
        return new Individual(childGenome);
    }
}
