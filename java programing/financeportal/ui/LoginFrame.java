package com.financeportal.ui;

import com.financeportal.model.AccountHolder;
import com.financeportal.dao.AccountHolderDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

/**
 * Login window that authenticates a user and opens MainFrame on success.
 */
public class LoginFrame extends JFrame implements ActionListener {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton cancelButton;

    public LoginFrame() {
        setTitle("Finance Portal - Login");
        setSize(380, 180);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 8, 8));
        setResizable(false);

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        add(loginButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        add(cancelButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == loginButton) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter username and password", "Input required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Basic authentication using DAO.
            try {
                AccountHolderDAO dao = new AccountHolderDAO();
                // authenticate expects the plain password and verifies against stored hash
                AccountHolder user = dao.authenticate(username, password);

                if (user == null) {
                    JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Open main application window (ensure MainFrame has a constructor that accepts AccountHolder)
                SwingUtilities.invokeLater(() -> {
                    MainFrame main = new MainFrame(user);
                    main.setVisible(true);
                });
                this.dispose();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (src == cancelButton) {
            usernameField.setText("");
            passwordField.setText("");
        }
    }

    // Quick launcher for manual testing of this frame only
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame f = new LoginFrame();
            f.setVisible(true);
        });
    }
}
