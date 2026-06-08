package com.snakeai.genetic.mutation;

import com.snakeai.genetic.algorithm.Individual;

import java.util.Random;

public class UniformMutation {
    private final double mutationRate;
    private final double amplitude;
    private final Random random;

    public UniformMutation(double mutationRate, double amplitude, Random random) {
        this.mutationRate = mutationRate;
        this.amplitude = amplitude;
        this.random = random;
    }

    public void mutate(Individual individual) {
        double[] genome = individual.getGenome();
        for (int i = 0; i < genome.length; i++) {
            if (random.nextDouble() < mutationRate) {
                // Mutates uniformly between -amplitude and +amplitude
                genome[i] += (random.nextDouble() * 2.0 - 1.0) * amplitude;
            }
        }
    }
}
