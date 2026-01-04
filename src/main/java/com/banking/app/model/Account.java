package com.banking.app.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.banking.app.exception.*;

public class Account {
    private String accountNumber;
    private String clientName;
    private String username;
    private String password;
    // private String phoneNumber;
    // private String email;
    private BigDecimal balance;
    private AccountStatus status;
    private boolean isAdmin;

    public Account(String accountNumber, String clientName, String username, String password, double balance,
            String status) {
        this.accountNumber = accountNumber;
        this.clientName = clientName;
        this.username = username;
        this.password = password;
        // this.phoneNumber = phoneNumber;
        // this.email = email;
        this.balance = BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP);
        this.status = AccountStatus.valueOf(status.toUpperCase());
        this.isAdmin = false;
    }

    public Account(String accountNumber, String clientName, String username, String password, double balance,
            String status, boolean isAdmin) {
        this.accountNumber = accountNumber;
        this.clientName = clientName;
        this.username = username;
        this.password = password;
        this.balance = BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP);
        this.status = AccountStatus.valueOf(status.toUpperCase());
        this.isAdmin = isAdmin;
    }

    // ###########needs to be removed
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public String toString() {
        return "Account [accountNumber=" + accountNumber + ", clientName=" + clientName + ", balance=" + balance
                + ", status=" + status + "]";
    }

    // ======== Account Operations ========
    // > Note: Rules and limits are validated in the TransactionPolicy layer
    // > before these state changes are called.

    public void withdraw(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void transfer(Account destination, BigDecimal amount) {
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

    /**
     * Handles violation: Unverified -> Suspended, or Verified -> Closed
     */
    public void violation() {
        if (status == AccountStatus.UNVERIFIED) {
            status = AccountStatus.SUSPENDED;
        } else if (status == AccountStatus.VERIFIED) {
            status = AccountStatus.CLOSED;
        } else {
            throw new AccountStatusException("Cannot apply violation to account in " + status.toString().toLowerCase() + " state.");
        }
    }

    /**
     * Admin action: Suspended -> Closed, or Verified -> Closed
     */
    public void adminAction() {
        if (status == AccountStatus.SUSPENDED || status == AccountStatus.VERIFIED) {
            status = AccountStatus.CLOSED;
        } else {
            throw new AccountStatusException("Cannot apply admin action to account in " + status.toString().toLowerCase() + " state.");
        }
    }

    /**
     * Appeal: Closed -> Suspended
     */
    public void appeal() {
        if (status != AccountStatus.CLOSED) {
            throw new AccountStatusException("Appeal can only be applied to closed accounts. Current status: " + status.toString().toLowerCase() + ".");
        }
        status = AccountStatus.SUSPENDED;
    }

}
