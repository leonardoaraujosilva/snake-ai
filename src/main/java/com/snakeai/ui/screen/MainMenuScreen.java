package com.snakeai.ui.screen;

import com.snakeai.persistence.io.TrainingPersistence;
import com.snakeai.persistence.model.TrainingCheckpoint;
import com.snakeai.training.TrainingSession;
import com.snakeai.ui.NavigationController;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class MainMenuScreen extends JPanel {
    private final NavigationController navigationController;

    public MainMenuScreen(NavigationController navigationController) {
        this.navigationController = navigationController;

        setLayout(new GridBagLayout());
        setBackground(new Color(15, 15, 20));

        // Create Title
        JLabel titleLabel = new JLabel("SNAKE AI");
        titleLabel.setFont(new Font("Outfit", Font.BOLD, 48));
        titleLabel.setForeground(new Color(0, 210, 255));

        JLabel subtitleLabel = new JLabel("Evolução Genética e Redes Neurais");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(180, 180, 200));

        // Create Buttons
        JButton btnNewTraining = createStyledButton("Novo Treinamento", new Color(0, 150, 255));
        JButton btnContinueTraining = createStyledButton("Continuar Treinamento", new Color(110, 0, 255));
        JButton btnViewBest = createStyledButton("Visualizar Melhor Indivíduo", new Color(255, 0, 127));
        JButton btnViewReplay = createStyledButton("Visualizar Replay", new Color(0, 200, 115));
        JButton btnExit = createStyledButton("Sair", new Color(70, 70, 80));

        // Button Actions
        btnNewTraining.addActionListener(e -> navigationController.navigateTo("new_training"));

        btnContinueTraining.addActionListener(e -> handleContinueTraining());

        btnViewBest.addActionListener(e -> handleViewBest());

        btnViewReplay.addActionListener(e -> handleViewReplay());

        btnExit.addActionListener(e -> System.exit(0));

        // Layout layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        gbc.gridy = 0;
        add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 40, 10);
        add(subtitleLabel, gbc);

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 2;
        add(btnNewTraining, gbc);

        gbc.gridy = 3;
        add(btnContinueTraining, gbc);

        gbc.gridy = 4;
        add(btnViewBest, gbc);

        gbc.gridy = 5;
        add(btnViewReplay, gbc);

        gbc.gridy = 6;
        add(btnExit, gbc);
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover Effect
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

    private void handleContinueTraining() {
        List<String> list = TrainingPersistence.listAvailableTrainings();
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum treinamento salvo encontrado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Selecione um treinamento para continuar:",
                "Carregar Treinamento",
                JOptionPane.PLAIN_MESSAGE,
                null,
                list.toArray(),
                list.get(0)
        );

        if (selected != null) {
            try {
                TrainingCheckpoint checkpoint = TrainingPersistence.loadCheckpoint(selected);
                TrainingSession session = new TrainingSession(checkpoint);
                
                TrainingScreen screen = (TrainingScreen) navigationController.getMainFrame().getContentPane().getComponent(2); // Wait, components are added by CardLayout, better to pass session to screen directly.
                // Let's implement card system where screens are registered and updated before showing.
                // Let's do it cleanly! We will invoke methods on the screens to load session.
                // We'll update NavigationController to have screen references or do screen updates.
                showTrainingScreen(session);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar o checkpoint: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewBest() {
        List<String> list = TrainingPersistence.listAvailableTrainings();
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum treinamento salvo encontrado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Selecione o treinamento para visualizar o melhor indivíduo:",
                "Visualizar Melhor",
                JOptionPane.PLAIN_MESSAGE,
                null,
                list.toArray(),
                list.get(0)
        );

        if (selected != null) {
            showBestIndividualScreen(selected);
        }
    }

    private void handleViewReplay() {
        List<String> list = TrainingPersistence.listAvailableTrainings();
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum treinamento salvo encontrado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Selecione o treinamento:",
                "Visualizar Replay",
                JOptionPane.PLAIN_MESSAGE,
                null,
                list.toArray(),
                list.get(0)
        );

        if (selected != null) {
            showReplaySelectionScreen(selected);
        }
    }

    private void showTrainingScreen(TrainingSession session) {
        TrainingScreen trainingScreen = (TrainingScreen) navigationController.getScreen("training");
        trainingScreen.setSession(session);
        navigationController.navigateTo("training");
        trainingScreen.startTraining();
    }

    private void showBestIndividualScreen(String trainingName) {
        BestIndividualScreen screen = (BestIndividualScreen) navigationController.getScreen("best_individual");
        screen.loadTraining(trainingName);
        navigationController.navigateTo("best_individual");
    }

    private void showReplaySelectionScreen(String trainingName) {
        ReplayScreen screen = (ReplayScreen) navigationController.getScreen("replay");
        screen.loadTraining(trainingName);
        navigationController.navigateTo("replay");
    }
}
