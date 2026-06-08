package com.snakeai.domain.board;

import java.util.Random;

public class Board {
    private final int width;
    private final int height;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isOutside(Position position) {
        return position.row() < 0 || position.row() >= height || position.col() < 0 || position.col() >= width;
    }

    public Position generateRandomFoodPosition(Random random) {
        return new Position(random.nextInt(height), random.nextInt(width));
    }
}
