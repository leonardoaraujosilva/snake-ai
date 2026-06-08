package com.snakeai.neural.activation;

public class ReLUActivation implements ActivationFunction {
    @Override
    public double activate(double x) {
        return Math.max(0, x);
    }

    @Override
    public double[] activateArray(double[] values) {
        double[] activated = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            activated[i] = activate(values[i]);
        }
        return activated;
    }
}
