package com.snakeai.evolution.agent;

import com.snakeai.domain.game.Direction;
import com.snakeai.domain.game.GameState;
import com.snakeai.evolution.encoder.GameStateEncoder;
import com.snakeai.neural.network.NeuralNetwork;
import com.snakeai.config.TrainingConfig;

public class SnakeAgent {
    private final NeuralNetwork brain;
    private final GameStateEncoder encoder;
    private final TrainingConfig config;

    public SnakeAgent(NeuralNetwork brain, GameStateEncoder encoder, TrainingConfig config) {
        this.brain = brain;
        this.encoder = encoder;
        this.config = config;
    }

    public NeuralNetwork getBrain() {
        return brain;
    }

    public Direction chooseDirection(GameState state) {
        double[] input = encoder.encode(state, config);
        double[] outputs = brain.predict(input);

        // Determine the forbidden direction (180 degree turn into its own neck)
        Direction forbiddenDirection = null;
        com.snakeai.domain.game.Snake snake = state.getSnake();
        if (snake.getLength() > 1) {
            java.util.Iterator<com.snakeai.domain.board.Position> it = snake.getBody().iterator();
            com.snakeai.domain.board.Position head = it.next();
            com.snakeai.domain.board.Position second = it.next();
            for (Direction d : Direction.values()) {
                if (d.move(head).equals(second)) {
                    forbiddenDirection = d;
                    break;
                }
            }
        }

        // Map output index to Direction enum
        // Outputs are index matching UP, RIGHT, DOWN, LEFT
        int maxIndex = -1;
        double maxVal = -Double.MAX_VALUE;
        for (int i = 0; i < outputs.length; i++) {
            Direction dir = switch (i) {
                case 0 -> Direction.UP;
                case 1 -> Direction.RIGHT;
                case 2 -> Direction.DOWN;
                case 3 -> Direction.LEFT;
                default -> throw new IllegalStateException("Unexpected output index: " + i);
            };

            if (dir != forbiddenDirection && outputs[i] > maxVal) {
                maxVal = outputs[i];
                maxIndex = i;
            }
        }

        return switch (maxIndex) {
            case 0 -> Direction.UP;
            case 1 -> Direction.RIGHT;
            case 2 -> Direction.DOWN;
            case 3 -> Direction.LEFT;
            default -> Direction.UP; // Fallback if something goes wrong
        };
    }
}
