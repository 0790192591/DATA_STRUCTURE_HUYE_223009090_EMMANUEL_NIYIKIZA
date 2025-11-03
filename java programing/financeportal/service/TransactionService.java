package com.financeportal.service;

import com.financeportal.dao.AccountDAO;
import com.financeportal.dao.TransactionDAO;
import com.financeportal.dao.DBConnection;
import com.financeportal.model.Account;
import com.financeportal.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * TransactionService - business logic around transactions.
 * Added transfer(...) method to move money between two accounts atomically.
 */
public class TransactionService {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public TransactionService() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
    }

    // existing postTransaction(Transaction tx) omitted for brevity (keep your earlier version)

    /**
     * Transfer amount from 'fromAccountId' to 'toAccountId' (atomic).
     * Returns true on success.
     */
    public boolean transfer(int fromAccountId, int toAccountId, BigDecimal amount) throws SQLException {
        if (fromAccountId <= 0 || toAccountId <= 0) throw new IllegalArgumentException("Account IDs must be positive");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (fromAccountId == toAccountId) throw new IllegalArgumentException("Cannot transfer to same account");

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);

                Account from = accountDAO.getById(fromAccountId);
                Account to = accountDAO.getById(toAccountId);
                if (from == null || to == null) throw new IllegalStateException("One or both accounts not found");

                BigDecimal fromBalance = from.getBalance() == null ? BigDecimal.ZERO : from.getBalance();
                if (fromBalance.compareTo(amount) < 0) throw new IllegalStateException("Insufficient funds");

                BigDecimal newFrom = fromBalance.subtract(amount);
                BigDecimal toBalance = to.getBalance() == null ? BigDecimal.ZERO : to.getBalance();
                BigDecimal newTo = toBalance.add(amount);

                boolean ub1 = accountDAO.updateBalance(fromAccountId, newFrom, conn);
                if (!ub1) throw new SQLException("Failed to debit source account");

                boolean ub2 = accountDAO.updateBalance(toAccountId, newTo, conn);
                if (!ub2) throw new SQLException("Failed to credit destination account");

                // create debit transaction
                Transaction debit = new Transaction();
                debit.setAccountID(fromAccountId);
                debit.setType("DEBIT");
                debit.setAmount(amount);
                debit.setDate(LocalDateTime.now());
                debit.setOrderNumber("TR-" + System.currentTimeMillis() + "-D");
                debit.setStatus("COMPLETED");
                debit.setPaymentMethod("TRANSFER");
                debit.setNotes("Transfer to account " + toAccountId);
                int dId = transactionDAO.create(debit, conn);
                if (dId <= 0) throw new SQLException("Failed to create debit transaction");

                // create credit transaction
                Transaction credit = new Transaction();
                credit.setAccountID(toAccountId);
                credit.setType("CREDIT");
                credit.setAmount(amount);
                credit.setDate(LocalDateTime.now());
                credit.setOrderNumber("TR-" + System.currentTimeMillis() + "-C");
                credit.setStatus("COMPLETED");
                credit.setPaymentMethod("TRANSFER");
                credit.setNotes("Transfer from account " + fromAccountId);
                int cId = transactionDAO.create(credit, conn);
                if (cId <= 0) throw new SQLException("Failed to create credit transaction");

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
