package com.financeportal.ui;

import com.financeportal.model.AccountHolder;
import javax.swing.*;
import java.awt.*;

/**
 * Dashboard summary frame/panel showing overview metrics.
 */
public class DashboardFrame extends JPanel {

    private final AccountHolder currentUser;
    private final JLabel welcomeLabel;
    private final JLabel balanceLabel;
    private final JLabel loanLabel;

    public DashboardFrame(AccountHolder user) {
        if (user == null) throw new IllegalArgumentException("user must not be null");
        this.currentUser = user;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        balanceLabel = new JLabel("Total Balance: Loading...");
        loanLabel = new JLabel("Active Loans: Loading...");

        JPanel stats = new JPanel(new GridLayout(2, 1, 10, 10));
        stats.add(balanceLabel);
        stats.add(loanLabel);

        add(welcomeLabel, BorderLayout.NORTH);
        add(stats, BorderLayout.CENTER);

        loadDashboardData();
    }

    private void loadDashboardData() {
        // Mocked data; replace with real DAO calls
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    Thread.sleep(400); // simulate DB
                } catch (InterruptedException ignored) {}
                return null;
            }

            @Override
            protected void done() {
                balanceLabel.setText("Total Balance: RWF 2,450,000.00");
                loanLabel.setText("Active Loans: 1 (RWF 600,000.00)");
            }
        };
        worker.execute();
    }
}
