package com.banking.app;

import com.banking.app.model.Account;
import com.banking.app.repository.AccountRepository;
import com.banking.app.service.AccountService;
import com.banking.app.service.CreditCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AccountOperationsTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setup() {
        // Reset accounts to known state
        updateBalance("123", new BigDecimal("20000"));
        System.out.println("Account 123 reset to 20000");

        // Ensure destination account "456" exists for transfers
        Account destAccount = accountService.getAccount("456");
        if (destAccount != null) {
            updateBalance("456", new BigDecimal("1000"));
            System.out.println("Account 456 reset to 1000");
        } else {
            System.err.println("Account 456 not found");
        }
    }

    private void updateBalance(String accountNumber, BigDecimal balance) {
        Account account = accountService.getAccount(accountNumber);
        if (account != null) {
            account.setBalance(balance);
            accountRepository.save(account);
        }
    }

    // ==========================================
    // 2.1 Deposit Tests
    // ==========================================

    @Test
    public void testDepositBoundaryValues() {
        // 9999 -> Success
        assertEquals("Deposit successful", accountService.processDeposit("123", new BigDecimal("9999")));

        // 10000 -> Success
        assertEquals("Deposit successful", accountService.processDeposit("123", new BigDecimal("10000")));

        // 10001 -> Fail
        assertTrue(accountService.processDeposit("123", new BigDecimal("10001"))
                .contains("Deposit failed: Amount must be less than 10000EGP"));

        // 99 -> Fail
        assertTrue(accountService.processDeposit("123", new BigDecimal("99"))
                .contains("Deposit failed: Amount must be at least 100EGP"));

        // 100 -> Success
        assertEquals("Deposit successful", accountService.processDeposit("123", new BigDecimal("100")));

        // 101 -> Success
        assertEquals("Deposit successful", accountService.processDeposit("123", new BigDecimal("101")));
    }

    @Test
    public void testDepositEquivalencePartitioning() {
        // Valid Range (5000) -> Success
        assertEquals("Deposit successful", accountService.processDeposit("123", new BigDecimal("5000")));

        // Invalid (> Max) (1000000) -> Fail
        assertTrue(accountService.processDeposit("123", new BigDecimal("1000000"))
                .contains("Deposit failed: Amount must be less than 10000EGP"));

        // Invalid (Negative) (-1000) -> Fail
        assertTrue(accountService.processDeposit("123", new BigDecimal("-1000"))
                .contains("Deposit failed: Amount must be greater than zero"));

        // Invalid (< Min) (50) -> Fail
        assertTrue(accountService.processDeposit("123", new BigDecimal("50"))
                .contains("Deposit failed: Amount must be at least 100EGP"));
    }

    // 2.1.3 Equivalence Partitioning (Payment Gateway)
    // Note: We test this via CreditCardService which delegates to AccountService on
    // success
    @Test
    public void testDepositPaymentGateway() {
        // Valid Payment (...1234)
        assertEquals("Deposit successful",
                creditCardService.processCardDeposit("123", "1234123412341234", "123", "12/26", new BigDecimal("100")));

        // Card Expired (...0000)
        try {
            creditCardService.processCardDeposit("123", "1234123412340000", "123", "12/26", new BigDecimal("100"));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("expired"));
        }

        // Insufficient Funds Card (...1111)
        try {
            creditCardService.processCardDeposit("123", "1234123412341111", "123", "12/26", new BigDecimal("100"));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Insufficient funds"));
        }

        // Fraud Risk (...9999)
        try {
            creditCardService.processCardDeposit("123", "1234123412349999", "123", "12/26", new BigDecimal("100"));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("blocked"));
        }

        // Network Error (...8888)
        try {
            creditCardService.processCardDeposit("123", "1234123412348888", "123", "12/26", new BigDecimal("100"));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("network"));
        }

        // Gateway Timeout (...7777)
        try {
            creditCardService.processCardDeposit("123", "1234123412347777", "123", "12/26", new BigDecimal("100"));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("time out") || e.getMessage().contains("timed out"));
        }
    }

    // ==========================================
    // 2.2 Withdraw Tests
    // ==========================================

    @Test
    public void testWithdrawBoundaryValues() {
        // Set balance to 5000 for this test
        updateBalance("123", new BigDecimal("5000"));

        // 4999 -> Success
        assertEquals("Withdrawal successful", accountService.processWithdraw("123", new BigDecimal("4999")));

        // Reset balance
        updateBalance("123", new BigDecimal("5000"));

        // 5000 -> Success
        assertEquals("Withdrawal successful", accountService.processWithdraw("123", new BigDecimal("5000")));

        // Reset balance
        updateBalance("123", new BigDecimal("5000"));

        // 5001 -> Fail (Insufficient funds)
        assertTrue(accountService.processWithdraw("123", new BigDecimal("5001"))
                .contains("Insufficient funds"));

        // 99 -> Fail
        assertTrue(accountService.processWithdraw("123", new BigDecimal("99"))
                .contains("Withdrawal failed: Amount must be at least 100EGP"));

        // 100 -> Success
        assertEquals("Withdrawal successful", accountService.processWithdraw("123", new BigDecimal("100")));

        // 101 -> Success
        assertEquals("Withdrawal successful", accountService.processWithdraw("123", new BigDecimal("101")));
    }

    @Test
    public void testWithdrawEquivalencePartitioning() {
        // Set balance to 5000
        updateBalance("123", new BigDecimal("5000"));

        // Valid Range (5000) -> Success
        assertEquals("Withdrawal successful", accountService.processWithdraw("123", new BigDecimal("5000")));

        updateBalance("123", new BigDecimal("5000")); // Reset

        // Invalid (> Balance) (6000) -> Fail
        assertTrue(accountService.processWithdraw("123", new BigDecimal("6000"))
                .contains("Insufficient funds"));

        // Invalid (Negative) (-1000) -> Fail
        assertTrue(accountService.processWithdraw("123", new BigDecimal("-1000"))
                .contains("Amount must be greater than zero"));

        // Invalid (< Min) (50) -> Fail
        assertTrue(accountService.processWithdraw("123", new BigDecimal("50"))
                .contains("Amount must be at least 100EGP"));
    }

    // ==========================================
    // 2.3 Transfer Tests
    // ==========================================

    @Test
    public void testTransferBoundaryValues() {
        // 9999 -> Success
        assertEquals("Transfer successful", accountService.processTransfer("123", "456", new BigDecimal("9999")));
        setup(); // Reset

        // 10000 -> Success
        assertEquals("Transfer successful", accountService.processTransfer("123", "456", new BigDecimal("10000")));
        setup();

        // 10001 -> Fail
        assertTrue(accountService.processTransfer("123", "456", new BigDecimal("10001"))
                .contains("Transfer failed: Amount must be less than 10000EGP"));

        // 99 -> Fail
        assertTrue(accountService.processTransfer("123", "456", new BigDecimal("99"))
                .contains("Transfer failed: Amount must be at least 100EGP"));

        // 100 -> Success
        assertEquals("Transfer successful", accountService.processTransfer("123", "456", new BigDecimal("100")));

        // 101 -> Success
        assertEquals("Transfer successful", accountService.processTransfer("123", "456", new BigDecimal("101")));
    }

    @Test
    public void testTransferEquivalencePartitioning() {
        // Valid Range + valid WID (5000, 456)
        assertEquals("Transfer successful", accountService.processTransfer("123", "456", new BigDecimal("5000")));

        // Invalid WID (5000, invalid)
        assertTrue(accountService.processTransfer("123", "invalid_account", new BigDecimal("5000"))
                .contains("Account not found"));

        // Invalid WID src = dst (5000, 123)
        assertTrue(accountService.processTransfer("123", "123", new BigDecimal("5000"))
                .contains("Cannot transfer money to the same account"));
    }
}
