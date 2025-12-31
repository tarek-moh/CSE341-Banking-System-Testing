package com.banking.app.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.banking.app.exception.*;

public class Account {
    private String accountNumber;
    private String clientName;
    private BigDecimal balance;
    private AccountStatus status;
    private final String transactionLimit = "1000";
    private final String minTransactionAmount = "50";

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

    // ======== Account Operations ========
    // > Note: These methods comply with the permissions table in permissions.md

    public void withdraw(BigDecimal amount) {
        if (status == AccountStatus.CLOSED) {
            throw new AccountStatusException("Transaction failed: Your account is closed.");
        }
        if (status == AccountStatus.SUSPENDED) {
            throw new AccountStatusException("Transaction failed: Your account is suspended.");
        }
        if (status == AccountStatus.UNVERIFIED) {
            throw new AccountStatusException("Transaction failed: Unverified accounts cannot withdraw funds.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transaction failed: Withdrawal amount must be greater than zero.");
        }

        if (amount.compareTo(balance) > 0) {
            throw new InsufficientFundsException(
                    "Transaction failed: Insufficient funds. Available balance is $" + balance);
        }

        if (amount.compareTo(new BigDecimal(transactionLimit)) > 0) {
            throw new InvalidAmountException(
                    "Transaction failed: Withdrawal amount must be less than $" + transactionLimit);
        }

        if (amount.compareTo(new BigDecimal(minTransactionAmount)) < 0) {
            throw new InvalidAmountException(
                    "Transaction failed: Withdrawal amount must be greater than $" + minTransactionAmount);
        }

        balance = balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        if (status == AccountStatus.CLOSED) {
            throw new AccountStatusException("Transaction failed: Your account is closed.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transaction failed: Deposit amount must be greater than zero.");
        }

        if (amount.compareTo(new BigDecimal(transactionLimit)) > 0) {
            throw new InvalidAmountException(
                    "Transaction failed: Deposit amount must be less than $" + transactionLimit);
        }

        if (amount.compareTo(new BigDecimal(minTransactionAmount)) < 0) {
            throw new InvalidAmountException(
                    "Transaction failed: Deposit amount must be greater than $" + minTransactionAmount);
        }

        balance = balance.add(amount);
    }

    public void transfer(Account destination, BigDecimal amount) {
        if (status == AccountStatus.CLOSED) {
            throw new AccountStatusException("Transfer failed: Your account is closed.");
        }
        if (status == AccountStatus.SUSPENDED) {
            throw new AccountStatusException("Transfer failed: Your account is suspended.");
        }
        if (status == AccountStatus.UNVERIFIED) {
            throw new AccountStatusException("Transfer failed: Unverified accounts cannot initiate transfers.");
        }

        if (destination.getStatus() == AccountStatus.CLOSED) {
            throw new AccountStatusException("Transfer failed: Destination account is closed.");
        }
        if (destination.getStatus() == AccountStatus.SUSPENDED) {
            throw new AccountStatusException("Transfer failed: Destination account is suspended.");
        }

        if (amount.compareTo(new BigDecimal(minTransactionAmount)) < 0) {
            throw new InvalidAmountException(
                    "Transfer failed: Transfer amount must be greater than $" + minTransactionAmount);
        }

        if (amount.compareTo(new BigDecimal(transactionLimit)) > 0) {
            throw new InvalidAmountException(
                    "Transfer failed: Transfer amount must be less than $" + transactionLimit);
        }

        // Withdraw from source
        this.withdraw(amount);

        // Deposit to destination
        try {
            destination.deposit(amount);
        } catch (BankingException e) {
            // Rollback withdrawal if deposit fails
            balance = balance.add(amount);
            throw new BankingException("Transfer failed: Could not deposit into recipient account. " + e.getMessage());
        }
    }

    public void verify() {
        if (status != AccountStatus.UNVERIFIED) {
            throw new AccountStatusException("Account is already " + status.toString().toLowerCase() + ".");
        }
        status = AccountStatus.VERIFIED;
    }

    public void suspend() {
        if (status == AccountStatus.SUSPENDED) {
            throw new AccountStatusException("Account is already suspended.");
        }
        status = AccountStatus.SUSPENDED;
    }

    public void close() {
        if (status == AccountStatus.CLOSED) {
            throw new AccountStatusException("Account is already closed.");
        }
        status = AccountStatus.CLOSED;
    }

}
