package com.snakeai.evolution.fitness;

public class FoodAndAwayStepFitnessStrategy implements FitnessStrategy {

    @Override
    public double calculateFitness(GameSimulationResult result) {

        double score = result.score();
        double steps = result.totalSteps();

        double stepWeight = 1.0 / (1.0 + score);

        double fitness = score * 400;
        fitness -= steps * 40 * stepWeight;

        switch (result.reason()) {
            case CYCLE_DETECTED -> fitness *= 0.1;
            case WALL_COLLISION -> fitness *= 0.3;
            case SELF_COLLISION -> fitness *= 0.5;
        }

        return Math.max(1.0, fitness);
    }
}