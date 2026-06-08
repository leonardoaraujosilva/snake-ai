package com.snakeai.persistence.model;

import com.snakeai.config.TrainingConfig;

import java.time.LocalDateTime;

public record TrainingMetadata(
        String name,
        String creationDate,
        int currentGeneration,
        double bestFitness,
        int bestScore,
        TrainingConfig config
) {}
