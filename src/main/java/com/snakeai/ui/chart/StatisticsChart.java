package com.snakeai.ui.chart;

import com.snakeai.training.GenerationSummary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

public class StatisticsChart extends JPanel {
    private List<GenerationSummary> data;

    // Visibility toggles
    private boolean showBestFitness = true;
    private boolean showEliteFitness = true;
    private boolean showAvgFitness = true;
    private boolean showBestFitnessEver = true;
    private boolean showBestScore = true;

    // Hitboxes for legend
    private final Rectangle rectBestFitness = new Rectangle();
    private final Rectangle rectEliteFitness = new Rectangle();
    private final Rectangle rectAvgFitness = new Rectangle();
    private final Rectangle rectBestFitnessEver = new Rectangle();
    private final Rectangle rectBestScore = new Rectangle();

    public StatisticsChart() {
        this.data = new ArrayList<>();
        setBackground(new Color(30, 30, 40));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Interaction logic for legend
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (rectBestFitness.contains(p)) showBestFitness = !showBestFitness;
                else if (rectEliteFitness.contains(p)) showEliteFitness = !showEliteFitness;
                else if (rectAvgFitness.contains(p)) showAvgFitness = !showAvgFitness;
                else if (rectBestFitnessEver.contains(p)) showBestFitnessEver = !showBestFitnessEver;
                else if (rectBestScore.contains(p)) showBestScore = !showBestScore;
                else return; // clicked elsewhere
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                if (rectBestFitness.contains(p) || rectEliteFitness.contains(p) || rectAvgFitness.contains(p) || 
                    rectBestFitnessEver.contains(p) || rectBestScore.contains(p)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    public synchronized void updateData(List<GenerationSummary> newData) {
        this.data = new ArrayList<>(newData);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int paddingLeft = 60;
        int paddingRight = 20;
        int paddingTop = 50;
        int paddingBottom = 50;
        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        // Draw chart background
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(paddingLeft, paddingTop, chartWidth, chartHeight);
        g2.setColor(new Color(50, 50, 70));
        g2.drawRect(paddingLeft, paddingTop, chartWidth, chartHeight);

        // Draw horizontal grid lines
        g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[]{4}, 0));
        g2.setColor(new Color(50, 50, 70));
        for (int i = 1; i < 5; i++) {
            int y = paddingTop + (chartHeight * i / 5);
            g2.drawLine(paddingLeft, y, paddingLeft + chartWidth, y);
        }

        List<GenerationSummary> currentData;
        synchronized (this) {
            currentData = this.data;
        }

        if (currentData.isEmpty()) {
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(Color.WHITE);
            g2.drawString("Aguardando dados...", paddingLeft + chartWidth / 2 - 60, paddingTop + chartHeight / 2);
            drawLegend(g2, width, paddingTop);
            return;
        }

        // Find the max value across all series for Y scale
        double maxFitness = currentData.stream()
                .mapToDouble(s -> Math.max(s.bestFitness(), s.bestFitnessEver()))
                .max()
                .orElse(1.0);
        if (maxFitness == 0.0) maxFitness = 1.0;

        // Separate max for Score (food), since it has a different scale from fitness
        int maxScore = currentData.stream()
                .mapToInt(GenerationSummary::bestScore)
                .max()
                .orElse(1);
        if (maxScore == 0) maxScore = 1;

        int totalGenerations = currentData.size();
        double xScale = (double) chartWidth / Math.max(1, totalGenerations - 1);
        double yScale = (double) chartHeight / maxFitness;
        double scoreYScale = (double) chartHeight / maxScore;

        // Draw Y axis labels
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(new Font("Inter", Font.PLAIN, 10));
        g2.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= 5; i++) {
            double value = maxFitness * i / 5.0;
            int y = paddingTop + chartHeight - (int) (value * yScale);
            g2.drawString(String.format("%.0f", value), 5, y + 4);
        }

        // Draw X axis labels
        g2.drawString("Gen: 0", paddingLeft, height - paddingBottom + 18);
        g2.drawString("Gen: " + (totalGenerations - 1), paddingLeft + chartWidth - 40, height - paddingBottom + 18);

        // --- Line 3: Best Fitness Ever (historical record, monotonically increasing) ---
        if (showBestFitnessEver) {
            g2.setColor(new Color(160, 80, 255));  // Purple
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 3}, 0));
            int prevX = paddingLeft;
            int prevY = paddingTop + chartHeight - (int) (currentData.getFirst().bestFitnessEver() * yScale);
            for (int i = 1; i < totalGenerations; i++) {
                int x = paddingLeft + (int) (i * xScale);
                int y = paddingTop + chartHeight - (int) (currentData.get(i).bestFitnessEver() * yScale);
                g2.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }

        // --- Line 5: Elite Average Fitness ---
        if (showEliteFitness) {
            g2.setColor(new Color(255, 200, 50));  // Yellow/Gold
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 3}, 0));
            int prevX = paddingLeft;
            int prevY = paddingTop + chartHeight - (int) (currentData.getFirst().averageEliteFitness() * yScale);
            for (int i = 1; i < totalGenerations; i++) {
                int x = paddingLeft + (int) (i * xScale);
                int y = paddingTop + chartHeight - (int) (currentData.get(i).averageEliteFitness() * yScale);
                g2.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }

        // --- Line 2: Average Fitness ---
        if (showAvgFitness) {
            g2.setColor(new Color(255, 140, 0));  // Orange
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int prevX = paddingLeft;
            int prevY = paddingTop + chartHeight - (int) (currentData.getFirst().averageFitness() * yScale);
            for (int i = 1; i < totalGenerations; i++) {
                int x = paddingLeft + (int) (i * xScale);
                int y = paddingTop + chartHeight - (int) (currentData.get(i).averageFitness() * yScale);
                g2.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }

        // --- Line 1: Best Fitness of the Generation ---
        if (showBestFitness) {
            g2.setColor(new Color(0, 220, 255));  // Cyan
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int prevX = paddingLeft;
            int prevY = paddingTop + chartHeight - (int) (currentData.getFirst().bestFitness() * yScale);
            for (int i = 1; i < totalGenerations; i++) {
                int x = paddingLeft + (int) (i * xScale);
                int y = paddingTop + chartHeight - (int) (currentData.get(i).bestFitness() * yScale);
                g2.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }

        // --- Line 4: Best Score (Comidas) of the Generation ---
        if (showBestScore) {
            g2.setColor(new Color(0, 255, 120));  // Green
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int prevX = paddingLeft;
            int prevY = paddingTop + chartHeight - (int) (currentData.getFirst().bestScore() * scoreYScale);
            for (int i = 1; i < totalGenerations; i++) {
                int x = paddingLeft + (int) (i * xScale);
                int y = paddingTop + chartHeight - (int) (currentData.get(i).bestScore() * scoreYScale);
                g2.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }

        drawLegend(g2, width, paddingTop);
    }

    private void drawLegend(Graphics2D g2, int width, int paddingTop) {
        g2.setFont(new Font("Inter", Font.BOLD, 11));
        
        int legendX = 65;  // just inside the Y-axis
        int legendY = paddingTop + 10;
        int lineLen = 20;
        int lineSpacing = 18;
        int hitWidth = 180;
        int hitHeight = 14;

        // Helper method to setup color based on visibility
        Color activeCyan = new Color(0, 220, 255);
        Color activeOrange = new Color(255, 140, 0);
        Color activePurple = new Color(160, 80, 255);
        Color activeGreen = new Color(0, 255, 120);
        Color disabledColor = new Color(80, 80, 90);
        Color textDisabledColor = new Color(100, 100, 110);
        Color textActiveColor = Color.WHITE;

        // Best Fitness (generation)
        rectBestFitness.setBounds(legendX, legendY - 8, hitWidth, hitHeight);
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(showBestFitness ? activeCyan : disabledColor);
        g2.drawLine(legendX, legendY, legendX + lineLen, legendY);
        g2.setColor(showBestFitness ? textActiveColor : textDisabledColor);
        g2.drawString("Melhor Fitness (Geração)", legendX + lineLen + 6, legendY + 4);

        // Average Fitness
        legendY += lineSpacing;
        rectAvgFitness.setBounds(legendX, legendY - 8, hitWidth, hitHeight);
        g2.setColor(showAvgFitness ? activeOrange : disabledColor);
        g2.drawLine(legendX, legendY, legendX + lineLen, legendY);
        g2.setColor(showAvgFitness ? textActiveColor : textDisabledColor);
        g2.drawString("Fitness Médio", legendX + lineLen + 6, legendY + 4);

        // Elite Fitness
        legendY += lineSpacing;
        rectEliteFitness.setBounds(legendX, legendY - 8, hitWidth, hitHeight);
        g2.setColor(showEliteFitness ? new Color(255, 200, 50) : disabledColor);
        float[] eliteDash = {3f, 3f};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, eliteDash, 0));
        g2.drawLine(legendX, legendY, legendX + lineLen, legendY);
        g2.setColor(showEliteFitness ? textActiveColor : textDisabledColor);
        g2.drawString("Fitness Médio (Elite)", legendX + lineLen + 6, legendY + 4);

        // Best Fitness Ever
        legendY += lineSpacing;
        rectBestFitnessEver.setBounds(legendX, legendY - 8, hitWidth, hitHeight);
        g2.setColor(showBestFitnessEver ? activePurple : disabledColor);
        float[] dash = {6f, 3f};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, dash, 0));
        g2.drawLine(legendX, legendY, legendX + lineLen, legendY);
        g2.setColor(showBestFitnessEver ? textActiveColor : textDisabledColor);
        g2.drawString("Melhor Fitness Histórico", legendX + lineLen + 6, legendY + 4);

        // Best Score
        legendY += lineSpacing;
        rectBestScore.setBounds(legendX, legendY - 8, hitWidth, hitHeight);
        g2.setColor(showBestScore ? activeGreen : disabledColor);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(legendX, legendY, legendX + lineLen, legendY);
        g2.setColor(showBestScore ? textActiveColor : textDisabledColor);
        g2.drawString("Maior Quant. Comidas", legendX + lineLen + 6, legendY + 4);
    }
}
