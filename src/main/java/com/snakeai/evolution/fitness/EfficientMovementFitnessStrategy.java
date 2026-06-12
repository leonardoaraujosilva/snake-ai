package com.snakeai.evolution.fitness;

public class EfficientMovementFitnessStrategy implements FitnessStrategy {

    @Override
    public double calculateFitness(GameSimulationResult result) {

        double fitness = 0;

        fitness += (((result.score() * 1000) + result.closerSteps() - result.awaySteps()) * 1000);
//        fitness =
//                result.score() * 1000
//                        + result.closerSteps() * 60
//                        - result.awaySteps() * 300;

        switch (result.reason()) {
            case CYCLE_DETECTED -> fitness *= 0.1;
            case SELF_COLLISION -> fitness *= 0.3;
            case WALL_COLLISION -> fitness *= 0.2;
            //case TIME_LIMIT_EXCEEDED -> fitness *= 0.7;
        }

        return Math.max(1.0, fitness);
    }
}
