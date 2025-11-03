package com.financeportal.ui;

import com.financeportal.model.AccountHolder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Main application window that hosts all feature panels.
 */
public class MainFrame extends JFrame {

    private final AccountHolder currentUser;
    private final JPanel contentPanel;
    private final JLabel userLabel;

    public MainFrame(AccountHolder user) {
        if (user == null) throw new IllegalArgumentException("user must not be null");
        this.currentUser = user;

        setTitle("Finance Portal - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Top bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(60, 63, 65));
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        userLabel = new JLabel("Logged in as: " + (user.getFullName() == null ? user.getUsername() : user.getFullName()) + " (" + user.getRole() + ")");
        userLabel.setForeground(Color.WHITE);
        topPanel.add(userLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(this::onLogout);
        topPanel.add(logoutButton, BorderLayout.EAST);

        // Sidebar menu
        JPanel sidePanel = new JPanel(new GridLayout(6, 1, 5, 5));
        sidePanel.setBackground(new Color(240, 240, 240));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton accountBtn = new JButton("Accounts");
        JButton transBtn = new JButton("Transactions");
        JButton loanBtn = new JButton("Loans");
        JButton branchBtn = new JButton("Branches");
        JButton cardBtn = new JButton("Cards");
        JButton exitBtn = new JButton("Exit");

        sidePanel.add(accountBtn);
        sidePanel.add(transBtn);
        sidePanel.add(loanBtn);
        sidePanel.add(branchBtn);
        sidePanel.add(cardBtn);
        sidePanel.add(exitBtn);

        // Main content
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Select an option from the menu"), BorderLayout.CENTER);

        // Listeners for menu buttons — pass currentUser to each panel
        accountBtn.addActionListener(e -> switchPanel(new AccountPanel(currentUser)));
        transBtn.addActionListener(e -> switchPanel(new TransactionPanel(currentUser)));
        loanBtn.addActionListener(e -> switchPanel(new LoanPanel(currentUser)));
        branchBtn.addActionListener(e -> switchPanel(new BranchPanel(currentUser)));
        cardBtn.addActionListener(e -> switchPanel(new CardPanel(currentUser)));
        exitBtn.addActionListener(e -> System.exit(0));

        // Layout structure
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidePanel, contentPanel);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.2);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void switchPanel(JPanel newPanel) {
        contentPanel.removeAll();
        contentPanel.add(newPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void onLogout(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    public static void main(String[] args) {
        // Dummy user for quick demo
        AccountHolder demoUser = new AccountHolder();
        demoUser.setFullName("Demo User");
        demoUser.setRole("ADMIN");
        demoUser.setUsername("demo");
        demoUser.setAccountHolderID(1);

        SwingUtilities.invokeLater(() -> {
            MainFrame f = new MainFrame(demoUser);
            f.setVisible(true);
        });
    }
}
