package com.banking.app.model;

public class Account {
    private String accountNumber;
    private double balance;
    private String status; // "Unverified", "Verified", "Suspended", "Closed"

    public Account(String accountNumber, double balance, String status) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.status = status;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Account [accountNumber=" + accountNumber + ", balance=" + balance + ", status=" + status + "]";
    }

    public boolean withdraw(double amount) {
        if (status.equals("Closed") || status.equals("Suspended"))
            return false;
        if (amount > balance)
            return false;
        balance -= amount;
        return true;
    }

    public boolean deposit(double amount) {
        if (status.equals("Closed") || amount <= 0)
            return false;
        balance += amount;
        return true;
    }

    public boolean transfer(Account destination, double amount) {
        if (!withdraw(amount))
            return false;
        if (!destination.deposit(amount))
            return false;
        return true;
    }

    public boolean verify() {
        if (status.equals("Unverified")) {
            status = "Verified";
            return true;
        }
        return false;
    }

    public boolean suspend() {
        if (status.equals("Verified") || status.equals("Closed")) {
            status = "Suspended";
            return true;
        }
        return false;
    }

    public boolean close() {
        if (status.equals("Verified") || status.equals("Suspended")) {
            status = "Closed";
            return true;
        }
        return false;
    }

}
