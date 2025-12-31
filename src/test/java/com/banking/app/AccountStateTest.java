package com.banking.app;

import com.banking.app.model.Account;
import com.banking.app.model.AccountStatus;
import com.banking.app.repository.AccountRepository;
import com.banking.app.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountStateTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    private static final String ACC_VERIFIED = "123";
    private static final String ACC_UNVERIFIED = "789";
    private static final String ACC_SUSPENDED = "999";
    private static final String ACC_CLOSED_ID = "CLOSED_ACC"; // Will create dynamic

    @BeforeEach
    public void setup() {
        // Reset 123 to VERIFIED, 1000.00
        resetAccount(ACC_VERIFIED, "VERIFIED", 1000.00);

        // Reset 789 to UNVERIFIED, 1000.00
        resetAccount(ACC_UNVERIFIED, "UNVERIFIED", 1000.00);

        // Reset 999 to SUSPENDED, 1000.00
        resetAccount(ACC_SUSPENDED, "SUSPENDED", 1000.00);

        createOrResetAccount("888", "Closed User", "closed_user", "pass", 1000.00, "CLOSED");
    }

    private void resetAccount(String accNum, String statusStr, double balance) {
        Account acc = accountService.getAccount(accNum);
        if (acc != null) {
            acc.setStatus(AccountStatus.valueOf(statusStr));
            acc.setBalance(new BigDecimal(balance));
            accountRepository.save(acc);
        } else {
            // Fallback if not exists (should exist from repo constructor)
            createOrResetAccount(accNum, "Test User", "test_user", "pass", balance, statusStr);
        }
    }

    private void createOrResetAccount(String accNum, String name, String user, String pass, double balance,
            String status) {
        Account acc = new Account(accNum, name, user, pass, balance, status);
        accountRepository.save(acc);
    }

    // =========================================================================
    // UNVERIFIED STATE
    // Deposit: Allowed
    // Withdraw: Blocked
    // Transfer Send: Blocked
    // Transfer Receive: Allowed
    // =========================================================================

    @Test
    public void testUnverified_Deposit_Allowed() {
        String result = accountService.processDeposit(ACC_UNVERIFIED, new BigDecimal("100"));
        assertEquals("Deposit successful", result);
        assertEquals(new BigDecimal("1100.00"), accountService.getAccount(ACC_UNVERIFIED).getBalance());
    }

    @Test
    public void testUnverified_Withdraw_Blocked() {
        String result = accountService.processWithdraw(ACC_UNVERIFIED, new BigDecimal("100"));
        assertTrue(result.contains("Unverified accounts cannot withdraw"),
                "Should contain error about unverified status. Actual: " + result);
    }

    @Test
    public void testUnverified_TransferSend_Blocked() {
        String result = accountService.processTransfer(ACC_UNVERIFIED, ACC_VERIFIED, new BigDecimal("100"));
        assertTrue(result.contains("Unverified accounts cannot initiate"));
    }

    @Test
    public void testUnverified_TransferReceive_Allowed() {
        String result = accountService.processTransfer(ACC_VERIFIED, ACC_UNVERIFIED, new BigDecimal("100"));
        assertEquals("Transfer successful", result);
        assertEquals(new BigDecimal("1100.00"), accountService.getAccount(ACC_UNVERIFIED).getBalance());
    }

    // =========================================================================
    // VERIFIED STATE
    // Deposit: Allowed
    // Withdraw: Allowed
    // Transfer Send: Allowed
    // Transfer Receive: Allowed
    // =========================================================================

    @Test
    public void testVerified_AllAllowed() {
        // Deposit
        assertEquals("Deposit successful", accountService.processDeposit(ACC_VERIFIED, new BigDecimal("100")));

        // Withdraw
        assertEquals("Withdrawal successful", accountService.processWithdraw(ACC_VERIFIED, new BigDecimal("100")));

        // Transfer Send
        assertEquals("Transfer successful",
                accountService.processTransfer(ACC_VERIFIED, ACC_UNVERIFIED, new BigDecimal("100")));

        // Transfer Receive (from Suspended? No, Suspended cant send. From Unverified?
        // No. From another Verified or itself? Cant transfer self.)
        // We'll trust the verified send test covers receive for the other party.
    }

    // =========================================================================
    // SUSPENDED STATE
    // Deposit: Allowed
    // Withdraw: Blocked
    // Transfer Send: Blocked
    // Transfer Receive: Allowed
    // =========================================================================

    @Test
    public void testSuspended_Deposit_Allowed() {
        String result = accountService.processDeposit(ACC_SUSPENDED, new BigDecimal("100"));
        assertEquals("Deposit successful", result);
    }

    @Test
    public void testSuspended_Withdraw_Blocked() {
        String result = accountService.processWithdraw(ACC_SUSPENDED, new BigDecimal("100"));
        assertTrue(result.contains("Your account is suspended"));
    }

    @Test
    public void testSuspended_TransferSend_Blocked() {
        String result = accountService.processTransfer(ACC_SUSPENDED, ACC_VERIFIED, new BigDecimal("100"));
        assertTrue(result.contains("Your account is suspended"));
    }

    @Test
    public void testSuspended_TransferReceive_Allowed() {
        // Verified -> Suspended
        // Note: This relies on the POLICY FIX being applied.
        String result = accountService.processTransfer(ACC_VERIFIED, ACC_SUSPENDED, new BigDecimal("100"));
        assertEquals("Transfer successful", result);
    }

    // =========================================================================
    // CLOSED STATE
    // All Blocked
    // =========================================================================

    @Test
    public void testClosed_AllBlocked() {
        String closedId = "888";

        // Deposit
        String resDep = accountService.processDeposit(closedId, new BigDecimal("100"));
        assertTrue(resDep.contains("Your account is closed"));

        // Withdraw
        String resWith = accountService.processWithdraw(closedId, new BigDecimal("100"));
        assertTrue(resWith.contains("Your account is closed"));

        // Transfer Send
        String resSend = accountService.processTransfer(closedId, ACC_VERIFIED, new BigDecimal("100"));
        assertTrue(resSend.contains("Your account is closed"));

        // Transfer Receive
        String resRecv = accountService.processTransfer(ACC_VERIFIED, closedId, new BigDecimal("100"));
        assertTrue(resRecv.contains("Destination account is closed"));
    }
}
