package com.snakeai.evolution.encoder;

import com.snakeai.config.TrainingConfig;
import com.snakeai.domain.board.Position;
import com.snakeai.domain.game.Direction;
import com.snakeai.domain.game.GameState;
import com.snakeai.domain.game.Snake;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class HeadCenteredLocalVisionEncoder implements GameStateEncoder {

    @Override
    public double[] encode(GameState state, TrainingConfig config) {
        int windowSize = config.visionWindowSize();
        int halfWindow = windowSize / 2;

        Snake snake = state.getSnake();
        Position head = snake.getHead();
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

        // Visão local centrada na cabeça
        for (int rowOffset = -halfWindow; rowOffset <= halfWindow; rowOffset++) {
            for (int colOffset = -halfWindow; colOffset <= halfWindow; colOffset++) {

                Position currentPos = new Position(
                        head.row() + rowOffset,
                        head.col() + colOffset
                );

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

        // Posição relativa da comida
        double dx = food.col() - head.col();
        double dy = food.row() - head.row();

        input[index++] = dx / (double) state.getBoard().getWidth();
        input[index++] = dy / (double) state.getBoard().getHeight();

        input[index++] = dx > 0 ? 1.0 : 0.0;
        input[index++] = dx < 0 ? 1.0 : 0.0;

        input[index++] = dy > 0 ? 1.0 : 0.0;
        input[index++] = dy < 0 ? 1.0 : 0.0;

        double distance = Math.sqrt(dx * dx + dy * dy);
        double maxDistance = Math.sqrt(
                state.getBoard().getWidth() * state.getBoard().getWidth()
                        + state.getBoard().getHeight() * state.getBoard().getHeight()
        );

        input[index++] = distance / maxDistance;

        // Direção atual da cobra (one-hot)
        Direction direction = getCurrentDirection(snake);

        input[index++] = direction == Direction.UP ? 1.0 : 0.0;
        input[index++] = direction == Direction.DOWN ? 1.0 : 0.0;
        input[index++] = direction == Direction.LEFT ? 1.0 : 0.0;
        input[index++] = direction == Direction.RIGHT ? 1.0 : 0.0;

        Position front = getFrontPosition(head, direction);
        Position left = getLeftPosition(head, direction);
        Position right = getRightPosition(head, direction);

        input[index++] = isBlocked(state, front) ? 1.0 : 0.0;
        input[index++] = isBlocked(state, left) ? 1.0 : 0.0;
        input[index++] = isBlocked(state, right) ? 1.0 : 0.0;

        input[index++] =
                snake.getLength()
                        / (double) (state.getBoard().getWidth() * state.getBoard().getHeight());

        return input;
    }

    private Direction getCurrentDirection(Snake snake) {
        if (snake.getLength() < 2) {
            return Direction.UP;
        }

        var iterator = snake.getBody().iterator();

        Position head = iterator.next();
        Position neck = iterator.next();

        int rowDiff = head.row() - neck.row();
        int colDiff = head.col() - neck.col();

        if (rowDiff == -1) {
            return Direction.UP;
        }

        if (rowDiff == 1) {
            return Direction.DOWN;
        }

        if (colDiff == -1) {
            return Direction.LEFT;
        }

        return Direction.RIGHT;
    }

    private boolean isBlocked(
            GameState state,
            Position position
    ) {
        return state.getBoard().isOutside(position)
                || state.getSnake().contains(position);
    }

    private Position getFrontPosition(Position head, Direction direction) {
        return direction.move(head);
    }

    private Position getLeftPosition(Position head, Direction direction) {
        return switch (direction) {
            case UP -> head.translate(0, -1);
            case DOWN -> head.translate(0, 1);
            case LEFT -> head.translate(1, 0);
            case RIGHT -> head.translate(-1, 0);
        };
    }

    private Position getRightPosition(Position head, Direction direction) {
        return switch (direction) {
            case UP -> head.translate(0, 1);
            case DOWN -> head.translate(0, -1);
            case LEFT -> head.translate(-1, 0);
            case RIGHT -> head.translate(1, 0);
        };
    }

    @Override
    public int getInputSize(TrainingConfig config) {
        int visionInputs = config.visionWindowSize() * config.visionWindowSize();

        // dx + dy + distancia + 4 direções
        return visionInputs + 15;
    }
}