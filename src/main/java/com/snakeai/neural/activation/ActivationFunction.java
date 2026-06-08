package com.snakeai.neural.activation;

public interface ActivationFunction {
    double activate(double x);
    double[] activateArray(double[] values);
}
