package com.snakeai.domain.board;

public record Position(int row, int col) {
    public Position translate(int deltaRow, int deltaCol) {
        return new Position(this.row + deltaRow, this.col + deltaCol);
    }
}
