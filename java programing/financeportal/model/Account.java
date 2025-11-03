package com.financeportal.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private int accountID;
    private String accountNumber;
    private int accountHolderID;
    private String accountType;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private String status;

    public int getAccountID() { return accountID; }
    public void setAccountID(int accountID) { this.accountID = accountID; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public int getAccountHolderID() { return accountHolderID; }
    public void setAccountHolderID(int accountHolderID) { this.accountHolderID = accountHolderID; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
