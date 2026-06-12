package com.snakeai.evolution.fitness;

import com.snakeai.domain.game.GameOverReason;

public record GameSimulationResult(
        int score,
        int totalSteps,
        GameOverReason reason,
        int stepsSinceLastFood,
        int scoreDuplicate,
        double averageDistance,
        int closerSteps,
        int awaySteps,
        int totalEfficiencyScore,
        int directionChanges
) {}
