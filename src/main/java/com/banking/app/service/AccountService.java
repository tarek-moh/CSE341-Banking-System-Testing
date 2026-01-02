package com.banking.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.banking.app.model.Account;
import com.banking.app.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.Optional;

import com.banking.app.exception.BankingException;

import com.banking.app.policy.TransactionPolicy;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionPolicy transactionPolicy;

    @Autowired
    public AccountService(AccountRepository accountRepository, TransactionPolicy transactionPolicy) {
        this.accountRepository = accountRepository;
        this.transactionPolicy = transactionPolicy;
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber).orElse(null);
    }

    public String processWithdraw(String accountNumber, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        try {
            transactionPolicy.validateWithdraw(account, amount);
            account.withdraw(amount);
            accountRepository.save(account);
            return "Withdrawal successful";
        } catch (BankingException e) {
            return "Failed: " + e.getMessage();
        }
    }

    public String processDeposit(String accountNumber, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        try {
            transactionPolicy.validateDeposit(account, amount);
            account.deposit(amount);
            accountRepository.save(account);
            return "Deposit successful";
        } catch (BankingException e) {
            return "Failed: " + e.getMessage();
        }
    }

    public String processTransfer(String sourceNumber, String destNumber, BigDecimal amount) {
        Optional<Account> sourceOpt = accountRepository.findByAccountNumber(sourceNumber);
        Optional<Account> destOpt = accountRepository.findByAccountNumber(destNumber);

        if (sourceOpt.isEmpty() || destOpt.isEmpty())
            return "Failed: Account not found";

        Account source = sourceOpt.get();
        Account dest = destOpt.get();

        try {
            transactionPolicy.validateTransfer(source, dest, amount);
            source.transfer(dest, amount);
            accountRepository.save(source);
            accountRepository.save(dest);
            return "Transfer successful";
        } catch (BankingException e) {
            return "Failed: " + e.getMessage();
        }
    }

    public String processVerify(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        try {
            account.verify();
            accountRepository.save(account);
            return "Verification successful";
        } catch (BankingException e) {
            return "Failed: " + e.getMessage();
        }
    }

    public String processSuspend(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        try {
            account.suspend();
            accountRepository.save(account);
            return "Suspension successful";
        } catch (BankingException e) {
            return "Failed: " + e.getMessage();
        }
    }

    public String processClose(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        try {
            account.close();
            accountRepository.save(account);
            return "Closing successful";
        } catch (BankingException e) {
            return "Failed: " + e.getMessage();
        }
    }

    public String processViolation(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        try {
            account.violation();
            accountRepository.save(account);
            return "Violation processed successfully";
        } catch (BankingException e) {
            return "Failed: " + e.getMessage();
        }
    }

    public String processAdminAction(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        try {
            account.adminAction();
            accountRepository.save(account);
            return "Admin action processed successfully";
        } catch (BankingException e) {
            return "Failed: " + e.getMessage();
        }
    }

    public String processAppeal(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        try {
            account.appeal();
            accountRepository.save(account);
            return "Appeal processed successfully";
        } catch (BankingException e) {
            return "Failed: " + e.getMessage();
        }
    }

    public java.util.List<Account> getAllAccounts() {
        return new java.util.ArrayList<>(accountRepository.findAll().values());
    }

    public Account login(String inputIdentifier, String password) {
        // Step 1: Try to find by Account Number (ID)
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(inputIdentifier);

        // Step 2: If not found by ID, try to find by Username
        if (accountOpt.isEmpty()) {
            accountOpt = accountRepository.findAccountbyUsername(inputIdentifier);
        }

        // Step 3: If STILL empty, the user doesn't exist
        if (accountOpt.isEmpty()) {
            throw new BankingException("User not found.");
        }

        Account account = accountOpt.get();

        // Step 4: Check Password
        if (!account.getPassword().equals(password)) {
            throw new BankingException("Invalid password.");
        }

        return account;
    }

    public Account registerUser(String clientName, String username, String password) {

        // TEST CASE 1: Duplicate Username Check
        // This allows you to test if the system blocks existing users.
        if (accountRepository.findAccountbyUsername(username).isPresent()) {
            throw new BankingException("Registration failed: Username '" + username + "' is already taken.");
        }

        // Generate a random 9-digit Account Number
        String newAccountNumber = String.valueOf((long) (Math.random() * 900000000L) + 100000000L);

        // TEST CASE 2: Collision Check ...for white box testing to be easier
        if (accountRepository.findByAccountNumber(newAccountNumber).isPresent()) {
            throw new BankingException("System error: Generated account number collision. Please try again.");
        }

        // Create the Account
        // Note: Status is hardcoded to "UNVERIFIED" to test state-based testing
        Account newAccount = new Account(
                newAccountNumber,
                clientName,
                username,
                password,
                0.00, // Initial Balance
                "UNVERIFIED");

        // Save to "Database"
        return accountRepository.save(newAccount);
    }
}
