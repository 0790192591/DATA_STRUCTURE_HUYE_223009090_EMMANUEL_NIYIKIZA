package com.financeportal.model;

import java.time.LocalDateTime;

public class Card {
    private int cardID;
    private String cardNumber;
    private int accountHolderID;
    private LocalDateTime expiry;
    private String status;
    private LocalDateTime issuedAt;

    public int getCardID() { return cardID; }
    public void setCardID(int cardID) { this.cardID = cardID; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public int getAccountHolderID() { return accountHolderID; }
    public void setAccountHolderID(int accountHolderID) { this.accountHolderID = accountHolderID; }

    public LocalDateTime getExpiry() { return expiry; }
    public void setExpiry(LocalDateTime expiry) { this.expiry = expiry; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}
