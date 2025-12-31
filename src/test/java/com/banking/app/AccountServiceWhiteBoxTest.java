package com.banking.app;

import com.banking.app.exception.BankingException;
import com.banking.app.exception.InvalidAmountException;
import com.banking.app.model.Account;
import com.banking.app.policy.TransactionPolicy;
import com.banking.app.repository.AccountRepository;
import com.banking.app.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountServiceWhiteBoxTest {

    private StubAccountRepository accountRepository;
    private StubTransactionPolicy transactionPolicy;
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        accountRepository = new StubAccountRepository();
        transactionPolicy = new StubTransactionPolicy();
        accountService = new AccountService(accountRepository, transactionPolicy);
    }

    // =========================================================
    // Deposit Analysis
    // =========================================================

    @Test
    // Coverage: Deposit - Path 1 (Decision: Account Not Found)
    public void testDeposit_AccountNotFound() {
        String result = accountService.processDeposit("999", new BigDecimal("100"));
        assertEquals("Failed: Account not found", result);
    }

    @Test
    // Coverage: Deposit - Path 2 (Decision: Success)
    public void testDeposit_Success() {

        Account account = new Account("123", "John", "john_user", "pass123", 1000.0, "VERIFIED");
        accountRepository.save(account);

        String result = accountService.processDeposit("123", new BigDecimal("500"));

        assertEquals("Deposit successful", result);
        assertEquals(new BigDecimal("1500.00"), account.getBalance());
    }

    @Test
    // Coverage: Deposit - Path 3 (Decision: Policy Exception)
    public void testDeposit_PolicyFailure() {
        Account account = new Account("123", "John", "john_user", "pass123", 1000.0, "VERIFIED");
        accountRepository.save(account);

        // Configure stub to throw exception for this specific scenario
        transactionPolicy.shouldThrowDepositError = true;

        String result = accountService.processDeposit("123", new BigDecimal("-100"));

        assertEquals("Failed: Invalid amount", result);
    }

    // =========================================================
    // Withdraw Analysis
    // =========================================================

    @Test
    // Coverage: Withdraw - Path 1 (Decision: Account Not Found)
    public void testWithdraw_AccountNotFound() {
        String result = accountService.processWithdraw("999", new BigDecimal("100"));
        assertEquals("Failed: Account not found", result);
    }

    @Test
    // Coverage: Withdraw - Path 2 (Decision: Success)
    public void testWithdraw_Success() {
        Account account = new Account("123", "John", "john_user", "pass123", 1000.0, "VERIFIED");
        accountRepository.save(account);

        String result = accountService.processWithdraw("123", new BigDecimal("100"));

        assertEquals("Withdrawal successful", result);
        assertEquals(new BigDecimal("900.00"), account.getBalance());
    }

    @Test
    // Coverage: Withdraw - Path 3 (Decision: Policy Exception)
    public void testWithdraw_PolicyFailure() {
        Account account = new Account("123", "John", "john_user", "pass123", 1000.0, "VERIFIED");
        accountRepository.save(account);

        transactionPolicy.shouldThrowWithdrawError = true;

        String result = accountService.processWithdraw("123", new BigDecimal("2000"));

        assertEquals("Failed: Insufficient funds", result);
    }

    // =========================================================
    // Transfer Analysis
    // =========================================================

    @Test
    // Coverage: Transfer - Path 1 (Decision: One/Both Accounts Not Found)
    public void testTransfer_AccountNotFound() {
        Account source = new Account("123", "John", "john_user", "pass123", 1000.0, "VERIFIED");
        accountRepository.save(source);
        // Dest "999" missing

        String result = accountService.processTransfer("123", "999", new BigDecimal("100"));

        assertEquals("Failed: Account not found", result);
    }

    @Test
    // Coverage: Transfer - Path 2 (Decision: Success)
    public void testTransfer_Success() {
        Account source = new Account("123", "John", "john_user", "pass123", 1000.0, "VERIFIED");
        Account dest = new Account("456", "Jane", "jane_user", "pass456", 500.0, "VERIFIED");
        accountRepository.save(source);
        accountRepository.save(dest);

        String result = accountService.processTransfer("123", "456", new BigDecimal("100"));

        assertEquals("Transfer successful", result);
        assertEquals(new BigDecimal("900.00"), source.getBalance());
        assertEquals(new BigDecimal("600.00"), dest.getBalance());
    }

    @Test
    // Coverage: Transfer - Path 3 (Decision: Policy Exception)
    public void testTransfer_PolicyFailure() {
        Account source = new Account("123", "John", "john_user", "pass123", 1000.0, "VERIFIED");
        Account dest = new Account("456", "Jane", "jane_user", "pass456", 500.0, "VERIFIED");
        accountRepository.save(source);
        accountRepository.save(dest);

        transactionPolicy.shouldThrowTransferError = true;

        String result = accountService.processTransfer("123", "456", new BigDecimal("100000"));

        assertEquals("Failed: Transfer failed: limit exceeded", result);
    }

    // =========================================================
    // Manual Stubs Implementation
    // =========================================================

    // Note: AccountRepository is a CLASS, so we must EXTEND it, not start from
    // scratch.
    class StubAccountRepository extends AccountRepository {
        private Map<String, Account> db = new HashMap<>();

        @Override
        public Optional<Account> findByAccountNumber(String accountNumber) {
            return Optional.ofNullable(db.get(accountNumber));
        }

        @Override
        public Optional<Account> findAccountbyUsername(String username) {
            return db.values().stream()
                    .filter(acc -> acc.getUsername().equalsIgnoreCase(username))
                    .findFirst();
        }

        @Override
        public Account save(Account entity) {
            db.put(entity.getAccountNumber(), entity);
            return entity;
        }
    }

    class StubTransactionPolicy implements TransactionPolicy {
        public boolean shouldThrowWithdrawError = false;
        public boolean shouldThrowDepositError = false;
        public boolean shouldThrowTransferError = false;

        @Override
        public void validateWithdraw(Account account, BigDecimal amount) {
            if (shouldThrowWithdrawError) {
                throw new BankingException("Insufficient funds");
            }
        }

        @Override
        public void validateDeposit(Account account, BigDecimal amount) {
            if (shouldThrowDepositError) {
                throw new InvalidAmountException("Invalid amount");
            }
        }

        @Override
        public void validateTransfer(Account source, Account destination, BigDecimal amount) {
            if (shouldThrowTransferError) {
                throw new BankingException("Transfer failed: limit exceeded");
            }
        }
    }
}
