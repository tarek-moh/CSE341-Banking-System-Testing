package com.banking.app.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Account {
    private String accountNumber;
    private String clientName;
    private BigDecimal balance;
    private AccountStatus status;

    public Account(String accountNumber, String clientName, double balance, String status) {
        this.accountNumber = accountNumber;
        this.clientName = clientName;
        this.balance = BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP);
        this.status = AccountStatus.valueOf(status.toUpperCase());
    }

    public Account(String accountNumber, BigDecimal balance, AccountStatus status) {
        this.accountNumber = accountNumber;
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
        this.status = status;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public String toString() {
        return "Account [accountNumber=" + accountNumber + ", clientName=" + clientName + ", balance=" + balance
                + ", status=" + status + "]";
    }

    public boolean withdraw(BigDecimal amount) {
        if (status == AccountStatus.CLOSED || status == AccountStatus.SUSPENDED)
            return false;
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(balance) > 0)
            return false;
        balance = balance.subtract(amount);
        return true;
    }

    public boolean deposit(BigDecimal amount) {
        if (status == AccountStatus.CLOSED || amount.compareTo(BigDecimal.ZERO) <= 0)
            return false;
        balance = balance.add(amount);
        return true;
    }

    public boolean transfer(Account destination, BigDecimal amount) {
        if (!withdraw(amount))
            return false;
        if (!destination.deposit(amount)) {
            // Rollback withdrawal if deposit fails
            deposit(amount);
            return false;
        }
        return true;
    }

    public boolean verify() {
        if (status == AccountStatus.UNVERIFIED) {
            status = AccountStatus.VERIFIED;
            return true;
        }
        return false;
    }

    public boolean suspend() {
        if (status == AccountStatus.VERIFIED || status == AccountStatus.CLOSED) {
            status = AccountStatus.SUSPENDED;
            return true;
        }
        return false;
    }

    public boolean close() {
        if (status == AccountStatus.VERIFIED || status == AccountStatus.SUSPENDED) {
            status = AccountStatus.CLOSED;
            return true;
        }
        return false;
    }

}
