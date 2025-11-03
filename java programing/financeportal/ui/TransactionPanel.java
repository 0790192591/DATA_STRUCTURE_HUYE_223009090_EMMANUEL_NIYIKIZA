package com.financeportal.ui;

import com.financeportal.dao.AccountDAO;
import com.financeportal.model.Account;
import com.financeportal.model.AccountHolder;
import com.financeportal.model.Transaction;
import com.financeportal.service.TransactionService;
import com.financeportal.dao.TransactionDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * View and initiate simple money transfers.
 */
public class TransactionPanel extends JPanel {

    private final AccountHolder currentUser;
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionService transactionService = new TransactionService();

    private final JTable table;
    private final DefaultTableModel model;

    private final JTextField fromAccountField = new JTextField();
    private final JTextField toAccountField = new JTextField();
    private final JTextField amountField = new JTextField();

    public TransactionPanel(AccountHolder user) {
        if (user == null) throw new IllegalArgumentException("user required");
        this.currentUser = user;

        setLayout(new BorderLayout(8,8));
        JPanel form = new JPanel(new GridLayout(2,4,6,6));
        form.setBorder(BorderFactory.createTitledBorder("Make Transfer"));

        form.add(new JLabel("From Account ID:"));
        form.add(fromAccountField);
        form.add(new JLabel("To Account ID:"));
        form.add(toAccountField);
        form.add(new JLabel("Amount:"));
        form.add(amountField);

        JButton sendBtn = new JButton("Send");
        JButton refreshBtn = new JButton("Refresh");
        form.add(sendBtn);
        form.add(refreshBtn);

        add(form, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"TxID","AccountID","Type","Amount","Date","Notes"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        sendBtn.addActionListener(e -> doTransfer());
        refreshBtn.addActionListener(e -> loadTransactions());

        // initial load
        loadTransactions();
    }

    private void doTransfer() {
        try {
            int from = Integer.parseInt(fromAccountField.getText().trim());
            int to = Integer.parseInt(toAccountField.getText().trim());
            BigDecimal amt = new BigDecimal(amountField.getText().trim());

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return transactionService.transfer(from, to, amt);
                }
                @Override
                protected void done() {
                    try {
                        boolean ok = get();
                        if (ok) {
                            JOptionPane.showMessageDialog(TransactionPanel.this, "Transfer completed");
                            loadTransactions();
                        } else {
                            JOptionPane.showMessageDialog(TransactionPanel.this, "Transfer failed", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(TransactionPanel.this, "Transfer error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Account IDs and amount must be numeric", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactions() {
        model.setRowCount(0);
        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Transaction> doInBackground() throws Exception {
                // show transactions for all accounts of the holder (limited)
                List<Account> accounts = accountDAO.listByHolder(currentUser.getAccountHolderID());
                java.util.List<Transaction> all = new java.util.ArrayList<>();
                if (accounts != null) {
                    for (Account a : accounts) {
                        all.addAll(transactionDAO.listByAccount(a.getAccountID(), 50));
                    }
                }
                return all;
            }

            @Override
            protected void done() {
                try {
                    for (Transaction t : get()) {
                        model.addRow(new Object[]{
                                t.getTransactionID(),
                                t.getAccountID(),
                                t.getType(),
                                t.getAmount(),
                                t.getDate(),
                                t.getNotes()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TransactionPanel.this, "Error loading transactions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
