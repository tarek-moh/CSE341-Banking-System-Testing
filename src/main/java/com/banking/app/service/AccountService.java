package com.banking.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.banking.app.model.Account;
import com.banking.app.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.Optional;

import com.banking.app.exception.BankingException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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
}
