package com.snakeai.ui;

import com.snakeai.ui.screen.*;

import javax.swing.*;
import java.awt.*;

public class SnakeAIApplication {

    public static void main(String[] args) {
        // Use cross platform look and feel to allow custom button background colors
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake AI - Genetic Algorithm & Neural Network Study Case");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 860);
            frame.setMinimumSize(new Dimension(900, 700));
            frame.setLocationRelativeTo(null);

            NavigationController navigation = new NavigationController(frame);

            // Instantiate screens
            MainMenuScreen mainMenu = new MainMenuScreen(navigation);
            NewTrainingScreen newTraining = new NewTrainingScreen(navigation);
            TrainingScreen training = new TrainingScreen(navigation);
            BestIndividualScreen bestIndividual = new BestIndividualScreen(navigation);
            ReplayScreen replay = new ReplayScreen(navigation);

            // Register screens
            navigation.registerScreen("main_menu", mainMenu);
            navigation.registerScreen("new_training", newTraining);
            navigation.registerScreen("training", training);
            navigation.registerScreen("best_individual", bestIndividual);
            navigation.registerScreen("replay", replay);

            // Start on Main Menu
            navigation.navigateTo("main_menu");

            frame.setVisible(true);
        });
    }
}
