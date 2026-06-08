package com.snakeai.ui.panel;

import com.snakeai.domain.board.Position;
import com.snakeai.domain.game.GameState;

import javax.swing.*;
import java.awt.*;

public class GameBoardPanel extends JPanel {
    private GameState gameState;

    public GameBoardPanel() {
        setBackground(new Color(20, 20, 25));
        setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60), 2));
    }

    public void updateState(GameState state) {
        this.gameState = state;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameState == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int rows = gameState.getBoard().getHeight();
        int cols = gameState.getBoard().getWidth();

        int cellWidth = getWidth() / cols;
        int cellHeight = getHeight() / rows;

        // Draw grid boundaries
        g2.setColor(new Color(30, 30, 35));
        for (int r = 0; r <= rows; r++) {
            g2.drawLine(0, r * cellHeight, getWidth(), r * cellHeight);
        }
        for (int c = 0; c <= cols; c++) {
            g2.drawLine(c * cellWidth, 0, c * cellWidth, getHeight());
        }

        // Draw Food (vibrant red/pink)
        Position food = gameState.getFood();
        if (food != null) {
            g2.setColor(new Color(255, 65, 54));
            g2.fillOval(
                    food.col() * cellWidth + 2,
                    food.row() * cellHeight + 2,
                    cellWidth - 4,
                    cellHeight - 4
            );
        }

        // Draw Snake body (softer emerald/green)
        g2.setColor(new Color(46, 204, 113));
        for (Position bodyPart : gameState.getSnake().getBody()) {
            if (bodyPart.equals(gameState.getSnake().getHead())) {
                continue; // Draw head separately
            }
            g2.fillRoundRect(
                    bodyPart.col() * cellWidth + 1,
                    bodyPart.row() * cellHeight + 1,
                    cellWidth - 2,
                    cellHeight - 2,
                    8,
                    8
            );
        }

        // Draw Snake Head (bright electric green/teal)
        Position head = gameState.getSnake().getHead();
        if (head != null) {
            g2.setColor(new Color(26, 188, 156));
            g2.fillRoundRect(
                    head.col() * cellWidth + 1,
                    head.row() * cellHeight + 1,
                    cellWidth - 2,
                    cellHeight - 2,
                    12,
                    12
            );

            // Draw small eyes for indication
            g2.setColor(Color.BLACK);
            int eyeSize = Math.max(3, cellWidth / 6);
            g2.fillOval(
                    head.col() * cellWidth + cellWidth / 4 - eyeSize / 2,
                    head.row() * cellHeight + cellHeight / 3 - eyeSize / 2,
                    eyeSize,
                    eyeSize
            );
            g2.fillOval(
                    head.col() * cellWidth + 3 * cellWidth / 4 - eyeSize / 2,
                    head.row() * cellHeight + cellHeight / 3 - eyeSize / 2,
                    eyeSize,
                    eyeSize
            );
        }
    }
}
