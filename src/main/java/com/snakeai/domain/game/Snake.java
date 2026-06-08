package com.snakeai.domain.game;

import com.snakeai.domain.board.Position;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class Snake {
    private final Deque<Position> body;

    public Snake(Position startingHeadPosition) {
        this.body = new ArrayDeque<>();
        this.body.add(startingHeadPosition);
    }

    public Position getHead() {
        return body.getFirst();
    }

    public Deque<Position> getBody() {
        return new ArrayDeque<>(body);
    }

    public void moveHeadTo(Position newHead) {
        body.addFirst(newHead);
    }

    public void removeTail() {
        body.removeLast();
    }

    public int getLength() {
        return body.size();
    }

    public boolean contains(Position position) {
        return body.contains(position);
    }

    public boolean collidesWithItself() {
        Position head = getHead();
        int occurrences = 0;
        for (Position segment : body) {
            if (segment.equals(head)) {
                occurrences++;
            }
        }
        return occurrences > 1;
    }
}
