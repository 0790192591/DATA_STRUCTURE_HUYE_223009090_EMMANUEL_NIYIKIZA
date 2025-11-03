package com.financeportal.ui;

import com.financeportal.dao.LoanDAO;
import com.financeportal.model.AccountHolder;
import com.financeportal.model.Loan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Manage loans for the logged-in user.
 */
public class LoanPanel extends JPanel {

    private final AccountHolder currentUser;
    private final LoanDAO loanDAO = new LoanDAO();

    private final JTextField principalField = new JTextField();
    private final JTextField rateField = new JTextField("5.0");
    private final JTextField termField = new JTextField("12");
    private final JTable table;
    private final DefaultTableModel model;

    public LoanPanel(AccountHolder user) {
        if (user == null) throw new IllegalArgumentException("user required");
        this.currentUser = user;
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridLayout(2,4,6,6));
        form.setBorder(BorderFactory.createTitledBorder("Apply for Loan"));
        form.add(new JLabel("Principal:"));
        form.add(principalField);
        form.add(new JLabel("Interest %:"));
        form.add(rateField);
        form.add(new JLabel("Term (months):"));
        form.add(termField);

        JButton applyBtn = new JButton("Apply");
        JButton refreshBtn = new JButton("Refresh");
        form.add(applyBtn);
        form.add(refreshBtn);
        add(form, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"LoanID","Principal","InterestRate","Term","Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        applyBtn.addActionListener(e -> applyLoan());
        refreshBtn.addActionListener(e -> loadLoans());
        loadLoans();
    }

    private void applyLoan() {
        try {
            BigDecimal principal = new BigDecimal(principalField.getText().trim());
            BigDecimal ratePercent = new BigDecimal(rateField.getText().trim());
            int term = Integer.parseInt(termField.getText().trim());

            if (principal.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Principal must be positive", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Loan loan = new Loan();
            loan.setAccountHolderID(currentUser.getAccountHolderID());
            loan.setPrincipal(principal);
            // convert percent to decimal (5 -> 0.05)
            loan.setInterestRate(ratePercent.divide(BigDecimal.valueOf(100)));
            loan.setTermMonths(term);
            loan.setStatus("APPLIED");

            int id = loanDAO.create(loan);
            if (id > 0) {
                JOptionPane.showMessageDialog(this, "Loan application submitted. ID: " + id);
                loadLoans();
            } else {
                JOptionPane.showMessageDialog(this, "Loan application failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLoans() {
        model.setRowCount(0);
        new SwingWorker<List<Loan>, Void>() {
            @Override
            protected List<Loan> doInBackground() throws Exception {
                return loanDAO.listByHolder(currentUser.getAccountHolderID());
            }

            @Override
            protected void done() {
                try {
                    List<Loan> list = get();
                    if (list != null) {
                        for (Loan l : list) {
                            model.addRow(new Object[]{
                                    l.getLoanID(),
                                    l.getPrincipal(),
                                    l.getInterestRate(),
                                    l.getTermMonths(),
                                    l.getStatus()
                            });
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoanPanel.this, "Failed to load loans: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
