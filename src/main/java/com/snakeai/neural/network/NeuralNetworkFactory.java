package com.snakeai.neural.network;

import com.snakeai.config.TrainingConfig;
import com.snakeai.neural.activation.ActivationFunction;
import com.snakeai.neural.activation.ReLUActivation;
import com.snakeai.neural.activation.SoftmaxActivation;
import com.snakeai.neural.layer.DenseLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NeuralNetworkFactory {

    public static NeuralNetwork createNetwork(int inputSize, List<Integer> hiddenSizes, int outputSize) {
        List<DenseLayer> layers = new ArrayList<>();
        ActivationFunction relu = new ReLUActivation();
        ActivationFunction softmax = new SoftmaxActivation();

        int currentInputSize = inputSize;
        for (int hiddenSize : hiddenSizes) {
            layers.add(new DenseLayer(currentInputSize, hiddenSize, relu));
            currentInputSize = hiddenSize;
        }

        layers.add(new DenseLayer(currentInputSize, outputSize, softmax));
        return new NeuralNetwork(layers);
    }

    public static NeuralNetwork createRandomNetwork(int inputSize, TrainingConfig config, Random random) {
        int outputSize = 4; // UP, RIGHT, DOWN, LEFT
        NeuralNetwork network = createNetwork(inputSize, config.hiddenLayerSizes(), outputSize);

        initializeWeightsRandomly(network, random);
        return network;
    }

    private static void initializeWeightsRandomly(NeuralNetwork network, Random random) {
        double[] weights = new double[network.getWeightCount()];
        // He Initialization (Gaussian with stdDev = sqrt(2/n)) or simple Gaussian N(0, 0.1) for evolutionary algorithm stability
        // For genetic algorithms, standard normal distribution N(0, 1) scaled down works very well.
        for (int i = 0; i < weights.length; i++) {
            weights[i] = random.nextGaussian() * 0.1;
        }
        network.setWeights(weights);
    }

    public static NeuralNetwork copyNetwork(NeuralNetwork source) {
        List<DenseLayer> layers = source.getLayers();
        List<Integer> hiddenSizes = new ArrayList<>();
        int inputSize = layers.getFirst().getInputSize();
        for (int i = 0; i < layers.size() - 1; i++) {
            hiddenSizes.add(layers.get(i).getOutputSize());
        }
        int outputSize = layers.getLast().getOutputSize();

        NeuralNetwork copy = createNetwork(inputSize, hiddenSizes, outputSize);
        copy.setWeights(source.getWeights());
        return copy;
    }
}
