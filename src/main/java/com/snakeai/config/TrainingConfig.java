package com.snakeai.config;

import java.util.List;

public record TrainingConfig(
        int populationSize,
        int elitismCount,
        int tournamentSize,
        double mutationRate,
        double mutationAmplitude,
        int boardWidth,
        int boardHeight,
        int visionWindowSize,
        EvolutionMode evolutionMode,
        EliteSelectionMode eliteSelectionMode,
        int evaluationsPerIndividual,
        int evaluationThreads,
        List<Integer> hiddenLayerSizes
) {
    public static TrainingConfig createDefault() {
        return new TrainingConfig(
                1000,
                20,
                5,
                0.02,
                0.3,
                20,
                20,
                11,
                EvolutionMode.CROSSOVER_AND_MUTATION,
                EliteSelectionMode.UNIFORM,
                3,
                Runtime.getRuntime().availableProcessors(),
                List.of(128, 64, 32)
        );
    }
}
