package com.financeportal.dao;

import com.financeportal.model.AccountHolder;
import com.financeportal.util.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for account_holder table.
 */
public class AccountHolderDAO {

    /**
     * Create a new account holder. Returns generated id (>0) or -1 on failure.
     */
    public int create(AccountHolder ah) throws SQLException {
        String sql = "INSERT INTO account_holder (username, password_hash, email, full_name, role, created_at, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, ah.getUsername());
            ps.setString(2, ah.getPasswordHash()); // hash should already be set
            ps.setString(3, ah.getEmail());
            ps.setString(4, ah.getFullName());
            ps.setString(5, ah.getRole());
            ps.setTimestamp(6, Timestamp.valueOf(ah.getCreatedAt() == null ? LocalDateTime.now() : ah.getCreatedAt()));
            ps.setString(7, ah.getStatus() == null ? "ACTIVE" : ah.getStatus());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    ah.setAccountHolderID(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Find account holder by id.
     */
    public AccountHolder findById(int id) throws SQLException {
        String sql = "SELECT * FROM account_holder WHERE account_holder_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Find account holder by username.
     */
    public AccountHolder findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM account_holder WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Authenticate using plain password; PasswordUtil verifies PBKDF2 stored hash.
     * Returns AccountHolder if credentials valid, otherwise null.
     */
    public AccountHolder authenticate(String username, String plainPassword) throws SQLException {
        AccountHolder ah = findByUsername(username);
        if (ah == null) return null;
        String stored = ah.getPasswordHash();
        if (stored == null) return null;
        boolean ok = PasswordUtil.verifyPassword(plainPassword, stored);
        return ok ? ah : null;
    }

    /**
     * Update last_login timestamp
     */
    public boolean updateLastLogin(int accountHolderId, LocalDateTime lastLogin) throws SQLException {
        String sql = "UPDATE account_holder SET last_login = ? WHERE account_holder_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(lastLogin));
            ps.setInt(2, accountHolderId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * List all account holders (careful on large datasets).
     */
    public List<AccountHolder> listAll() throws SQLException {
        String sql = "SELECT * FROM account_holder ORDER BY created_at DESC";
        List<AccountHolder> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Soft deactivate user (status = INACTIVE)
     */
    public boolean deactivate(int accountHolderId) throws SQLException {
        String sql = "UPDATE account_holder SET status = 'INACTIVE' WHERE account_holder_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountHolderId);
            return ps.executeUpdate() == 1;
        }
    }

    private AccountHolder mapRow(ResultSet rs) throws SQLException {
        AccountHolder a = new AccountHolder();
        a.setAccountHolderID(rs.getInt("account_holder_id"));
        a.setUsername(rs.getString("username"));
        a.setPasswordHash(rs.getString("password_hash"));
        a.setEmail(rs.getString("email"));
        a.setFullName(rs.getString("full_name"));
        a.setRole(rs.getString("role"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) a.setCreatedAt(created.toLocalDateTime());
        Timestamp last = rs.getTimestamp("last_login");
        if (last != null) a.setLastLogin(last.toLocalDateTime());
        a.setStatus(rs.getString("status"));
        return a;
    }
}
