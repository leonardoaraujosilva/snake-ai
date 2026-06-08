package com.snakeai.evolution.fitness;

/**
 * Carries both the average fitness and the score from the first simulation run,
 * allowing the training session to avoid an extra redundant simulation just for score.
 */
public record IndividualEvaluationResult(
        double averageFitness,
        int scoreFromFirstRun
) {}
