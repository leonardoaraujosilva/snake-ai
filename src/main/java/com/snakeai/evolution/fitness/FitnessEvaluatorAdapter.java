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
import com.snakeai.neural.network.NeuralNetwork;
import com.snakeai.neural.network.NeuralNetworkFactory;

public class FitnessEvaluatorAdapter {
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

    /**
     * Evaluates the individual over N simulations and returns both the average fitness
     * and the score from the first run — avoiding a redundant extra simulation.
     */
    public IndividualEvaluationResult evaluate(Individual individual) {
        NeuralNetwork brain = NeuralNetworkFactory.createNetwork(
                encoder.getInputSize(config),
                config.hiddenLayerSizes(),
                4
        );
        brain.setWeights(individual.getGenome());

        SnakeAgent agent = new SnakeAgent(brain, encoder, config);

        // Run N simulations as configured, to reduce the impact of lucky food spawns
        double totalFitness = 0.0;
        int numEvaluations = config.evaluationsPerIndividual();
        int scoreFromFirstRun = 0;

        for (int i = 0; i < numEvaluations; i++) {
            GameSimulationResult result = runSimulation(agent, evaluationSeed + i);
            totalFitness += fitnessStrategy.calculateFitness(result);

            // Capture score from the first run to avoid an extra simulation call
            if (i == 0) {
                scoreFromFirstRun = result.score();
            }
        }

        double averageFitness = totalFitness / numEvaluations;
        return new IndividualEvaluationResult(averageFitness, scoreFromFirstRun);
    }

    private GameSimulationResult runSimulation(SnakeAgent agent, long seed) {
        GameEngine engine = new GameEngine(config.boardWidth(), config.boardHeight(), seed);
        GameState state = engine.getGameState();

        int closerSteps = 0;
        int awaySteps = 0;
        double distanceSum = 0;
        
        int currentScore = state.getScore();
        int totalEfficiencyScore = 0;
        int stepsToFood = 0;
        Direction previousDirection = null;
        int directionChanges = 0;

        while (!state.isGameOver()) {
            Position head = state.getSnake().getHead();
            Position food = state.getFood();
            double oldDistance = getManhattanDistance(head, food);

            Direction dir = agent.chooseDirection(state);
            if (previousDirection != null && previousDirection != dir) {
                directionChanges++;
            }
            previousDirection = dir;
            engine.step(dir);
            stepsToFood++;

            if (state.getScore() > currentScore) {
                currentScore = state.getScore();
                totalEfficiencyScore += Math.max(1, 200 - stepsToFood);
                stepsToFood = 0;
            }

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
        double averageStepsPerFood =
                state.getScore() == 0
                        ? state.getTotalSteps()
                        : (double) state.getTotalSteps() / state.getScore();

        return new GameSimulationResult(
                state.getScore(),
                state.getTotalSteps(),
                engine.getGameOverReason(),
                state.getStepsSinceLastFood(),
                state.getScore(),
                averageStepsPerFood,
                closerSteps,
                awaySteps,
                totalEfficiencyScore,
                directionChanges
        );
    }

    private double getManhattanDistance(Position p1, Position p2) {
        return Math.abs(p1.row() - p2.row()) + Math.abs(p1.col() - p2.col());
    }
}
