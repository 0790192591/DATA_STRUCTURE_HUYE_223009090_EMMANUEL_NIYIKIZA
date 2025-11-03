package com.financeportal.dao;

import com.financeportal.model.Loan;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for loan table.
 */
public class LoanDAO {

    public int create(Loan loan) throws SQLException {
        String sql = "INSERT INTO loan (account_holder_id, principal, interest_rate, term_months, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, loan.getAccountHolderID());
            ps.setBigDecimal(2, loan.getPrincipal());
            ps.setBigDecimal(3, loan.getInterestRate());
            ps.setInt(4, loan.getTermMonths());
            ps.setString(5, loan.getStatus() == null ? "APPLIED" : loan.getStatus());
            ps.setTimestamp(6, Timestamp.valueOf(loan.getCreatedAt() == null ? LocalDateTime.now() : loan.getCreatedAt()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    loan.setLoanID(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Loan findById(int loanId) throws SQLException {
        String sql = "SELECT * FROM loan WHERE loan_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Loan> listByHolder(int holderId) throws SQLException {
        String sql = "SELECT * FROM loan WHERE account_holder_id = ? ORDER BY created_at DESC";
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, holderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean updateStatus(int loanId, String newStatus) throws SQLException {
        String sql = "UPDATE loan SET status = ? WHERE loan_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, loanId);
            return ps.executeUpdate() == 1;
        }
    }

    private Loan mapRow(ResultSet rs) throws SQLException {
        Loan l = new Loan();
        l.setLoanID(rs.getInt("loan_id"));
        l.setAccountHolderID(rs.getInt("account_holder_id"));
        l.setPrincipal(rs.getBigDecimal("principal"));
        l.setInterestRate(rs.getBigDecimal("interest_rate"));
        l.setTermMonths(rs.getInt("term_months"));
        l.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) l.setCreatedAt(ts.toLocalDateTime());
        return l;
    }
}
