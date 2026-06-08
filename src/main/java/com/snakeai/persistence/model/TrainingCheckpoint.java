package com.snakeai.persistence.model;

import com.snakeai.training.GenerationSummary;
import java.util.List;

public record TrainingCheckpoint(
        TrainingMetadata metadata,
        List<SavedEliteIndividual> elite,
        List<GenerationSummary> statisticsHistory
) {}
