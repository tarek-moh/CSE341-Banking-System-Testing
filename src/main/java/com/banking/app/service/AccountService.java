package com.banking.app.service;

import org.springframework.stereotype.Service;
import com.banking.app.model.Account;

@Service
public class AccountService {
    private static Account currentAccount = new Account("123", 1000, "Verified");

    public static Account getAccount(String accountNumber) {
        return currentAccount;
    }

    public String processWithdraw(double amount) {
        if (!currentAccount.withdraw(amount))
            return "Failed to withdraw";
        return "Withdrawal successful";
    }

    public String processDeposit(double amount) {
        if (!currentAccount.deposit(amount))
            return "Failed to deposit";
        return "Deposit successful";
    }

    public String processTransfer(Account destination, double amount) {
        if (!currentAccount.transfer(destination, amount))
            return "Failed to transfer";
        return "Transfer successful";
    }

    public String processVerify() {
        if (!currentAccount.verify())
            return "Failed to verify";
        return "Verification successful";
    }

    public String processSuspend() {
        if (!currentAccount.suspend())
            return "Failed to suspend";
        return "Suspension successful";
    }

    public String processClose() {
        if (!currentAccount.close())
            return "Failed to close";
        return "Closing successful";
    }
}
