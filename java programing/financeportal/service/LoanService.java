package com.financeportal.service;

import com.financeportal.dao.AccountDAO;
import com.financeportal.dao.LoanDAO;
import com.financeportal.dao.TransactionDAO;
import com.financeportal.dao.DBConnection;
import com.financeportal.model.Loan;
import com.financeportal.model.Transaction;
import com.financeportal.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * LoanService - basic loan flows:
 *  - applyForLoan: create loan application (status = APPLIED)
 *  - approveAndDisburse: mark loan APPROVED/DISBURSED and create a credit transaction to an account (atomic)
 *  - repayLoan: create a DEBIT transaction on the specified account and optionally close the loan (naive)
 *
 * NOTES:
 *  - This implementation is intentionally minimal. In production you'd track repayments,
 *    interest accrual, amortization schedule, outstanding principal, and repayments table.
 */
public class LoanService {

    private final LoanDAO loanDAO;
    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;

    public LoanService() {
        this.loanDAO = new LoanDAO();
        this.transactionDAO = new TransactionDAO();
        this.accountDAO = new AccountDAO();
    }

    /**
     * Create a loan application row.
     * @param loan loan object with accountHolderID, principal, interestRate, termMonths
     * @return generated loan id
     * @throws SQLException
     */
    public int applyForLoan(Loan loan) throws SQLException {
        if (loan == null) throw new IllegalArgumentException("Loan cannot be null");
        if (loan.getAccountHolderID() <= 0) throw new IllegalArgumentException("AccountHolderID required");
        if (loan.getPrincipal() == null || loan.getPrincipal().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Principal must be positive");

        loan.setStatus("APPLIED");
        loan.setCreatedAt(LocalDateTime.now());
        return loanDAO.create(loan);
    }

    /**
     * Approve a loan and disburse to a target account in one atomic operation.
     *
     * @param loanId   loan to approve
     * @param accountId target account to credit (must belong to the loan applicant)
     * @return transaction id of disbursement
     * @throws SQLException
     */
    public int approveAndDisburse(int loanId, int accountId) throws SQLException {
        if (loanId <= 0) throw new IllegalArgumentException("loanId required");
        if (accountId <= 0) throw new IllegalArgumentException("accountId required");

        // Fetch loan and account
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) throw new IllegalStateException("Loan not found: " + loanId);
        Account account = accountDAO.getById(accountId);
        if (account == null) throw new IllegalStateException("Target account not found: " + accountId);

        // Basic ownership check: account belongs to loan applicant
        if (account.getAccountHolderID() != loan.getAccountHolderID())
            throw new IllegalStateException("Target account does not belong to the loan applicant");

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // 1) Update loan status to APPROVED then DISBURSED (we can do both or single step)
                boolean updated = loanDAO.updateStatus(loanId, "DISBURSED");
                if (!updated) throw new SQLException("Failed to update loan status");

                // 2) Create a CREDIT transaction that increases account balance by loan.principal
                Transaction tx = new Transaction();
                tx.setAccountID(accountId);
                tx.setType("CREDIT");
                tx.setAmount(loan.getPrincipal());
                tx.setDate(LocalDateTime.now());
                tx.setOrderNumber("LN-DSB-" + System.currentTimeMillis());
                tx.setStatus("COMPLETED");
                tx.setPaymentMethod("LOAN_DISBURSEMENT");
                tx.setNotes("Loan disbursement for loanId=" + loanId);

                // Update account balance using connection-aware AccountDAO
                BigDecimal current = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
                BigDecimal newBalance = current.add(loan.getPrincipal());
                boolean balanceUpdated = accountDAO.updateBalance(accountId, newBalance, conn);
                if (!balanceUpdated) throw new SQLException("Failed to update account balance for disbursement");

                // Insert transaction using same connection
                int txId = transactionDAO.create(tx, conn);
                if (txId <= 0) throw new SQLException("Failed to create disbursement transaction");

                conn.commit();
                return txId;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Repay loan by debiting an account.
     * This is a naive implementation: it debits the given account and if the repaid
     * amount >= loan principal it marks the loan CLOSED. In production you'd track
     * cumulative repayments and interest.
     *
     * @param loanId loan id
     * @param accountId account to debit
     * @param amount repayment amount
     * @return transaction id of repayment
     * @throws SQLException
     */
    public int repayLoan(int loanId, int accountId, BigDecimal amount) throws SQLException {
        if (loanId <= 0) throw new IllegalArgumentException("loanId required");
        if (accountId <= 0) throw new IllegalArgumentException("accountId required");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("amount must be positive");

        Loan loan = loanDAO.findById(loanId);
        if (loan == null) throw new IllegalStateException("Loan not found: " + loanId);

        Account account = accountDAO.getById(accountId);
        if (account == null) throw new IllegalStateException("Account not found: " + accountId);

        // Ensure account belongs to loan holder
        if (account.getAccountHolderID() != loan.getAccountHolderID())
            throw new IllegalStateException("Account does not belong to loan holder");

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // Ensure sufficient funds
                BigDecimal current = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
                if (current.compareTo(amount) < 0) throw new IllegalStateException("Insufficient funds for repayment");

                BigDecimal newBalance = current.subtract(amount);
                boolean updated = accountDAO.updateBalance(accountId, newBalance, conn);
                if (!updated) throw new SQLException("Failed to update account balance for repayment");

                Transaction tx = new Transaction();
                tx.setAccountID(accountId);
                tx.setType("DEBIT");
                tx.setAmount(amount);
                tx.setDate(LocalDateTime.now());
                tx.setOrderNumber("LN-RPY-" + System.currentTimeMillis());
                tx.setStatus("COMPLETED");
                tx.setPaymentMethod("LOAN_REPAYMENT");
                tx.setNotes("Loan repayment for loanId=" + loanId);

                int txId = transactionDAO.create(tx, conn);
                if (txId <= 0) throw new SQLException("Failed to create repayment transaction");

                // Naive loan closing: if this single repayment >= principal, mark closed.
                // In real systems compute total repaid across payments -> outstanding.
                if (amount.compareTo(loan.getPrincipal()) >= 0) {
                    boolean closed = loanDAO.updateStatus(loanId, "CLOSED");
                    if (!closed) throw new SQLException("Failed to close loan after repayment");
                }

                conn.commit();
                return txId;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
