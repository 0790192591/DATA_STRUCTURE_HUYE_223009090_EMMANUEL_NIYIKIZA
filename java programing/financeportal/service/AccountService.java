package com.financeportal.service;

import com.financeportal.dao.AccountDAO;
import com.financeportal.model.Account;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;

/**
 * AccountService - business logic around account creation and retrieval.
 *
 * Responsibilities:
 *  - Open accounts (generate account number, initial deposit)
 *  - Retrieve accounts
 *  - Provide helper operations used by UI
 */
public class AccountService {

    private final AccountDAO accountDAO;

    public AccountService() {
        this.accountDAO = new AccountDAO();
    }

    /**
     * Opens a new account for an existing account holder.
     *
     * @param accountHolderId existing account holder ID
     * @param accountType     e.g. "SAVINGS" or "CHECKING"
     * @param initialDeposit  initial deposit amount (nullable -> zero)
     * @return generated AccountID (>0) on success
     * @throws SQLException on DB errors
     */
    public int openAccount(int accountHolderId, String accountType, BigDecimal initialDeposit) throws SQLException {
        if (accountHolderId <= 0) throw new IllegalArgumentException("accountHolderId must be positive");
        if (accountType == null || accountType.isBlank()) throw new IllegalArgumentException("accountType required");

        BigDecimal deposit = initialDeposit == null ? BigDecimal.ZERO : initialDeposit;
        if (deposit.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("initialDeposit cannot be negative");

        Account account = new Account();
        account.setAccountHolderID(accountHolderId);
        account.setAccountType(accountType.toUpperCase());
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(deposit);
        account.setStatus("ACTIVE");

        return accountDAO.create(account);
    }

    /**
     * Fetch account by id.
     */
    public Account getAccount(int accountId) throws SQLException {
        if (accountId <= 0) throw new IllegalArgumentException("accountId must be positive");
        return accountDAO.getById(accountId);
    }

    /**
     * Generate a reasonably unique account number for the prototype.
     * For production, replace with bank-grade algorithm.
     */
    private String generateAccountNumber() {
        // Use a short UUID-derived string (alphanumeric). Prefix for recognizability.
        String u = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "AC" + u.substring(0, 14);
    }
}
