package com.snakeai.domain.game;

import com.snakeai.domain.board.Board;
import com.snakeai.domain.board.Position;

public class GameState {
    private final Board board;
    private final Snake snake;
    private Position food;
    private int score;
    private int stepsSinceLastFood;
    private int totalSteps;
    private boolean gameOver;

    public GameState(Board board, Snake snake, Position food) {
        this.board = board;
        this.snake = snake;
        this.food = food;
        this.score = 0;
        this.stepsSinceLastFood = 0;
        this.totalSteps = 0;
        this.gameOver = false;
    }

    public Board getBoard() {
        return board;
    }

    public Snake getSnake() {
        return snake;
    }

    public Position getFood() {
        return food;
    }

    public void setFood(Position food) {
        this.food = food;
    }

    public int getScore() {
        return score;
    }

    public void incrementScore() {
        this.score++;
    }

    public int getStepsSinceLastFood() {
        return stepsSinceLastFood;
    }

    public void incrementStepsSinceLastFood() {
        this.stepsSinceLastFood++;
    }

    public void resetStepsSinceLastFood() {
        this.stepsSinceLastFood = 0;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void incrementTotalSteps() {
        this.totalSteps++;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}
