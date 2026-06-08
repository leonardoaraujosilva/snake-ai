package com.snakeai.evolution.fitness;

public class BalancedFitnessStrategy implements FitnessStrategy {
    @Override
    public double calculateFitness(GameSimulationResult result) {
        // Base points for survival
        double fitness = result.totalSteps() * 1.0;

        // Reward eating food
        fitness += result.score() * 150.0;

        // Reward/Penalty based on moving towards or away from food
        fitness += result.stepsMovingCloserToFood() * 2.0;
        fitness -= result.stepsMovingAwayFromFood() * 2.5;

        // Penalties for ending poorly
        if (result.cycleDetected()) {
            fitness -= 150.0;
        }

        if (result.timeLimitExceeded()) {
            fitness -= 100.0;
        }

        // Penalty for dying early without eating
        if (result.score() == 0 && (result.cycleDetected() || result.timeLimitExceeded() || result.totalSteps() < 10)) {
            fitness -= 50.0;
        }

        // Avoid negative fitness
        return Math.max(0.1, fitness);
    }
}
