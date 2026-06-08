package com.snakeai.config;

public enum EliteSelectionMode {
    UNIFORM("Uniforme (aleatório entre elites)"),
    WEIGHTED("Ponderado por Fitness");

    private final String description;

    EliteSelectionMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
