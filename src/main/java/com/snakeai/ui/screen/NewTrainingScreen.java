package com.snakeai.ui.screen;

import com.snakeai.config.EvolutionMode;
import com.snakeai.config.TrainingConfig;
import com.snakeai.training.TrainingSession;
import com.snakeai.ui.NavigationController;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NewTrainingScreen extends JPanel {
    private final NavigationController navigationController;

    private JTextField txtName;
    private JSpinner spinPopSize;
    private JSpinner spinElitismo;
    private JSpinner spinMutationRate;
    private JSpinner spinMutationAmp;
    private JSpinner spinBoardSize;
    private JSpinner spinVisionSize;
    private JComboBox<EvolutionMode> cmbEvolutionMode;
    private JTextField txtHiddenLayers;

    public NewTrainingScreen(NavigationController navigationController) {
        this.navigationController = navigationController;

        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        JButton btnBack = createStyledButton("← Voltar", new Color(70, 70, 80));
        btnBack.addActionListener(e -> navigationController.navigateTo("main_menu"));
        headerPanel.add(btnBack);

        JLabel lblTitle = new JLabel("Novo Treinamento");
        lblTitle.setFont(new Font("Outfit", Font.BOLD, 28));
        lblTitle.setForeground(new Color(0, 210, 255));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Colors
        Color textColor = new Color(200, 200, 220);

        // Add inputs
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Nome do Treinamento:", textColor), gbc);
        gbc.gridx = 1;
        txtName = new JTextField("treinamento-novo", 15);
        txtName.setFont(new Font("Inter", Font.PLAIN, 14));
        txtName.setBackground(new Color(30, 30, 40));
        txtName.setForeground(Color.WHITE);
        txtName.setCaretColor(Color.WHITE);
        txtName.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60)));
        formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Tamanho da População:", textColor), gbc);
        gbc.gridx = 1;
        spinPopSize = new JSpinner(new SpinnerNumberModel(1000, 10, 10000, 50));
        styleSpinner(spinPopSize);
        formPanel.add(spinPopSize, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Elitismo (Indivíduos):", textColor), gbc);
        gbc.gridx = 1;
        spinElitismo = new JSpinner(new SpinnerNumberModel(20, 0, 1000, 2));
        styleSpinner(spinElitismo);
        formPanel.add(spinElitismo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Taxa de Mutação (%):", textColor), gbc);
        gbc.gridx = 1;
        spinMutationRate = new JSpinner(new SpinnerNumberModel(2.0, 0.1, 100.0, 0.5));
        styleSpinner(spinMutationRate);
        formPanel.add(spinMutationRate, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createLabel("Amplitude da Mutação:", textColor), gbc);
        gbc.gridx = 1;
        spinMutationAmp = new JSpinner(new SpinnerNumberModel(0.3, 0.01, 5.0, 0.1));
        styleSpinner(spinMutationAmp);
        formPanel.add(spinMutationAmp, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createLabel("Tamanho do Tabuleiro (NxN):", textColor), gbc);
        gbc.gridx = 1;
        spinBoardSize = new JSpinner(new SpinnerNumberModel(20, 5, 50, 1));
        styleSpinner(spinBoardSize);
        formPanel.add(spinBoardSize, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(createLabel("Tamanho da Janela de Visão (Ímpar):", textColor), gbc);
        gbc.gridx = 1;
        spinVisionSize = new JSpinner(new SpinnerNumberModel(11, 3, 25, 2));
        styleSpinner(spinVisionSize);
        formPanel.add(spinVisionSize, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(createLabel("Modo de Evolução:", textColor), gbc);
        gbc.gridx = 1;
        cmbEvolutionMode = new JComboBox<>(EvolutionMode.values());
        cmbEvolutionMode.setFont(new Font("Inter", Font.PLAIN, 14));
        cmbEvolutionMode.setBackground(new Color(30, 30, 40));
        cmbEvolutionMode.setForeground(Color.WHITE);
        formPanel.add(cmbEvolutionMode, gbc);

        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(createLabel("Rede Oculta (Camadas separadas por vírgula):", textColor), gbc);
        gbc.gridx = 1;
        txtHiddenLayers = new JTextField("128, 64, 32", 15);
        txtHiddenLayers.setFont(new Font("Inter", Font.PLAIN, 14));
        txtHiddenLayers.setBackground(new Color(30, 30, 40));
        txtHiddenLayers.setForeground(Color.WHITE);
        txtHiddenLayers.setCaretColor(Color.WHITE);
        txtHiddenLayers.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60)));
        formPanel.add(txtHiddenLayers, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Footer / Button Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        JButton btnStart = createStyledButton("Iniciar Treinamento", new Color(0, 210, 255));
        btnStart.addActionListener(e -> startTrainingAction());
        footerPanel.add(btnStart);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.BOLD, 14));
        label.setForeground(color);
        return label;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Inter", Font.PLAIN, 14));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            JTextField textField = defaultEditor.getTextField();
            textField.setBackground(new Color(30, 30, 40));
            textField.setForeground(Color.WHITE);
            textField.setCaretColor(Color.WHITE);
        }
        spinner.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60)));
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
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

    private void startTrainingAction() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, especifique um nome para o treinamento.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate vision size odd
        int visionSize = (Integer) spinVisionSize.getValue();
        if (visionSize % 2 == 0) {
            JOptionPane.showMessageDialog(this, "O tamanho da janela de visão deve ser um número ímpar (ex: 11).", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Parse layers
        String layersStr = txtHiddenLayers.getText();
        List<Integer> layers = new ArrayList<>();
        try {
            for (String part : layersStr.split(",")) {
                layers.add(Integer.parseInt(part.trim()));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Formato incorreto para camadas ocultas. Exemplo válido: 128, 64, 32", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Gather config
        TrainingConfig config = new TrainingConfig(
                (Integer) spinPopSize.getValue(),
                (Integer) spinElitismo.getValue(),
                5, // Tournament size
                (Double) spinMutationRate.getValue() / 100.0,
                (Double) spinMutationAmp.getValue(),
                (Integer) spinBoardSize.getValue(),
                (Integer) spinBoardSize.getValue(),
                visionSize,
                (EvolutionMode) cmbEvolutionMode.getSelectedItem(),
                layers
        );

        TrainingSession session = new TrainingSession(name, config);

        // Navigate to training screen and start it
        TrainingScreen trainingScreen = (TrainingScreen) navigationController.getScreen("training");
        trainingScreen.setSession(session);
        navigationController.navigateTo("training");
        trainingScreen.startTraining();
    }
}
