package com.banking.app.policy;

import com.banking.app.exception.*;
import com.banking.app.model.Account;
import com.banking.app.model.AccountStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StandardTransactionPolicy implements TransactionPolicy {

    private static final BigDecimal MAX_LIMIT = new BigDecimal("10000");
    private static final BigDecimal MIN_LIMIT = new BigDecimal("100");

    @Override
    public void validateWithdraw(Account account, BigDecimal amount) {
        checkStatus(account, "Withdrawal");

        if (account.getStatus() == AccountStatus.UNVERIFIED) {
            throw new AccountStatusException("Transaction failed: Unverified accounts cannot withdraw funds.");
        }

        validateAmountRange(amount, "Withdrawal");

        if (amount.compareTo(account.getBalance()) > 0) {
            throw new InsufficientFundsException(
                    "Transaction failed: Insufficient funds. Available balance is " + account.getBalance() + "EGP");
        }
    }

    @Override
    public void validateDeposit(Account account, BigDecimal amount) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountStatusException("Transaction failed: Your account is closed.");
        }

        validateAmountRange(amount, "Deposit");
    }

    @Override
    public void validateTransfer(Account source, Account destination, BigDecimal amount) {
        if (source.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new BankingException("Transfer failed: Cannot transfer money to the same account.");
        }
        checkStatus(source, "Transfer");

        if (source.getStatus() == AccountStatus.UNVERIFIED) {
            throw new AccountStatusException("Transfer failed: Unverified accounts cannot initiate transfers.");
        }

        if (destination.getStatus() == AccountStatus.CLOSED) {
            throw new AccountStatusException("Transfer failed: Destination account is closed.");
        }

        validateAmountRange(amount, "Transfer");

        if (amount.compareTo(source.getBalance()) > 0) {
            throw new InsufficientFundsException(
                    "Transfer failed: Insufficient funds. Available balance is " + source.getBalance() + "EGP");
        }
    }

    private void checkStatus(Account account, String operation) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountStatusException(operation + " failed: Your account is closed.");
        }
        if (account.getStatus() == AccountStatus.SUSPENDED) {
            throw new AccountStatusException(operation + " failed: Your account is suspended.");
        }
    }

    private void validateAmountRange(BigDecimal amount, String operation) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(operation + " failed: Amount must be greater than zero.");
        }

        if (amount.compareTo(MAX_LIMIT) > 0) {
            throw new InvalidAmountException(
                    operation + " failed: Amount must be less than " + MAX_LIMIT + "EGP");
        }

        if (amount.compareTo(MIN_LIMIT) < 0) {
            throw new InvalidAmountException(
                    operation + " failed: Amount must be at least " + MIN_LIMIT + "EGP");
        }
    }
}
