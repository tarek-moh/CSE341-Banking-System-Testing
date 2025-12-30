package com.banking.app.dto;

import java.math.BigDecimal;

public class PaymentRequest {
    private String cardNumber;
    private String cvv;
    private String expiryDate; // MM/YY
    private BigDecimal amount;

    public PaymentRequest() {
    }

    public PaymentRequest(String cardNumber, String cvv, String expiryDate, BigDecimal amount) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
        this.amount = amount;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
