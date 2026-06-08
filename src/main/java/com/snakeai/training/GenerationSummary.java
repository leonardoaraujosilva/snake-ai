package com.snakeai.training;

public record GenerationSummary(
        int generation,
        double bestFitness,
        double averageFitness,
        double worstFitness,
        int bestScore
) {}
