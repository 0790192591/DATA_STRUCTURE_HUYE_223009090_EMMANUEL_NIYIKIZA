package com.financeportal.ui;

import com.financeportal.model.Account;
import com.financeportal.model.AccountHolder;
import com.financeportal.dao.AccountDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel to create and list accounts.
 * Requires the currently logged-in AccountHolder to be passed in.
 */
public class AccountPanel extends JPanel {

    private final AccountDAO accountDAO = new AccountDAO();
    private final AccountHolder currentUser;

    private final JTextField holderIdField;
    private final JComboBox<String> typeCombo;
    private final JTextField initialDepositField;
    private final JButton openBtn;
    private final JButton refreshBtn;
    private final JTable table;
    private final DefaultTableModel model;

    /**
     * Create AccountPanel for the given user.
     *
     * @param user the logged-in account holder (cannot be null)
     */
    public AccountPanel(AccountHolder user) {
        if (user == null) throw new IllegalArgumentException("user must not be null");
        this.currentUser = user;

        setLayout(new BorderLayout(8, 8));

        // Form
        JPanel form = new JPanel(new GridLayout(2, 4, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Open Account"));

        form.add(new JLabel("Account Holder ID:"));
        holderIdField = new JTextField(String.valueOf(currentUser.getAccountHolderID()));
        form.add(holderIdField);

        form.add(new JLabel("Account Type:"));
        typeCombo = new JComboBox<>(new String[]{"SAVINGS", "CHECKING"});
        form.add(typeCombo);

        form.add(new JLabel("Initial Deposit:"));
        initialDepositField = new JTextField("0.00");
        form.add(initialDepositField);

        openBtn = new JButton("Open Account");
        refreshBtn = new JButton("Refresh");
        form.add(openBtn);
        form.add(refreshBtn);

        add(form, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new Object[]{"ID", "Number", "HolderID", "Type", "Balance", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        openBtn.addActionListener(e -> onOpenAccount());
        refreshBtn.addActionListener(e -> loadAccounts());

        // initial load
        loadAccounts();
    }

    private void onOpenAccount() {
        try {
            int holderId = Integer.parseInt(holderIdField.getText().trim());
            String type = (String) typeCombo.getSelectedItem();
            BigDecimal deposit = new BigDecimal(initialDepositField.getText().trim());

            // Basic validation
            if (holderId <= 0) {
                JOptionPane.showMessageDialog(this, "Holder ID must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (deposit.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Initial deposit cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Account acc = new Account();
            acc.setAccountHolderID(holderId);
            acc.setAccountType(type);
            acc.setAccountNumber("AC" + System.currentTimeMillis());
            acc.setBalance(deposit);
            acc.setStatus("ACTIVE");

            int id = accountDAO.create(acc);
            if (id > 0) {
                JOptionPane.showMessageDialog(this, "Account created. ID: " + id);
                loadAccounts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create account", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Holder ID and deposit must be numeric.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAccounts() {
        model.setRowCount(0);
        openBtn.setEnabled(false);
        refreshBtn.setEnabled(false);

        SwingWorker<List<Account>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Account> doInBackground() throws Exception {
                int holderId;
                try {
                    holderId = Integer.parseInt(holderIdField.getText().trim());
                } catch (NumberFormatException ex) {
                    // fallback to currentUser
                    holderId = currentUser.getAccountHolderID();
                }
                return accountDAO.listByHolder(holderId);
            }

            @Override
            protected void done() {
                openBtn.setEnabled(true);
                refreshBtn.setEnabled(true);
                try {
                    List<Account> list = get();
                    if (list != null) {
                        for (Account a : list) {
                            model.addRow(new Object[]{
                                    a.getAccountID(),
                                    a.getAccountNumber(),
                                    a.getAccountHolderID(),
                                    a.getAccountType(),
                                    a.getBalance(),
                                    a.getStatus()
                            });
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(AccountPanel.this, "Failed to load accounts: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
