package com.snakeai.evolution.encoder;

import com.snakeai.config.TrainingConfig;
import com.snakeai.domain.board.Position;
import com.snakeai.domain.game.GameState;
import com.snakeai.domain.game.Snake;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<Position, Double> bodyGradient = new HashMap<>();
        Deque<Position> body = snake.getBody();
        int totalLength = body.size();

        int i = 0;
        for (Position segment : body) {
            double severity = 1.0 - ((double) i / totalLength);
            double bodyValue = -0.2 - (0.8 * severity);
            bodyGradient.put(segment, bodyValue);
            i++;
        }

        for (int rowOffset = -halfWindow; rowOffset <= halfWindow; rowOffset++) {
            for (int colOffset = -halfWindow; colOffset <= halfWindow; colOffset++) {
                Position currentPos = new Position(head.row() + rowOffset, head.col() + colOffset);

                if (state.getBoard().isOutside(currentPos)) {
                    input[index++] = -1.0;
                } else if (currentPos.equals(head)) {
                    input[index++] = 0.0;
                } else if (currentPos.equals(food)) {
                    input[index++] = 1.0;
                } else if (bodyGradient.containsKey(currentPos)) {
                    input[index++] = bodyGradient.get(currentPos);
                } else {
                    input[index++] = 0.0;
                }
            }
        }

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