package com.snakeai.evolution.fitness;

public class BalancedFitnessStrategy implements FitnessStrategy {

    @Override
    public double calculateFitness(GameSimulationResult result) {

        double pathScore = (result.closerSteps() * 10.0) - (result.awaySteps() * 45.0);
        double distancePenalty = result.averageDistance() * 8.0;
        double efficiencyBonus = result.totalEfficiencyScore() * 2.0;
        double foodScore = result.score() * 200.0;

        double fitness = pathScore - distancePenalty + efficiencyBonus + foodScore;

        fitness = Math.max(5.0, fitness);
        double zigZagPenalty = result.directionChanges() * 5.0;
        fitness = Math.max(1.0, fitness - zigZagPenalty);

        switch (result.reason()) {
            case CYCLE_DETECTED -> fitness *= 0.1;
            case WALL_COLLISION -> fitness *= 0.3;
            case SELF_COLLISION -> fitness *= 0.5;
        }

        return Math.max(1.0, fitness);
    }
}