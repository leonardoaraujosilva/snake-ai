package com.snakeai.replay;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReplayPersistence {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void saveReplay(ReplayRecord record, Path targetFile) throws IOException {
        if (targetFile.getParent() != null) {
            Files.createDirectories(targetFile.getParent());
        }
        MAPPER.writeValue(targetFile.toFile(), record);
    }

    public static ReplayRecord loadReplay(Path file) throws IOException {
        return MAPPER.readValue(file.toFile(), ReplayRecord.class);
    }
}
