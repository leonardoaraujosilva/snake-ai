package com.snakeai.genetic.algorithm;

public class Individual {
    private final double[] genome;
    private double fitness;

    public Individual(double[] genome) {
        this.genome = genome;
        this.fitness = 0.0;
    }

    public double[] getGenome() {
        return genome;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
