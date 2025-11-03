package com.financeportal;

import com.financeportal.dao.DBConnection;
import com.financeportal.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Finance Portal Main Application Entry Point.
 *
 * This class initializes the environment, verifies the database connection,
 * and launches the main login window.
 */
public class App {

    public static void main(String[] args) {
        // Set up a global exception handler (for uncaught exceptions)
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Unexpected error: " + e.getMessage(),
                    "Application Error",
                    JOptionPane.ERROR_MESSAGE);
        });

        // Always start Swing apps on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Set Look and Feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Warning: Could not set system look and feel.");
            }

            // Optional: splash screen or startup message
            showSplashMessage();

            // Verify DB connection before launching GUI
            if (!testDatabaseConnection()) {
                JOptionPane.showMessageDialog(null,
                        "Database connection failed.\nPlease check your MySQL/WAMP server.",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // Launch login form
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }

    /**
     * Test the DB connection briefly.
     * @return true if connection is valid, false otherwise.
     */
    private static boolean testDatabaseConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null || !conn.isValid(2)) {
                return false;
            }
            System.out.println("✅ Database connection established successfully.");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Database test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Displays a simple splash message (optional).
     */
    private static void showSplashMessage() {
        JWindow splash = new JWindow();
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        JLabel label = new JLabel("Loading Finance Portal System...", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        content.add(label, BorderLayout.CENTER);
        splash.getContentPane().add(content);
        splash.setSize(300, 150);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        try {
            Thread.sleep(1200); // Simulate short loading
        } catch (InterruptedException ignored) {
        }

        splash.setVisible(false);
        splash.dispose();
    }
}
