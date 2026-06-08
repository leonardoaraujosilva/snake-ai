package com.snakeai.config;

public enum EvolutionMode {
    CROSSOVER_AND_MUTATION("Crossover + Mutação"),
    PURE_MUTATION("Mutação Pura sobre a Elite");

    private final String description;

    EvolutionMode(String description) {
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
