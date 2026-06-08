package com.snakeai.ui.screen;

import com.snakeai.persistence.io.TrainingPersistence;
import com.snakeai.replay.ReplayPersistence;
import com.snakeai.replay.ReplayPlayer;
import com.snakeai.replay.ReplayRecord;
import com.snakeai.ui.NavigationController;
import com.snakeai.ui.panel.GameBoardPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReplayScreen extends JPanel {
    private final NavigationController navigationController;
    private ReplayPlayer player;
    private String trainingName;

    private GameBoardPanel boardPanel;
    private JComboBox<String> replaySelector;
    private Timer replayTimer;
    private int currentSpeedMs = 150;

    private JLabel lblScore;
    private JLabel lblSteps;
    private JButton btnPlayPause;
    private JSlider speedSlider;

    public ReplayScreen(NavigationController navigationController) {
        this.navigationController = navigationController;

        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Visualizador de Replays");
        titleLabel.setFont(new Font("Outfit", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 210, 255));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Selector panel inside header
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        selectorPanel.setOpaque(false);
        JLabel lblChoose = new JLabel("Selecione o Replay:");
        lblChoose.setForeground(Color.WHITE);
        lblChoose.setFont(new Font("Inter", Font.PLAIN, 14));
        selectorPanel.add(lblChoose);

        replaySelector = new JComboBox<>();
        replaySelector.setFont(new Font("Inter", Font.PLAIN, 14));
        replaySelector.setBackground(new Color(30, 30, 40));
        replaySelector.setForeground(Color.WHITE);
        replaySelector.addActionListener(e -> handleReplaySelected());
        selectorPanel.add(replaySelector);
        headerPanel.add(selectorPanel, BorderLayout.CENTER);

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
        btnReset.addActionListener(e -> resetReplay());
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

        speedSlider = new JSlider(20, 500, 150);
        speedSlider.setOpaque(false);
        speedSlider.addChangeListener(e -> {
            currentSpeedMs = speedSlider.getValue();
            if (replayTimer != null) {
                replayTimer.setDelay(currentSpeedMs);
            }
        });
        speedPanel.add(speedSlider);
        controls.add(speedPanel);

        add(controls, BorderLayout.SOUTH);

        // Setup timer
        replayTimer = new Timer(currentSpeedMs, e -> replayStep());
    }

    public void loadTraining(String name) {
        this.trainingName = name;
        replaySelector.removeAllItems();

        Path replayDir = TrainingPersistence.getReplayDirectory(name);
        if (!Files.exists(replayDir)) {
            JOptionPane.showMessageDialog(this, "Nenhum replay encontrado para este treinamento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try (Stream<Path> stream = Files.list(replayDir)) {
            List<String> files = stream
                    .filter(path -> path.getFileName().toString().endsWith(".replay"))
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());

            for (String file : files) {
                replaySelector.addItem(file);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao listar replays: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReplaySelected() {
        String selectedFile = (String) replaySelector.getSelectedItem();
        if (selectedFile == null) return;

        stopTimer();

        try {
            Path file = TrainingPersistence.getReplayDirectory(trainingName).resolve(selectedFile);
            ReplayRecord record = ReplayPersistence.loadReplay(file);
            player = new ReplayPlayer(record);

            boardPanel.updateState(player.getGameState());
            updateStats();

            btnPlayPause.setText("Play");
            btnPlayPause.setBackground(new Color(0, 200, 115));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar o arquivo de replay: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetReplay() {
        if (player == null) return;
        stopTimer();
        player = new ReplayPlayer(player.getRecord());
        boardPanel.updateState(player.getGameState());
        updateStats();

        btnPlayPause.setText("Play");
        btnPlayPause.setBackground(new Color(0, 200, 115));
    }

    private void replayStep() {
        if (player == null) return;

        if (!player.hasNextStep()) {
            stopTimer();
            JOptionPane.showMessageDialog(this, "Replay Finalizado!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        player.step();
        boardPanel.updateState(player.getGameState());
        updateStats();
    }

    private void togglePlayPause() {
        if (replayTimer.isRunning()) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        replayTimer.start();
        btnPlayPause.setText("Pause");
        btnPlayPause.setBackground(new Color(255, 65, 54));
    }

    private void stopTimer() {
        replayTimer.stop();
        btnPlayPause.setText("Play");
        btnPlayPause.setBackground(new Color(0, 200, 115));
    }

    private void updateStats() {
        if (player == null) return;
        lblScore.setText("Score: " + player.getGameState().getScore());
        lblSteps.setText("Passos: " + player.getGameState().getTotalSteps());
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
