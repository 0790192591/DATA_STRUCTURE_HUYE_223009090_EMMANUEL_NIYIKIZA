package com.financeportal.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Loan {
    private int loanID;
    private int accountHolderID;
    private BigDecimal principal;
    private BigDecimal interestRate;
    private int termMonths;
    private String status;
    private LocalDateTime createdAt;

    public int getLoanID() { return loanID; }
    public void setLoanID(int loanID) { this.loanID = loanID; }

    public int getAccountHolderID() { return accountHolderID; }
    public void setAccountHolderID(int accountHolderID) { this.accountHolderID = accountHolderID; }

    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
