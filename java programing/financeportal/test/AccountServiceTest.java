package com.financeportal.test;

import com.financeportal.dao.AccountDAO;
import com.financeportal.dao.AccountHolderDAO;
import com.financeportal.dao.DBConnection;
import com.financeportal.model.Account;
import com.financeportal.model.AccountHolder;
import com.financeportal.service.AccountService;
import com.financeportal.util.PasswordUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Integration test for AccountService.openAccount(...)
 * Java SE 21 + JUnit 5 (Jupiter) compatible. No lambdas used.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountServiceTest {

    private AccountService accountService;
    private AccountDAO accountDAO;
    private AccountHolderDAO accountHolderDAO;

    private Integer createdAccountId = null;
    private Integer createdHolderId = null;

 
    public void init() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null || !conn.isValid(2)) {
                // abort the whole test class if DB is not reachable
                Assumptions.abort("Database not reachable - aborting AccountServiceTest");
            }
        } catch (Exception e) {
            Assumptions.abort("DB not available: " + e.getMessage());
        }

        accountService = new AccountService();
        accountDAO = new AccountDAO();
        accountHolderDAO = new AccountHolderDAO();
    }

   
    public void cleanup() {
        try (Connection c = DBConnection.getConnection()) {
            if (createdAccountId != null) {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM account WHERE account_id = ?")) {
                    ps.setInt(1, createdAccountId);
                    ps.executeUpdate();
                } catch (Exception ignore) {
                    // try best-effort cleanup
                    System.err.println("Failed to delete account id " + createdAccountId + ": " + ignore.getMessage());
                }
            }

            if (createdHolderId != null) {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM account_holder WHERE account_holder_id = ?")) {
                    ps.setInt(1, createdHolderId);
                    ps.executeUpdate();
                } catch (Exception ignore) {
                    System.err.println("Failed to delete account_holder id " + createdHolderId + ": " + ignore.getMessage());
                }
            }
        } catch (Exception ex) {
            System.err.println("Cleanup failed (connection): " + ex.getMessage());
        } finally {
            createdAccountId = null;
            createdHolderId = null;
        }
    }

   
    public void testOpenAccount_createsAccount() {
        try {
            // Create account holder
            String username = "junit_user_" + System.currentTimeMillis();
            String passwordHash = PasswordUtil.hashPassword("Test@1234!");
            AccountHolder holder = new AccountHolder();
            holder.setUsername(username);
            holder.setPasswordHash(passwordHash);
            holder.setEmail(username + "@example.com");
            holder.setFullName("JUnit User");
            holder.setRole("CUSTOMER");

            int holderId = accountHolderDAO.create(holder);
            assertTrue(holderId > 0, "Account holder creation failed");
            createdHolderId = holderId;

            // Open account
            int accountId = accountService.openAccount(holderId, "SAVINGS", new BigDecimal("300.00"));
            assertTrue(accountId > 0, "Account creation failed");
            createdAccountId = accountId;

            Account acct = accountDAO.getById(accountId);
            assertNotNull(acct, "Account should not be null");
            assertEquals(holderId, acct.getAccountHolderID(), "Holder ID mismatch");
            assertEquals("SAVINGS", acct.getAccountType(), "Account type mismatch");
            assertEquals(0, acct.getBalance().compareTo(new BigDecimal("300.00")), "Balance mismatch");

        } catch (Exception ex) {
            fail("Test failed with exception: " + ex.getMessage());
        }
    }
}
