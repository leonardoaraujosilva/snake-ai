package com.snakeai.neural.network;

import com.snakeai.neural.layer.DenseLayer;
import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork {
    private final List<DenseLayer> layers;

    public NeuralNetwork(List<DenseLayer> layers) {
        this.layers = layers;
    }

    public List<DenseLayer> getLayers() {
        return layers;
    }

    public double[] predict(double[] inputs) {
        double[] current = inputs;
        for (DenseLayer layer : layers) {
            current = layer.forward(current);
        }
        return current;
    }

    public int getWeightCount() {
        int count = 0;
        for (DenseLayer layer : layers) {
            count += layer.getWeightCount();
        }
        return count;
    }

    public double[] getWeights() {
        double[] weights = new double[getWeightCount()];
        int index = 0;
        for (DenseLayer layer : layers) {
            double[] layerWeights = layer.getWeightsAndBiases();
            System.arraycopy(layerWeights, 0, weights, index, layerWeights.length);
            index += layerWeights.length;
        }
        return weights;
    }

    public void setWeights(double[] weights) {
        int expectedCount = getWeightCount();
        if (weights.length != expectedCount) {
            throw new IllegalArgumentException("Weight size mismatch. Expected: " + expectedCount + ", got: " + weights.length);
        }

        int index = 0;
        for (DenseLayer layer : layers) {
            layer.setWeightsAndBiases(weights, index);
            index += layer.getWeightCount();
        }
    }

    public NeuralNetwork copy() {
        return NeuralNetworkFactory.copyNetwork(this);
    }
}
