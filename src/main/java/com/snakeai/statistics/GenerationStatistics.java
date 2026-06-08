package com.snakeai.statistics;

import com.snakeai.training.GenerationSummary;

import java.util.ArrayList;
import java.util.List;

public class GenerationStatistics {
    private final List<GenerationSummary> history;

    public GenerationStatistics() {
        this.history = new ArrayList<>();
    }

    public GenerationStatistics(List<GenerationSummary> history) {
        this.history = new ArrayList<>(history);
    }

    public synchronized void addSummary(GenerationSummary summary) {
        history.add(summary);
    }

    public synchronized List<GenerationSummary> getHistory() {
        return new ArrayList<>(history);
    }

    public synchronized GenerationSummary getBestGeneration() {
        return history.stream()
                .max((s1, s2) -> Double.compare(s1.bestFitness(), s2.bestFitness()))
                .orElse(null);
    }

    public synchronized int getBestScore() {
        return history.stream()
                .mapToInt(GenerationSummary::bestScore)
                .max()
                .orElse(0);
    }

    public synchronized double getBestFitness() {
        return history.stream()
                .mapToDouble(GenerationSummary::bestFitness)
                .max()
                .orElse(0.0);
    }
}
