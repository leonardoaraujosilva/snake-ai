package com.snakeai.neural.layer;

import com.snakeai.neural.activation.ActivationFunction;

public class DenseLayer {
    private final int inputSize;
    private final int outputSize;
    private final double[][] weights;
    private final double[] biases;
    private final ActivationFunction activationFunction;

    public DenseLayer(int inputSize, int outputSize, ActivationFunction activationFunction) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.weights = new double[outputSize][inputSize];
        this.biases = new double[outputSize];
        this.activationFunction = activationFunction;
    }

    public int getInputSize() {
        return inputSize;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public double[][] getWeights() {
        return weights;
    }

    public double[] getBiases() {
        return biases;
    }

    public double[] forward(double[] inputs) {
        if (inputs.length != inputSize) {
            throw new IllegalArgumentException("Input size mismatch. Expected: " + inputSize + ", got: " + inputs.length);
        }

        double[] rawOutputs = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            double sum = 0.0;
            for (int j = 0; j < inputSize; j++) {
                sum += inputs[j] * weights[i][j];
            }
            rawOutputs[i] = sum + biases[i];
        }

        return activationFunction.activateArray(rawOutputs);
    }

    public int getWeightCount() {
        return (inputSize * outputSize) + outputSize;
    }

    public double[] getWeightsAndBiases() {
        double[] flat = new double[getWeightCount()];
        int index = 0;
        for (int i = 0; i < outputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                flat[index++] = weights[i][j];
            }
        }
        for (int i = 0; i < outputSize; i++) {
            flat[index++] = biases[i];
        }
        return flat;
    }

    public void setWeightsAndBiases(double[] flat, int startIndex) {
        int index = startIndex;
        for (int i = 0; i < outputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                weights[i][j] = flat[index++];
            }
        }
        for (int i = 0; i < outputSize; i++) {
            biases[i] = flat[index++];
        }
    }
}
