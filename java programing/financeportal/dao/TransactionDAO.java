package com.financeportal.dao;

import com.financeportal.model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for transaction table.
 */
public class TransactionDAO {

    public int create(Transaction tx) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return create(tx, conn);
        }
    }

    /**
     * Create transaction using given connection (for atomic service operations).
     * Returns generated transaction id.
     */
    public int create(Transaction tx, Connection conn) throws SQLException {
        String sql = "INSERT INTO transaction (order_number, account_id, date, type, status, amount, payment_method, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tx.getOrderNumber());
            ps.setInt(2, tx.getAccountID());
            ps.setTimestamp(3, Timestamp.valueOf(tx.getDate() == null ? LocalDateTime.now() : tx.getDate()));
            ps.setString(4, tx.getType());
            ps.setString(5, tx.getStatus() == null ? "COMPLETED" : tx.getStatus());
            ps.setBigDecimal(6, tx.getAmount());
            ps.setString(7, tx.getPaymentMethod());
            ps.setString(8, tx.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    tx.setTransactionID(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Transaction findById(int transactionId) throws SQLException {
        String sql = "SELECT * FROM transaction WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Transaction> listByAccount(int accountId, int limit) throws SQLException {
        String sql = "SELECT * FROM transaction WHERE account_id = ? ORDER BY date DESC LIMIT ?";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionID(rs.getInt("transaction_id"));
        t.setOrderNumber(rs.getString("order_number"));
        t.setAccountID(rs.getInt("account_id"));
        Timestamp ts = rs.getTimestamp("date");
        if (ts != null) t.setDate(ts.toLocalDateTime());
        t.setType(rs.getString("type"));
        t.setStatus(rs.getString("status"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setPaymentMethod(rs.getString("payment_method"));
        t.setNotes(rs.getString("notes"));
        return t;
    }
}
