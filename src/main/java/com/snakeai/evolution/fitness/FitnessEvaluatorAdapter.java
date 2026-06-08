package com.snakeai.evolution.fitness;

import com.snakeai.config.TrainingConfig;
import com.snakeai.domain.board.Position;
import com.snakeai.domain.game.Direction;
import com.snakeai.domain.game.GameEngine;
import com.snakeai.domain.game.GameState;
import com.snakeai.evolution.agent.SnakeAgent;
import com.snakeai.evolution.encoder.GameStateEncoder;
import com.snakeai.evolution.encoder.HeadCenteredLocalVisionEncoder;
import com.snakeai.genetic.algorithm.Individual;
import com.snakeai.genetic.algorithm.FitnessEvaluator;
import com.snakeai.neural.network.NeuralNetwork;
import com.snakeai.neural.network.NeuralNetworkFactory;

import java.util.Random;

public class FitnessEvaluatorAdapter implements FitnessEvaluator {
    private final TrainingConfig config;
    private final GameStateEncoder encoder;
    private final FitnessStrategy fitnessStrategy;
    private final long evaluationSeed;

    public FitnessEvaluatorAdapter(
            TrainingConfig config,
            FitnessStrategy fitnessStrategy,
            long evaluationSeed
    ) {
        this.config = config;
        this.encoder = new HeadCenteredLocalVisionEncoder();
        this.fitnessStrategy = fitnessStrategy;
        this.evaluationSeed = evaluationSeed;
    }

    @Override
    public double evaluate(Individual individual) {
        // Build brain from genome
        NeuralNetwork brain = NeuralNetworkFactory.createNetwork(
                encoder.getInputSize(config),
                config.hiddenLayerSizes(),
                4
        );
        brain.setWeights(individual.getGenome());

        SnakeAgent agent = new SnakeAgent(brain, encoder, config);

        // Run simulation 3 times for robustness to avoid lucky seeds
        double totalFitness = 0.0;
        int numEvaluations = 3;
        for (int i = 0; i < numEvaluations; i++) {
            GameSimulationResult result = runSimulation(agent, evaluationSeed + i);
            totalFitness += fitnessStrategy.calculateFitness(result);
        }
        return totalFitness / numEvaluations;
    }

    private GameSimulationResult runSimulation(SnakeAgent agent, long seed) {
        GameEngine engine = new GameEngine(config.boardWidth(), config.boardHeight(), seed);
        GameState state = engine.getGameState();

        int closerSteps = 0;
        int awaySteps = 0;
        double distanceSum = 0;

        while (!state.isGameOver()) {
            Position head = state.getSnake().getHead();
            Position food = state.getFood();
            double oldDistance = getManhattanDistance(head, food);

            Direction dir = agent.chooseDirection(state);
            engine.step(dir);

            if (!state.isGameOver()) {
                Position newHead = state.getSnake().getHead();
                double newDistance = getManhattanDistance(newHead, food);

                if (newDistance < oldDistance) {
                    closerSteps++;
                } else if (newDistance > oldDistance) {
                    awaySteps++;
                }
                distanceSum += newDistance;
            }
        }

        double averageDistance = state.getTotalSteps() == 0 ? 0 : distanceSum / state.getTotalSteps();

        return new GameSimulationResult(
                state.getScore(),
                state.getTotalSteps(),
                engine.isCycleDetected(),
                engine.isTimeLimitExceeded(),
                state.getStepsSinceLastFood(),
                state.getScore(),
                averageDistance,
                closerSteps,
                awaySteps
        );
    }

    private double getManhattanDistance(Position p1, Position p2) {
        return Math.abs(p1.row() - p2.row()) + Math.abs(p1.col() - p2.col());
    }
}
