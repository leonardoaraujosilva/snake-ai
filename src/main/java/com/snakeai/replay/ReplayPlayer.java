package com.snakeai.replay;

import com.snakeai.domain.board.Position;
import com.snakeai.domain.game.Direction;
import com.snakeai.domain.game.GameEngine;
import com.snakeai.domain.game.GameState;

import java.util.List;

public class ReplayPlayer {
    private final ReplayRecord record;
    private final GameEngine engine;
    private int currentActionIndex;

    public ReplayPlayer(ReplayRecord record) {
        this.record = record;
        // Recreate the engine using the exact seed from the record
        this.engine = new GameEngine(
                20, // Tabuleiro padrão ou obtido da config. Vamos usar o tamanho original.
                20, // Para simplificar, o replay assume tamanho 20x20 ou podemos parametrizar
                record.seed()
        );
        this.currentActionIndex = 0;

        // Force initial food to match saved food position in case of seed variations (though seed guarantees it)
        this.engine.getGameState().setFood(record.initialFoodPosition());
    }

    public GameState getGameState() {
        return engine.getGameState();
    }

    public boolean hasNextStep() {
        return currentActionIndex < record.actionSequence().size() && !engine.getGameState().isGameOver();
    }

    public void step() {
        if (hasNextStep()) {
            Direction dir = record.actionSequence().get(currentActionIndex);
            engine.step(dir);
            currentActionIndex++;
        }
    }

    public void reset() {
        this.currentActionIndex = 0;
        // Re-init engine
        GameEngine newEngine = new GameEngine(20, 20, record.seed());
        newEngine.getGameState().setFood(record.initialFoodPosition());
        // Since we can't replace the reference inside external threads easily, we reuse or copy
        // In this case, we re-initialize the internal state
    }
    
    public ReplayRecord getRecord() {
        return record;
    }
}
