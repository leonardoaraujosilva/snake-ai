package com.snakeai.ui.screen;

import com.snakeai.config.TrainingConfig;
import com.snakeai.domain.board.Position;
import com.snakeai.domain.game.Direction;
import com.snakeai.domain.game.GameEngine;
import com.snakeai.domain.game.GameState;
import com.snakeai.evolution.agent.SnakeAgent;
import com.snakeai.evolution.encoder.GameStateEncoder;
import com.snakeai.evolution.encoder.HeadCenteredLocalVisionEncoder;
import com.snakeai.neural.network.NeuralNetwork;
import com.snakeai.neural.network.NeuralNetworkFactory;
import com.snakeai.persistence.io.TrainingPersistence;
import com.snakeai.persistence.model.SavedEliteIndividual;
import com.snakeai.persistence.model.TrainingCheckpoint;
import com.snakeai.ui.NavigationController;
import com.snakeai.ui.panel.GameBoardPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

public class BestIndividualScreen extends JPanel {
    private final NavigationController navigationController;
    private TrainingCheckpoint checkpoint;
    private GameEngine engine;
    private SnakeAgent agent;
    private GameStateEncoder encoder;
    
    private GameBoardPanel boardPanel;
    private Timer gameTimer;
    private int currentSpeedMs = 100;

    private JLabel lblScore;
    private JLabel lblSteps;
    private JButton btnPlayPause;
    private JSlider speedSlider;

    public BestIndividualScreen(NavigationController navigationController) {
        this.navigationController = navigationController;
        this.encoder = new HeadCenteredLocalVisionEncoder();

        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Melhor Indivíduo Salvo");
        titleLabel.setFont(new Font("Outfit", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 210, 255));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton btnBack = createStyledButton("Voltar", new Color(70, 70, 80));
        btnBack.addActionListener(e -> stopAndGoBack());
        headerPanel.add(btnBack, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Game Board Panel
        boardPanel = new GameBoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // Controls Panel (Bottom)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controls.setOpaque(false);
        controls.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        btnPlayPause = createStyledButton("Play", new Color(0, 200, 115));
        btnPlayPause.addActionListener(e -> togglePlayPause());
        controls.add(btnPlayPause);

        JButton btnReset = createStyledButton("Reiniciar", new Color(255, 120, 0));
        btnReset.addActionListener(e -> resetGame());
        controls.add(btnReset);

        lblScore = createStatLabel("Score: 0");
        lblSteps = createStatLabel("Passos: 0");
        controls.add(lblScore);
        controls.add(lblSteps);

        // Speed Slider
        JPanel speedPanel = new JPanel(new FlowLayout());
        speedPanel.setOpaque(false);
        JLabel lblSpeed = new JLabel("Velocidade (ms):");
        lblSpeed.setForeground(Color.WHITE);
        speedPanel.add(lblSpeed);

        speedSlider = new JSlider(20, 500, 100);
        speedSlider.setOpaque(false);
        speedSlider.addChangeListener(e -> {
            currentSpeedMs = speedSlider.getValue();
            if (gameTimer != null) {
                gameTimer.setDelay(currentSpeedMs);
            }
        });
        speedPanel.add(speedSlider);
        controls.add(speedPanel);

        add(controls, BorderLayout.SOUTH);

        // Setup timer loop
        gameTimer = new Timer(currentSpeedMs, e -> gameLoopStep());
    }

    public void loadTraining(String name) {
        try {
            checkpoint = TrainingPersistence.loadCheckpoint(name);
            resetGame();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar o checkpoint: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetGame() {
        if (checkpoint == null) return;
        stopTimer();

        TrainingConfig config = checkpoint.metadata().config();

        // Create random engine
        long seed = new Random().nextLong();
        engine = new GameEngine(config.boardWidth(), config.boardHeight(), seed);

        // Load the best individual's brain
        SavedEliteIndividual best = checkpoint.elite().getFirst();
        NeuralNetwork brain = NeuralNetworkFactory.createNetwork(
                encoder.getInputSize(config),
                config.hiddenLayerSizes(),
                4
        );
        brain.setWeights(best.genome());
        agent = new SnakeAgent(brain, encoder, config);

        boardPanel.updateState(engine.getGameState());
        updateStats();

        btnPlayPause.setText("Play");
        btnPlayPause.setBackground(new Color(0, 200, 115));
    }

    private void gameLoopStep() {
        if (engine == null || agent == null) return;
        GameState state = engine.getGameState();

        if (state.isGameOver()) {
            stopTimer();
            JOptionPane.showMessageDialog(this, "Fim de jogo! Score final: " + state.getScore(), "Partida Encerrada", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Direction nextDir = agent.chooseDirection(state);
        engine.step(nextDir);

        boardPanel.updateState(state);
        updateStats();
    }

    private void togglePlayPause() {
        if (gameTimer.isRunning()) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        gameTimer.start();
        btnPlayPause.setText("Pause");
        btnPlayPause.setBackground(new Color(255, 65, 54));
    }

    private void stopTimer() {
        gameTimer.stop();
        btnPlayPause.setText("Play");
        btnPlayPause.setBackground(new Color(0, 200, 115));
    }

    private void updateStats() {
        if (engine == null) return;
        lblScore.setText("Score: " + engine.getGameState().getScore());
        lblSteps.setText("Passos: " + engine.getGameState().getTotalSteps());
    }

    private void stopAndGoBack() {
        stopTimer();
        navigationController.navigateTo("main_menu");
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
            }
        });
        return button;
    }
}
