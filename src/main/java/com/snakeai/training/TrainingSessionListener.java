package com.snakeai.training;

public interface TrainingSessionListener {
    void onGenerationCompleted(GenerationSummary summary);
    void onTrainingFinished();
    void onTrainingError(String message, Throwable throwable);
}
