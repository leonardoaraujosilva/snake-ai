package com.snakeai.evolution.fitness;

public record GameSimulationResult(
        int score,
        int totalSteps,
        boolean cycleDetected,
        boolean timeLimitExceeded,
        int stepsSinceLastFood,
        int foodEaten,
        double averageDistanceToFood,
        int stepsMovingCloserToFood,
        int stepsMovingAwayFromFood,
        int totalEfficiencyScore
) {}
