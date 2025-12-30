package com.banking.app.exception;

public class InvalidAmountException extends BankingException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
