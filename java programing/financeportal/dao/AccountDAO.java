package com.financeportal.dao;

import com.financeportal.model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for account table.
 */
public class AccountDAO {

    public int create(Account account) throws SQLException {
        String sql = "INSERT INTO account (account_number, account_holder_id, account_type, balance, created_at, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, account.getAccountNumber());
            ps.setInt(2, account.getAccountHolderID());
            ps.setString(3, account.getAccountType());
            ps.setBigDecimal(4, account.getBalance() == null ? BigDecimal.ZERO : account.getBalance());
            ps.setTimestamp(5, Timestamp.valueOf(account.getCreatedAt() == null ? LocalDateTime.now() : account.getCreatedAt()));
            ps.setString(6, account.getStatus() == null ? "ACTIVE" : account.getStatus());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    account.setAccountID(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Account getById(int accountId) throws SQLException {
        String sql = "SELECT * FROM account WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Account getByNumber(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM account WHERE account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Account> listByHolder(int holderId) throws SQLException {
        String sql = "SELECT * FROM account WHERE account_holder_id = ? ORDER BY created_at DESC";
        List<Account> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, holderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Update balance using provided connection for atomic operations.
     */
    public boolean updateBalance(int accountId, BigDecimal newBalance, Connection conn) throws SQLException {
        String sql = "UPDATE account SET balance = ? WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, accountId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Non-transactional convenience method.
     */
    public boolean updateBalance(int accountId, BigDecimal newBalance) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return updateBalance(accountId, newBalance, conn);
        }
    }

    public boolean deactivate(int accountId) throws SQLException {
        String sql = "UPDATE account SET status = 'INACTIVE' WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            return ps.executeUpdate() == 1;
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountID(rs.getInt("account_id"));
        a.setAccountNumber(rs.getString("account_number"));
        a.setAccountHolderID(rs.getInt("account_holder_id"));
        a.setAccountType(rs.getString("account_type"));
        a.setBalance(rs.getBigDecimal("balance"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        a.setStatus(rs.getString("status"));
        return a;
    }
}
