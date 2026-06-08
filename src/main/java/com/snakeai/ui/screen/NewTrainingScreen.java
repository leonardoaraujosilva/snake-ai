package com.snakeai.ui.screen;

import com.snakeai.config.EliteSelectionMode;
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
    private JComboBox<EliteSelectionMode> cmbEliteSelectionMode;
    private JSpinner spinEvaluationsPerIndividual;
    private JSpinner spinEvaluationThreads;
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
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Color textColor = new Color(200, 200, 220);
        Color sectionColor = new Color(0, 180, 220);
        int row = 0;

        // --- Section: Básico ---
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(createSectionLabel("▸ Configuração Básica", sectionColor), gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Nome do Treinamento:", textColor), gbc);
        gbc.gridx = 1;
        txtName = new JTextField("treinamento-novo", 15);
        styleTextField(txtName);
        formPanel.add(txtName, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Tamanho da População:", textColor), gbc);
        gbc.gridx = 1;
        spinPopSize = new JSpinner(new SpinnerNumberModel(1000, 10, 10000, 50));
        styleSpinner(spinPopSize);
        formPanel.add(spinPopSize, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Elitismo (Indivíduos):", textColor), gbc);
        gbc.gridx = 1;
        spinElitismo = new JSpinner(new SpinnerNumberModel(20, 0, 1000, 2));
        styleSpinner(spinElitismo);
        formPanel.add(spinElitismo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Tamanho do Tabuleiro (NxN):", textColor), gbc);
        gbc.gridx = 1;
        spinBoardSize = new JSpinner(new SpinnerNumberModel(20, 5, 50, 1));
        styleSpinner(spinBoardSize);
        formPanel.add(spinBoardSize, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Janela de Visão (Ímpar):", textColor), gbc);
        gbc.gridx = 1;
        spinVisionSize = new JSpinner(new SpinnerNumberModel(11, 3, 25, 2));
        styleSpinner(spinVisionSize);
        formPanel.add(spinVisionSize, gbc);
        row++;

        // --- Section: Mutação ---
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(createSectionLabel("▸ Mutação", sectionColor), gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Taxa de Mutação (%):", textColor), gbc);
        gbc.gridx = 1;
        spinMutationRate = new JSpinner(new SpinnerNumberModel(2.0, 0.1, 100.0, 0.5));
        styleSpinner(spinMutationRate);
        formPanel.add(spinMutationRate, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Amplitude da Mutação:", textColor), gbc);
        gbc.gridx = 1;
        spinMutationAmp = new JSpinner(new SpinnerNumberModel(0.3, 0.01, 5.0, 0.1));
        styleSpinner(spinMutationAmp);
        formPanel.add(spinMutationAmp, gbc);
        row++;

        // --- Section: Evolução ---
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(createSectionLabel("▸ Modo de Evolução", sectionColor), gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Modo de Evolução:", textColor), gbc);
        gbc.gridx = 1;
        cmbEvolutionMode = new JComboBox<>(EvolutionMode.values());
        styleComboBox(cmbEvolutionMode);
        formPanel.add(cmbEvolutionMode, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Seleção de Elite (Mutação Pura):", textColor), gbc);
        gbc.gridx = 1;
        cmbEliteSelectionMode = new JComboBox<>(EliteSelectionMode.values());
        styleComboBox(cmbEliteSelectionMode);
        formPanel.add(cmbEliteSelectionMode, gbc);
        row++;

        // --- Section: Performance ---
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(createSectionLabel("▸ Performance", sectionColor), gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Avaliações por Indivíduo:", textColor), gbc);
        gbc.gridx = 1;
        spinEvaluationsPerIndividual = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        styleSpinner(spinEvaluationsPerIndividual);
        formPanel.add(spinEvaluationsPerIndividual, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        int availableProcs = Runtime.getRuntime().availableProcessors();
        formPanel.add(createLabel("Threads de Avaliação (CPU: " + availableProcs + "):", textColor), gbc);
        gbc.gridx = 1;
        spinEvaluationThreads = new JSpinner(new SpinnerNumberModel(availableProcs, 1, availableProcs * 2, 1));
        styleSpinner(spinEvaluationThreads);
        formPanel.add(spinEvaluationThreads, gbc);
        row++;

        // --- Section: Rede ---
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(createSectionLabel("▸ Rede Neural", sectionColor), gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Camadas Ocultas (separadas por vírgula):", textColor), gbc);
        gbc.gridx = 1;
        txtHiddenLayers = new JTextField("128, 64, 32", 15);
        styleTextField(txtHiddenLayers);
        formPanel.add(txtHiddenLayers, gbc);

        // Wrap the form in a scroll pane so all fields are reachable at any window size
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

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
        label.setFont(new Font("Inter", Font.PLAIN, 13));
        label.setForeground(color);
        return label;
    }

    private JLabel createSectionLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Outfit", Font.BOLD, 14));
        label.setForeground(color);
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBackground(new Color(30, 30, 40));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60)));
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

    private <T> void styleComboBox(JComboBox<T> combo) {
        combo.setFont(new Font("Inter", Font.PLAIN, 14));
        combo.setBackground(new Color(30, 30, 40));
        combo.setForeground(Color.WHITE);
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

        int visionSize = (Integer) spinVisionSize.getValue();
        if (visionSize % 2 == 0) {
            JOptionPane.showMessageDialog(this, "O tamanho da janela de visão deve ser um número ímpar (ex: 11).", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
                (EliteSelectionMode) cmbEliteSelectionMode.getSelectedItem(),
                (Integer) spinEvaluationsPerIndividual.getValue(),
                (Integer) spinEvaluationThreads.getValue(),
                layers
        );

        TrainingSession session = new TrainingSession(name, config);

        TrainingScreen trainingScreen = (TrainingScreen) navigationController.getScreen("training");
        trainingScreen.setSession(session);
        navigationController.navigateTo("training");
        trainingScreen.startTraining();
    }
}
