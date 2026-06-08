package com.snakeai.replay;

import com.snakeai.domain.board.Position;
import com.snakeai.domain.game.Direction;

import java.util.List;

public record ReplayRecord(
        String trainingName,
        int generation,
        long seed,
        Position initialFoodPosition,
        List<Direction> actionSequence,
        int finalScore
) {}
