package com.snakeai.genetic.selection;

import com.snakeai.genetic.algorithm.Individual;
import com.snakeai.genetic.algorithm.Population;

import java.util.Random;

public class TournamentSelection {
    private final int tournamentSize;
    private final Random random;

    public TournamentSelection(int tournamentSize, Random random) {
        this.tournamentSize = tournamentSize;
        this.random = random;
    }

    public Individual select(Population population) {
        int popSize = population.getIndividuals().size();
        Individual best = null;

        for (int i = 0; i < tournamentSize; i++) {
            Individual candidate = population.getIndividuals().get(random.nextInt(popSize));
            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        return best;
    }
}
