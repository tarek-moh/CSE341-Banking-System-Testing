package com.banking.app.dto;

public class PaymentResponse {
    private String status; // SUCCESS, FAILURE
    private String transactionId;
    private String errorCode;
    private String errorMessage;

    public PaymentResponse() {
    }

    public PaymentResponse(String status, String transactionId, String errorCode, String errorMessage) {
        this.status = status;
        this.transactionId = transactionId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
