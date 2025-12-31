package com.banking.app;

import com.banking.app.exception.BankingException;
import com.banking.app.exception.InvalidAmountException;
import com.banking.app.model.Account;
import com.banking.app.policy.TransactionPolicy;
import com.banking.app.repository.AccountRepository;
import com.banking.app.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// Simple Mockito usage
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AccountServiceWhiteBoxTest {

    private AccountRepository accountRepository;
    private TransactionPolicy transactionPolicy;
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        // Manual mocking
        accountRepository = mock(AccountRepository.class);
        transactionPolicy = mock(TransactionPolicy.class);
        accountService = new AccountService(accountRepository, transactionPolicy);
    }

    // =========================================================
    // Deposit Analysis
    // =========================================================

    @Test
    // Coverage: Deposit - Path 1 (Decision: Account Not Found)
    public void testDeposit_AccountNotFound() {
        when(accountRepository.findByAccountNumber("999")).thenReturn(Optional.empty());

        String result = accountService.processDeposit("999", new BigDecimal("100"));

        assertEquals("Failed: Account not found", result);
        verify(accountRepository, never()).save(any());
    }

    @Test
    // Coverage: Deposit - Path 2 (Decision: Success)
    public void testDeposit_Success() {
        Account mockAccount = new Account("123", "John", 1000.0, "VERIFIED");
        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(mockAccount));

        doNothing().when(transactionPolicy).validateDeposit(any(), any());

        String result = accountService.processDeposit("123", new BigDecimal("500"));

        assertEquals("Deposit successful", result);
        assertEquals(new BigDecimal("1500.00"), mockAccount.getBalance());
        verify(transactionPolicy).validateDeposit(mockAccount, new BigDecimal("500"));
        verify(accountRepository).save(mockAccount);
    }

    @Test
    // Coverage: Deposit - Path 3 (Decision: Policy Exception)
    public void testDeposit_PolicyFailure() {
        Account mockAccount = new Account("123", "John", 1000.0, "VERIFIED");
        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(mockAccount));

        doThrow(new InvalidAmountException("Invalid amount"))
                .when(transactionPolicy).validateDeposit(any(), any());

        String result = accountService.processDeposit("123", new BigDecimal("-100"));

        assertEquals("Failed: Invalid amount", result);
        verify(accountRepository, never()).save(any());
    }

    // =========================================================
    // Withdraw Analysis
    // =========================================================

    @Test
    // Coverage: Withdraw - Path 1 (Decision: Account Not Found)
    public void testWithdraw_AccountNotFound() {
        when(accountRepository.findByAccountNumber("999")).thenReturn(Optional.empty());

        String result = accountService.processWithdraw("999", new BigDecimal("100"));

        assertEquals("Failed: Account not found", result);
    }

    @Test
    // Coverage: Withdraw - Path 2 (Decision: Success)
    public void testWithdraw_Success() {
        Account mockAccount = new Account("123", "John", 1000.0, "VERIFIED");
        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(mockAccount));

        doNothing().when(transactionPolicy).validateWithdraw(any(), any());

        String result = accountService.processWithdraw("123", new BigDecimal("100"));

        assertEquals("Withdrawal successful", result);
        assertEquals(new BigDecimal("900.00"), mockAccount.getBalance());
        verify(accountRepository).save(mockAccount);
    }

    @Test
    // Coverage: Withdraw - Path 3 (Decision: Policy Exception)
    public void testWithdraw_PolicyFailure() {
        Account mockAccount = new Account("123", "John", 1000.0, "VERIFIED");
        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(mockAccount));

        doThrow(new BankingException("Insufficient funds"))
                .when(transactionPolicy).validateWithdraw(any(), any());

        String result = accountService.processWithdraw("123", new BigDecimal("2000"));

        assertEquals("Failed: Insufficient funds", result);
        verify(accountRepository, never()).save(any());
    }

    // =========================================================
    // Transfer Analysis
    // =========================================================

    @Test
    // Coverage: Transfer - Path 1 (Decision: One/Both Accounts Not Found)
    public void testTransfer_AccountNotFound() {
        when(accountRepository.findByAccountNumber("123"))
                .thenReturn(Optional.of(new Account("123", "dummy", 0.0, "VERIFIED")));
        when(accountRepository.findByAccountNumber("999")).thenReturn(Optional.empty());

        String result = accountService.processTransfer("123", "999", new BigDecimal("100"));

        assertEquals("Failed: Account not found", result);
        verify(accountRepository, never()).save(any());
    }

    @Test
    // Coverage: Transfer - Path 2 (Decision: Success)
    public void testTransfer_Success() {
        Account source = new Account("123", "John", 1000.0, "VERIFIED");
        Account dest = new Account("456", "Jane", 500.0, "VERIFIED");

        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(source));
        when(accountRepository.findByAccountNumber("456")).thenReturn(Optional.of(dest));

        doNothing().when(transactionPolicy).validateTransfer(any(), any(), any());

        String result = accountService.processTransfer("123", "456", new BigDecimal("100"));

        assertEquals("Transfer successful", result);
        assertEquals(new BigDecimal("900.00"), source.getBalance());
        assertEquals(new BigDecimal("600.00"), dest.getBalance());

        verify(accountRepository).save(source);
        verify(accountRepository).save(dest);
    }

    @Test
    // Coverage: Transfer - Path 3 (Decision: Policy Exception)
    public void testTransfer_PolicyFailure() {
        Account source = new Account("123", "John", 1000.0, "VERIFIED");
        Account dest = new Account("456", "Jane", 500.0, "VERIFIED");

        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(source));
        when(accountRepository.findByAccountNumber("456")).thenReturn(Optional.of(dest));

        doThrow(new BankingException("Transfer failed: limit exceeded"))
                .when(transactionPolicy).validateTransfer(any(), any(), any());

        String result = accountService.processTransfer("123", "456", new BigDecimal("100000"));

        assertEquals("Failed: Transfer failed: limit exceeded", result);
        verify(accountRepository, never()).save(any());
    }
}
