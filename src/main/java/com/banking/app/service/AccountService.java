package com.banking.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.banking.app.model.Account;
import com.banking.app.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.Optional;

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
        if (!account.withdraw(amount))
            return "Failed to withdraw";

        accountRepository.save(account);
        return "Withdrawal successful";
    }

    public String processDeposit(String accountNumber, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        if (!account.deposit(amount))
            return "Failed to deposit";

        accountRepository.save(account);
        return "Deposit successful";
    }

    public String processTransfer(String sourceNumber, String destNumber, BigDecimal amount) {
        Optional<Account> sourceOpt = accountRepository.findByAccountNumber(sourceNumber);
        Optional<Account> destOpt = accountRepository.findByAccountNumber(destNumber);

        if (sourceOpt.isEmpty() || destOpt.isEmpty())
            return "Failed: Account(s) not found";

        Account source = sourceOpt.get();
        Account dest = destOpt.get();

        if (!source.transfer(dest, amount))
            return "Failed to transfer";

        accountRepository.save(source);
        accountRepository.save(dest);
        return "Transfer successful";
    }

    public String processVerify(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        if (!account.verify())
            return "Failed to verify";

        accountRepository.save(account);
        return "Verification successful";
    }

    public String processSuspend(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        if (!account.suspend())
            return "Failed to suspend";

        accountRepository.save(account);
        return "Suspension successful";
    }

    public String processClose(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty())
            return "Failed: Account not found";

        Account account = accountOpt.get();
        if (!account.close())
            return "Failed to close";

        accountRepository.save(account);
        return "Closing successful";
    }
}
