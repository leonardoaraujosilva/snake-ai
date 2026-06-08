package com.snakeai.ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class NavigationController {
    private final JFrame mainFrame;
    private final JPanel containerPanel;
    private final CardLayout cardLayout;
    private final Map<String, JPanel> screens;

    public NavigationController(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.cardLayout = new CardLayout();
        this.containerPanel = new JPanel(cardLayout);
        this.screens = new HashMap<>();

        mainFrame.setContentPane(containerPanel);
    }

    public void registerScreen(String name, JPanel screen) {
        screens.put(name, screen);
        containerPanel.add(screen, name);
    }

    public JPanel getScreen(String name) {
        return screens.get(name);
    }

    public void navigateTo(String name) {
        if (!screens.containsKey(name)) {
            throw new IllegalArgumentException("Screen not registered: " + name);
        }
        cardLayout.show(containerPanel, name);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    public JFrame getMainFrame() {
        return mainFrame;
    }
}
