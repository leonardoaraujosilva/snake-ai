package com.snakeai.genetic.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Population {
    private final List<Individual> individuals;

    public Population(List<Individual> individuals) {
        this.individuals = new ArrayList<>(individuals);
    }

    public List<Individual> getIndividuals() {
        return individuals;
    }

    public void sortByFitnessDescending() {
        individuals.sort(Comparator.comparingDouble(Individual::getFitness).reversed());
    }

    public Individual getBest() {
        if (individuals.isEmpty()) {
            throw new IllegalStateException("Population is empty");
        }
        sortByFitnessDescending();
        return individuals.getFirst();
    }

    public double getAverageFitness() {
        return individuals.stream()
                .mapToDouble(Individual::getFitness)
                .average()
                .orElse(0.0);
    }

    public double getWorstFitness() {
        if (individuals.isEmpty()) {
            return 0.0;
        }
        sortByFitnessDescending();
        return individuals.getLast().getFitness();
    }
}
