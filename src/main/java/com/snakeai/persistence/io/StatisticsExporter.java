package com.snakeai.persistence.io;

import com.snakeai.training.GenerationSummary;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StatisticsExporter {

    public static void exportToCsv(List<GenerationSummary> history, Path targetFile) throws IOException {
        // Ensure parent directory exists
        if (targetFile.getParent() != null) {
            Files.createDirectories(targetFile.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(targetFile)) {
            writer.write("generation,bestFitness,averageFitness,worstFitness,bestScore");
            writer.newLine();

            for (GenerationSummary summary : history) {
                writer.write(String.format("%d,%.4f,%.4f,%.4f,%d",
                        summary.generation(),
                        summary.bestFitness(),
                        summary.averageFitness(),
                        summary.worstFitness(),
                        summary.bestScore()
                ));
                writer.newLine();
            }
        }
    }
}
