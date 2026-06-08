package com.snakeai.persistence.model;

public record SavedEliteIndividual(
        double[] genome,
        double fitness
) {}
