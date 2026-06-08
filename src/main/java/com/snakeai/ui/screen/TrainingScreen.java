package com.snakeai.ui.screen;

import com.snakeai.training.GenerationSummary;
import com.snakeai.training.TrainingSession;
import com.snakeai.training.TrainingSessionListener;
import com.snakeai.ui.NavigationController;
import com.snakeai.ui.chart.StatisticsChart;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrainingScreen extends JPanel implements TrainingSessionListener {
    private final NavigationController navigationController;
    private TrainingSession session;
    private ExecutorService trainingExecutor;

    private JLabel lblTitle;
    private JLabel lblGen;
    private JLabel lblBestFit;
    private JLabel lblAvgFit;
    private JLabel lblBestScore;

    private JButton btnPauseResume;
    private StatisticsChart chart;

    public TrainingScreen(NavigationController navigationController) {
        this.navigationController = navigationController;

        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 20));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        lblTitle = new JLabel("Treinamento em Progresso");
        lblTitle.setFont(new Font("Outfit", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 210, 255));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnBack = createStyledButton("Parar & Voltar", new Color(255, 65, 54));
        btnBack.addActionListener(e -> stopAndGoBack());
        headerPanel.add(btnBack, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Chart area
        chart = new StatisticsChart();
        add(chart, BorderLayout.CENTER);

        // Sidebar / Info Panel
        JPanel sidebar = new JPanel(new GridLayout(6, 1, 10, 10));
        sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebar.setPreferredSize(new Dimension(280, 0));

        lblGen = createStatLabel("Geração: 0");
        lblBestFit = createStatLabel("Melhor Fitness: 0.00");
        lblAvgFit = createStatLabel("Fitness Médio: 0.00");
        lblBestScore = createStatLabel("Melhor Comida: 0");

        sidebar.add(lblGen);
        sidebar.add(lblBestFit);
        sidebar.add(lblAvgFit);
        sidebar.add(lblBestScore);

        btnPauseResume = createStyledButton("Pausar", new Color(0, 200, 115));
        btnPauseResume.addActionListener(e -> togglePauseResume());
        sidebar.add(btnPauseResume);

        add(sidebar, BorderLayout.EAST);
    }

    public void setSession(TrainingSession session) {
        this.session = session;
        this.session.addListener(this);

        lblTitle.setText("Treinamento em Progresso: " + session.getName());
        lblGen.setText("Geração: " + session.getCurrentGeneration());
        lblBestFit.setText(String.format("Melhor Fitness: %.2f", session.getBestFitnessEver()));
        lblAvgFit.setText("Fitness Médio: 0.00");
        lblBestScore.setText(String.format("<html>Melhor Comida: %d<br><span style='font-size:12px;color:#888899'>(Gen: %d)</span></html>", session.getBestScoreEver(), session.getBestScoreGeneration()));
        chart.updateData(session.getStatistics().getHistory());
        btnPauseResume.setText("Pausar");
    }

    public void startTraining() {
        if (session == null) return;
        trainingExecutor = Executors.newSingleThreadExecutor();
        trainingExecutor.submit(session);
    }

    private void togglePauseResume() {
        if (session == null) return;
        if (session.isPaused()) {
            session.setPaused(false);
            btnPauseResume.setText("Pausar");
            btnPauseResume.setBackground(new Color(0, 200, 115));
        } else {
            session.setPaused(true);
            btnPauseResume.setText("Retomar");
            btnPauseResume.setBackground(new Color(255, 120, 0));
        }
    }

    private void stopAndGoBack() {
        if (session != null) {
            session.stop();
        }
        if (trainingExecutor != null) {
            trainingExecutor.shutdownNow();
        }
        navigationController.navigateTo("main_menu");
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.BOLD, 16));
        label.setForeground(new Color(200, 200, 220));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 40, 50), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
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

    @Override
    public void onGenerationCompleted(GenerationSummary summary) {
        SwingUtilities.invokeLater(() -> {
            lblGen.setText("Geração: " + summary.generation());
            lblBestFit.setText(String.format("Melhor Fitness: %.2f", summary.bestFitness()));
            lblAvgFit.setText(String.format("Fitness Médio: %.2f", summary.averageFitness()));
            lblBestScore.setText(String.format("<html>Melhor Comida: %d<br><span style='font-size:12px;color:#888899'>(Gen: %d)</span></html>", session.getBestScoreEver(), session.getBestScoreGeneration()));
            chart.updateData(session.getStatistics().getHistory());
        });
    }

    @Override
    public void onTrainingFinished() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Sessão de treinamento encerrada.", "Treinamento Finalizado", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    @Override
    public void onTrainingError(String message, Throwable throwable) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message + ": " + throwable.getMessage(), "Erro no Treinamento", JOptionPane.ERROR_MESSAGE);
        });
    }
}
