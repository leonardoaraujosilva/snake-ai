package com.snakeai.evolution.encoder;

import com.snakeai.config.TrainingConfig;
import com.snakeai.domain.game.GameState;

public interface GameStateEncoder {
    double[] encode(GameState state, TrainingConfig config);
    int getInputSize(TrainingConfig config);
}
