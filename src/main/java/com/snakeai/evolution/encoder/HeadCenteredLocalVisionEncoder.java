package com.snakeai.evolution.encoder;

import com.snakeai.config.TrainingConfig;
import com.snakeai.domain.board.Cell;
import com.snakeai.domain.board.Position;
import com.snakeai.domain.game.GameState;
import com.snakeai.domain.game.Snake;

public class HeadCenteredLocalVisionEncoder implements GameStateEncoder {

    @Override
    public double[] encode(GameState state, TrainingConfig config) {
        int windowSize = config.visionWindowSize();
        int halfWindow = windowSize / 2;
        Position head = state.getSnake().getHead();
        Snake snake = state.getSnake();
        Position food = state.getFood();

        double[] input = new double[getInputSize(config)];
        int index = 0;

        for (int rowOffset = -halfWindow; rowOffset <= halfWindow; rowOffset++) {
            for (int colOffset = -halfWindow; colOffset <= halfWindow; colOffset++) {
                Position currentPos = new Position(head.row() + rowOffset, head.col() + colOffset);

                if (state.getBoard().isOutside(currentPos)) {
                    input[index++] = 0.25; // Wall
                } else if (currentPos.equals(head)) {
                    input[index++] = 0.75; // Head
                } else if (currentPos.equals(food)) {
                    input[index++] = 1.00; // Food
                } else if (snake.contains(currentPos)) {
                    input[index++] = 0.50; // Body
                } else {
                    input[index++] = 0.00; // Empty
                }
            }
        }
        // Add normalized direction to food
        double dx = food.col() - head.col();
        double dy = food.row() - head.row();
        double distance = Math.max(1.0, Math.sqrt(dx * dx + dy * dy));
        
        input[index++] = dx / distance;
        input[index++] = dy / distance;

        return input;
    }

    @Override
    public int getInputSize(TrainingConfig config) {
        return (config.visionWindowSize() * config.visionWindowSize()) + 2;
    }
}
