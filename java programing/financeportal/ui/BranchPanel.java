package com.financeportal.ui;

import com.financeportal.dao.BranchDAO;
import com.financeportal.model.AccountHolder;
import com.financeportal.model.Branch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BranchPanel extends JPanel {
    private final AccountHolder currentUser;
    private final BranchDAO branchDAO = new BranchDAO();
    private final DefaultTableModel model;
    private final JTable table;

    public BranchPanel(AccountHolder user) {
        if (user == null) throw new IllegalArgumentException("user required");
        this.currentUser = user;

        setLayout(new BorderLayout(8,8));
        model = new DefaultTableModel(new Object[]{"BranchID","Name","Address","Manager","Contact"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadBranches());
        add(refresh, BorderLayout.SOUTH);

        loadBranches();
    }

    private void loadBranches() {
        model.setRowCount(0);
        new SwingWorker<List<Branch>, Void>() {
            @Override
            protected List<Branch> doInBackground() throws Exception {
                return branchDAO.listAll();
            }
            @Override
            protected void done() {
                try {
                    List<Branch> list = get();
                    if (list != null) {
                        for (Branch b : list) {
                            model.addRow(new Object[]{
                                    b.getBranchID(),
                                    b.getName(),
                                    b.getAddress(),
                                    b.getManager(),
                                    b.getContact()
                            });
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BranchPanel.this, "Failed to load branches: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
