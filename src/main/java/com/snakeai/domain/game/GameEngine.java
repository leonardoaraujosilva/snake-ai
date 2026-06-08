package com.snakeai.domain.game;

import com.snakeai.domain.board.Board;
import com.snakeai.domain.board.Position;

import java.util.*;

public class GameEngine {
    private final GameState gameState;
    private final Random random;
    private final Set<String> stateHistory;
    private boolean cycleDetected;
    private boolean timeLimitExceeded;
    private boolean cycleDetectionEnabled = true;
    private boolean timeLimitEnabled = true;

    public GameEngine(int boardWidth, int boardHeight, long seed) {
        this.random = new Random(seed);
        this.stateHistory = new HashSet<>();
        this.cycleDetected = false;
        this.timeLimitExceeded = false;

        Board board = new Board(boardWidth, boardHeight);
        Position startingHead = new Position(boardHeight / 2, boardWidth / 2);
        Snake snake = new Snake(startingHead);
        Position food = generateValidFoodPosition(board, snake);

        this.gameState = new GameState(board, snake, food);
        recordState();
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean isCycleDetected() {
        return cycleDetected;
    }

    public boolean isTimeLimitExceeded() {
        return timeLimitExceeded;
    }

    public void setCycleDetectionEnabled(boolean enabled) {
        this.cycleDetectionEnabled = enabled;
    }

    public void setTimeLimitEnabled(boolean enabled) {
        this.timeLimitEnabled = enabled;
    }

    public void step(Direction direction) {
        if (gameState.isGameOver()) {
            return;
        }

        gameState.incrementTotalSteps();
        gameState.incrementStepsSinceLastFood();

        if (timeLimitEnabled && hasExceededTimeLimit()) {
            timeLimitExceeded = true;
            gameState.setGameOver(true);
            return;
        }

        Snake snake = gameState.getSnake();
        Board board = gameState.getBoard();

        Position newHead = direction.move(snake.getHead());

        if (board.isOutside(newHead)) {
            gameState.setGameOver(true);
            return;
        }

        snake.moveHeadTo(newHead);

        if (snake.collidesWithItself()) {
            gameState.setGameOver(true);
            return;
        }

        if (newHead.equals(gameState.getFood())) {
            gameState.incrementScore();
            gameState.resetStepsSinceLastFood();
            Position nextFood = generateValidFoodPosition(board, snake);
            gameState.setFood(nextFood);
            stateHistory.clear();
        } else {
            snake.removeTail();
        }

        if (!gameState.isGameOver()) {
            recordStateAndCheckCycle();
        }
    }

    private boolean hasExceededTimeLimit() {
        int limit = 100 + (gameState.getSnake().getLength() * 50);
        return gameState.getStepsSinceLastFood() > limit;
    }

    private Position generateValidFoodPosition(Board board, Snake snake) {
        Position food = board.generateRandomFoodPosition(random);
        int maxAttempts = board.getWidth() * board.getHeight() * 2;
        int attempts = 0;
        while (snake.contains(food) && attempts < maxAttempts) {
            food = board.generateRandomFoodPosition(random);
            attempts++;
        }
        return food;
    }

    private void recordState() {
        stateHistory.add(buildStateString());
    }

    private void recordStateAndCheckCycle() {
        if (!cycleDetectionEnabled) return;
        
        String signature = buildStateString();
        if (stateHistory.contains(signature)) {
            cycleDetected = true;
            gameState.setGameOver(true);
        } else {
            stateHistory.add(signature);
        }
    }

    private String buildStateString() {
        StringBuilder sb = new StringBuilder();
        sb.append(gameState.getSnake().getHead().row()).append(",").append(gameState.getSnake().getHead().col());
        sb.append("|");
        sb.append(gameState.getFood().row()).append(",").append(gameState.getFood().col());
        sb.append("|");
        for (Position segment : gameState.getSnake().getBody()) {
            sb.append(segment.row()).append(",").append(segment.col()).append(";");
        }
        return sb.toString();
    }
}
