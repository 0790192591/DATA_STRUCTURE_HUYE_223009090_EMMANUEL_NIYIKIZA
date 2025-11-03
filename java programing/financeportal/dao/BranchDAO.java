package com.financeportal.dao;

import com.financeportal.model.Branch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for branch table.
 */
public class BranchDAO {

    public int create(Branch branch) throws SQLException {
        String sql = "INSERT INTO branch (name, address, capacity, manager, contact) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, branch.getName());
            ps.setString(2, branch.getAddress());
            ps.setInt(3, branch.getCapacity());
            ps.setString(4, branch.getManager());
            ps.setString(5, branch.getContact());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    branch.setBranchID(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Branch findById(int branchId) throws SQLException {
        String sql = "SELECT * FROM branch WHERE branch_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Branch> listAll() throws SQLException {
        String sql = "SELECT * FROM branch ORDER BY name";
        List<Branch> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean update(Branch branch) throws SQLException {
        String sql = "UPDATE branch SET name = ?, address = ?, capacity = ?, manager = ?, contact = ? WHERE branch_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branch.getName());
            ps.setString(2, branch.getAddress());
            ps.setInt(3, branch.getCapacity());
            ps.setString(4, branch.getManager());
            ps.setString(5, branch.getContact());
            ps.setInt(6, branch.getBranchID());
            return ps.executeUpdate() == 1;
        }
    }

    private Branch mapRow(ResultSet rs) throws SQLException {
        Branch b = new Branch();
        b.setBranchID(rs.getInt("branch_id"));
        b.setName(rs.getString("name"));
        b.setAddress(rs.getString("address"));
        b.setCapacity(rs.getInt("capacity"));
        b.setManager(rs.getString("manager"));
        b.setContact(rs.getString("contact"));
        return b;
    }
}
