package com.financeportal.dao;

import com.financeportal.model.Card;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for card table.
 */
public class CardDAO {

    public int create(Card card) throws SQLException {
        String sql = "INSERT INTO card (card_number, account_holder_id, expiry, status, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, card.getCardNumber());
            ps.setInt(2, card.getAccountHolderID());
            ps.setTimestamp(3, card.getExpiry() == null ? null : Timestamp.valueOf(card.getExpiry()));
            ps.setString(4, card.getStatus() == null ? "ACTIVE" : card.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(card.getIssuedAt() == null ? LocalDateTime.now() : card.getIssuedAt()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    card.setCardID(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Card findById(int cardId) throws SQLException {
        String sql = "SELECT * FROM card WHERE card_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cardId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Card> listByHolder(int holderId) throws SQLException {
        String sql = "SELECT * FROM card WHERE account_holder_id = ? ORDER BY created_at DESC";
        List<Card> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, holderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean blockCard(int cardId) throws SQLException {
        String sql = "UPDATE card SET status = 'BLOCKED' WHERE card_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cardId);
            return ps.executeUpdate() == 1;
        }
    }

    private Card mapRow(ResultSet rs) throws SQLException {
        Card c = new Card();
        c.setCardID(rs.getInt("card_id"));
        c.setCardNumber(rs.getString("card_number"));
        c.setAccountHolderID(rs.getInt("account_holder_id"));
        Timestamp expiry = rs.getTimestamp("expiry");
        if (expiry != null) c.setExpiry(expiry.toLocalDateTime());
        c.setStatus(rs.getString("status"));
        Timestamp issued = rs.getTimestamp("created_at");
        if (issued != null) c.setIssuedAt(issued.toLocalDateTime());
        return c;
    }
}
