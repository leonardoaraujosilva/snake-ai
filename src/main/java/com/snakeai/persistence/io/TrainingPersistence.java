package com.snakeai.persistence.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.snakeai.persistence.model.TrainingCheckpoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrainingPersistence {
    private static final String BASE_DIR_NAME = "trainings";
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static Path getBaseDirectory() {
        return Paths.get(BASE_DIR_NAME);
    }

    private static Path getTrainingDirectory(String name) {
        return getBaseDirectory().resolve(name);
    }

    public static void saveCheckpoint(TrainingCheckpoint checkpoint) throws IOException {
        String name = checkpoint.metadata().name();
        Path dir = getTrainingDirectory(name);
        Files.createDirectories(dir);

        // Save JSON Checkpoint
        Path checkpointFile = dir.resolve("checkpoint.json");
        MAPPER.writeValue(checkpointFile.toFile(), checkpoint);

        // Save CSV Statistics
        Path csvFile = dir.resolve("statistics.csv");
        StatisticsExporter.exportToCsv(checkpoint.statisticsHistory(), csvFile);
    }

    public static TrainingCheckpoint loadCheckpoint(String name) throws IOException {
        Path checkpointFile = getTrainingDirectory(name).resolve("checkpoint.json");
        if (!Files.exists(checkpointFile)) {
            throw new IOException("Checkpoint file not found for training: " + name);
        }
        return MAPPER.readValue(checkpointFile.toFile(), TrainingCheckpoint.class);
    }

    public static List<String> listAvailableTrainings() {
        Path baseDir = getBaseDirectory();
        if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
            return Collections.emptyList();
        }

        try (Stream<Path> stream = Files.list(baseDir)) {
            return stream
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static Path getReplayDirectory(String trainingName) {
        return getTrainingDirectory(trainingName).resolve("replays");
    }
}
