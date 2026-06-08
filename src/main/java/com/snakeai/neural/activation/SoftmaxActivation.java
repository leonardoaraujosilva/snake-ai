package com.snakeai.neural.activation;

import java.util.Arrays;

public class SoftmaxActivation implements ActivationFunction {
    @Override
    public double activate(double x) {
        throw new UnsupportedOperationException("Softmax requires calculations across all elements.");
    }

    @Override
    public double[] activateArray(double[] values) {
        double max = Arrays.stream(values).max().orElse(0.0);
        double sum = 0.0;
        double[] exponents = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            exponents[i] = Math.exp(values[i] - max);
            sum += exponents[i];
        }

        double[] activated = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            activated[i] = exponents[i] / (sum == 0.0 ? 1.0 : sum);
        }
        return activated;
    }
}
