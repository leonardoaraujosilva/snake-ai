package com.snakeai.ui.chart;

import com.snakeai.training.GenerationSummary;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StatisticsChart extends JPanel {
    private List<GenerationSummary> data;

    public StatisticsChart() {
        this.data = new ArrayList<>();
        setBackground(new Color(30, 30, 40));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
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
        int padding = 40;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;

        // Draw background grid
        g2.setColor(new Color(50, 50, 70));
        g2.drawRect(padding, padding, chartWidth, chartHeight);

        // Draw horizontal grid lines
        for (int i = 1; i < 5; i++) {
            int y = padding + (chartHeight * i / 5);
            g2.drawLine(padding, y, width - padding, y);
        }

        List<GenerationSummary> currentData;
        synchronized (this) {
            currentData = this.data;
        }

        if (currentData.isEmpty()) {
            g2.setColor(Color.WHITE);
            g2.drawString("No data available yet...", width / 2 - 70, height / 2);
            return;
        }

        // Find range
        double maxFitness = currentData.stream()
                .mapToDouble(GenerationSummary::bestFitness)
                .max()
                .orElse(1.0);
        if (maxFitness == 0.0) maxFitness = 1.0;

        int totalGenerations = currentData.size();

        // X scale factor
        double xScale = (double) chartWidth / Math.max(1, totalGenerations - 1);
        double yScale = (double) chartHeight / maxFitness;

        // Draw Axes Labels
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Gen: 0", padding, height - padding + 20);
        g2.drawString("Gen: " + (totalGenerations - 1), width - padding - 40, height - padding + 20);
        g2.drawString(String.format("Max Fit: %.1f", maxFitness), padding, padding - 10);

        // Draw Best Fitness Line (vibrant purple/blue gradient or color)
        g2.setColor(new Color(0, 220, 255));
        g2.setStroke(new BasicStroke(2.5f));

        int prevX = padding;
        int prevY = padding + chartHeight - (int) (currentData.getFirst().bestFitness() * yScale);

        for (int i = 1; i < totalGenerations; i++) {
            int x = padding + (int) (i * xScale);
            int y = padding + chartHeight - (int) (currentData.get(i).bestFitness() * yScale);
            g2.drawLine(prevX, prevY, x, y);
            prevX = x;
            prevY = y;
        }

        // Draw Average Fitness Line (subtle orange/pink)
        g2.setColor(new Color(255, 120, 0));
        g2.setStroke(new BasicStroke(1.5f));

        prevX = padding;
        prevY = padding + chartHeight - (int) (currentData.getFirst().averageFitness() * yScale);

        for (int i = 1; i < totalGenerations; i++) {
            int x = padding + (int) (i * xScale);
            int y = padding + chartHeight - (int) (currentData.get(i).averageFitness() * yScale);
            g2.drawLine(prevX, prevY, x, y);
            prevX = x;
            prevY = y;
        }
    }
}
