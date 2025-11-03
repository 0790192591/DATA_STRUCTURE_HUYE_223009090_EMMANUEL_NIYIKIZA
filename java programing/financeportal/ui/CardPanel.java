package com.financeportal.ui;

import com.financeportal.dao.CardDAO;
import com.financeportal.model.AccountHolder;
import com.financeportal.model.Card;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CardPanel extends JPanel {
    private final AccountHolder currentUser;
    private final CardDAO cardDAO = new CardDAO();
    private final JTable table;
    private final DefaultTableModel model;

    public CardPanel(AccountHolder user) {
        if (user == null) throw new IllegalArgumentException("user required");
        this.currentUser = user;

        setLayout(new BorderLayout(8,8));
        model = new DefaultTableModel(new Object[]{"CardID","CardNumber","HolderID","Expiry","Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadCards());
        add(refresh, BorderLayout.SOUTH);

        loadCards();
    }

    private void loadCards() {
        model.setRowCount(0);
        new SwingWorker<List<Card>, Void>() {
            @Override
            protected List<Card> doInBackground() throws Exception {
                return cardDAO.listByHolder(currentUser.getAccountHolderID());
            }
            @Override
            protected void done() {
                try {
                    List<Card> list = get();
                    if (list != null) {
                        for (Card c : list) {
                            model.addRow(new Object[]{
                                    c.getCardID(),
                                    c.getCardNumber(),
                                    c.getAccountHolderID(),
                                    c.getExpiry(),
                                    c.getStatus()
                            });
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CardPanel.this, "Failed to load cards: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
