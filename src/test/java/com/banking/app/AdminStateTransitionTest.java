package com.banking.app;

import com.banking.app.model.Account;
import com.banking.app.model.AccountStatus;
import com.banking.app.repository.AccountRepository;
import com.banking.app.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * State-Based Testing for Admin State Transitions
 * 
 * State Diagram:
 * - Unverified -verify-> Verified
 * - Verified -violation-> Closed
 * - Verified -AdminAction-> Closed
 * - Unverified -Violation-> Suspended
 * - Suspended -AdminAction-> Closed
 * - Closed -Appeal-> Suspended
 */
@SpringBootTest
@DisplayName("Admin State Transition Tests")
public class AdminStateTransitionTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    private static final String ACC_UNVERIFIED = "UNV001";
    private static final String ACC_VERIFIED = "VER001";
    private static final String ACC_SUSPENDED = "SUS001";
    private static final String ACC_CLOSED = "CLO001";

    @BeforeEach
    public void setup() {
        // Create test accounts with specific states
        createOrResetAccount(ACC_UNVERIFIED, "Unverified User", "unv_user", "pass", 1000.00, "UNVERIFIED");
        createOrResetAccount(ACC_VERIFIED, "Verified User", "ver_user", "pass", 1000.00, "VERIFIED");
        createOrResetAccount(ACC_SUSPENDED, "Suspended User", "sus_user", "pass", 1000.00, "SUSPENDED");
        createOrResetAccount(ACC_CLOSED, "Closed User", "clo_user", "pass", 1000.00, "CLOSED");
    }

    private void createOrResetAccount(String accNum, String name, String user, String pass, double balance,
            String status) {
        Account acc = accountService.getAccount(accNum);
        if (acc != null) {
            acc.setStatus(AccountStatus.valueOf(status));
            acc.setBalance(new BigDecimal(balance));
            accountRepository.save(acc);
        } else {
            acc = new Account(accNum, name, user, pass, balance, status);
            accountRepository.save(acc);
        }
    }

    // =========================================================================
    // VALID STATE TRANSITIONS
    // =========================================================================

    @Test
    @DisplayName("Transition: Unverified -> Verified (via verify)")
    public void testUnverified_To_Verified_Verify() {
        String result = accountService.processVerify(ACC_UNVERIFIED);
        assertEquals("Verification successful", result);
        
        Account account = accountService.getAccount(ACC_UNVERIFIED);
        assertEquals(AccountStatus.VERIFIED, account.getStatus());
    }

    @Test
    @DisplayName("Transition: Unverified -> Suspended (via violation)")
    public void testUnverified_To_Suspended_Violation() {
        String result = accountService.processViolation(ACC_UNVERIFIED);
        assertEquals("Violation processed successfully", result);
        
        Account account = accountService.getAccount(ACC_UNVERIFIED);
        assertEquals(AccountStatus.SUSPENDED, account.getStatus());
    }

    @Test
    @DisplayName("Transition: Verified -> Closed (via violation)")
    public void testVerified_To_Closed_Violation() {
        String result = accountService.processViolation(ACC_VERIFIED);
        assertEquals("Violation processed successfully", result);
        
        Account account = accountService.getAccount(ACC_VERIFIED);
        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    @DisplayName("Transition: Verified -> Closed (via adminAction)")
    public void testVerified_To_Closed_AdminAction() {
        String result = accountService.processAdminAction(ACC_VERIFIED);
        assertEquals("Admin action processed successfully", result);
        
        Account account = accountService.getAccount(ACC_VERIFIED);
        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    @DisplayName("Transition: Suspended -> Closed (via adminAction)")
    public void testSuspended_To_Closed_AdminAction() {
        String result = accountService.processAdminAction(ACC_SUSPENDED);
        assertEquals("Admin action processed successfully", result);
        
        Account account = accountService.getAccount(ACC_SUSPENDED);
        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    @DisplayName("Transition: Closed -> Suspended (via appeal)")
    public void testClosed_To_Suspended_Appeal() {
        String result = accountService.processAppeal(ACC_CLOSED);
        assertEquals("Appeal processed successfully", result);
        
        Account account = accountService.getAccount(ACC_CLOSED);
        assertEquals(AccountStatus.SUSPENDED, account.getStatus());
    }

    // =========================================================================
    // INVALID STATE TRANSITIONS (Should Fail)
    // =========================================================================

    @Test
    @DisplayName("Invalid: Verify on already Verified account")
    public void testInvalid_Verify_AlreadyVerified() {
        // Account is already VERIFIED from setup
        String result = accountService.processVerify(ACC_VERIFIED);
        assertTrue(result.contains("Failed"), "Should fail when verifying already verified account");
    }

    @Test
    @DisplayName("Invalid: Verify on Suspended account")
    public void testInvalid_Verify_Suspended() {
        String result = accountService.processVerify(ACC_SUSPENDED);
        assertTrue(result.contains("Failed"), "Should fail when verifying suspended account");
    }

    @Test
    @DisplayName("Invalid: Verify on Closed account")
    public void testInvalid_Verify_Closed() {
        String result = accountService.processVerify(ACC_CLOSED);
        assertTrue(result.contains("Failed"), "Should fail when verifying closed account");
    }

    @Test
    @DisplayName("Invalid: Violation on Suspended account")
    public void testInvalid_Violation_Suspended() {
        String result = accountService.processViolation(ACC_SUSPENDED);
        assertTrue(result.contains("Failed"), "Should fail when applying violation to suspended account");
    }

    @Test
    @DisplayName("Invalid: Violation on Closed account")
    public void testInvalid_Violation_Closed() {
        String result = accountService.processViolation(ACC_CLOSED);
        assertTrue(result.contains("Failed"), "Should fail when applying violation to closed account");
    }

    @Test
    @DisplayName("Invalid: AdminAction on Unverified account")
    public void testInvalid_AdminAction_Unverified() {
        String result = accountService.processAdminAction(ACC_UNVERIFIED);
        assertTrue(result.contains("Failed"), "Should fail when applying admin action to unverified account");
    }

    @Test
    @DisplayName("Invalid: AdminAction on Closed account")
    public void testInvalid_AdminAction_Closed() {
        String result = accountService.processAdminAction(ACC_CLOSED);
        assertTrue(result.contains("Failed"), "Should fail when applying admin action to already closed account");
    }

    @Test
    @DisplayName("Invalid: Appeal on Unverified account")
    public void testInvalid_Appeal_Unverified() {
        String result = accountService.processAppeal(ACC_UNVERIFIED);
        assertTrue(result.contains("Failed"), "Should fail when appealing unverified account");
    }

    @Test
    @DisplayName("Invalid: Appeal on Verified account")
    public void testInvalid_Appeal_Verified() {
        String result = accountService.processAppeal(ACC_VERIFIED);
        assertTrue(result.contains("Failed"), "Should fail when appealing verified account");
    }

    @Test
    @DisplayName("Invalid: Appeal on Suspended account")
    public void testInvalid_Appeal_Suspended() {
        String result = accountService.processAppeal(ACC_SUSPENDED);
        assertTrue(result.contains("Failed"), "Should fail when appealing suspended account");
    }

    // =========================================================================
    // TRANSACTION BEHAVIOR AFTER STATE TRANSITIONS
    // =========================================================================

    @Test
    @DisplayName("After Unverified->Verified: Account can now withdraw")
    public void testAfterVerify_CanWithdraw() {
        // Transition to verified
        accountService.processVerify(ACC_UNVERIFIED);
        
        // Now should be able to withdraw
        String result = accountService.processWithdraw(ACC_UNVERIFIED, new BigDecimal("100"));
        assertEquals("Withdrawal successful", result);
    }

    @Test
    @DisplayName("After Unverified->Suspended: Account cannot withdraw")
    public void testAfterViolationToSuspended_CannotWithdraw() {
        // Transition to suspended via violation
        accountService.processViolation(ACC_UNVERIFIED);
        
        // Should not be able to withdraw
        String result = accountService.processWithdraw(ACC_UNVERIFIED, new BigDecimal("100"));
        assertTrue(result.contains("suspended"), "Suspended account should not be able to withdraw");
    }

    @Test
    @DisplayName("After Verified->Closed: Account cannot perform any transactions")
    public void testAfterVerifiedToClosed_AllBlocked() {
        // Transition to closed via admin action
        accountService.processAdminAction(ACC_VERIFIED);
        
        // All operations should be blocked
        String depositResult = accountService.processDeposit(ACC_VERIFIED, new BigDecimal("100"));
        assertTrue(depositResult.contains("closed"), "Closed account should not accept deposits");
        
        String withdrawResult = accountService.processWithdraw(ACC_VERIFIED, new BigDecimal("100"));
        assertTrue(withdrawResult.contains("closed"), "Closed account should not allow withdrawals");
    }

    @Test
    @DisplayName("After Closed->Suspended: Account can receive deposits but not withdraw")
    public void testAfterAppeal_CanReceiveButNotWithdraw() {
        // Transition from closed to suspended via appeal
        accountService.processAppeal(ACC_CLOSED);
        
        // Should be able to receive deposits
        String depositResult = accountService.processDeposit(ACC_CLOSED, new BigDecimal("100"));
        assertEquals("Deposit successful", depositResult);
        
        // Should NOT be able to withdraw
        String withdrawResult = accountService.processWithdraw(ACC_CLOSED, new BigDecimal("100"));
        assertTrue(withdrawResult.contains("suspended"), "Suspended account should not be able to withdraw");
    }

    @Test
    @DisplayName("After Suspended->Closed: Account completely blocked")
    public void testAfterSuspendedToClosed_AllBlocked() {
        // Transition from suspended to closed
        accountService.processAdminAction(ACC_SUSPENDED);
        
        // All operations should be blocked
        String depositResult = accountService.processDeposit(ACC_SUSPENDED, new BigDecimal("100"));
        assertTrue(depositResult.contains("closed"), "Closed account should not accept deposits");
    }

    // =========================================================================
    // STATE TRANSITION MATRIX TEST
    // =========================================================================

    @Test
    @DisplayName("State Transition Matrix: All valid transitions")
    public void testStateTransitionMatrix_ValidTransitions() {
        // Create fresh accounts for matrix testing
        String testAcc1 = "MATRIX1";
        String testAcc2 = "MATRIX2";
        String testAcc3 = "MATRIX3";
        String testAcc4 = "MATRIX4";
        String testAcc5 = "MATRIX5";
        String testAcc6 = "MATRIX6";

        // Matrix Row 1: Unverified -> Verified
        createOrResetAccount(testAcc1, "Test1", "test1", "pass", 1000.00, "UNVERIFIED");
        accountService.processVerify(testAcc1);
        assertEquals(AccountStatus.VERIFIED, accountService.getAccount(testAcc1).getStatus());

        // Matrix Row 2: Unverified -> Suspended (via violation)
        createOrResetAccount(testAcc2, "Test2", "test2", "pass", 1000.00, "UNVERIFIED");
        accountService.processViolation(testAcc2);
        assertEquals(AccountStatus.SUSPENDED, accountService.getAccount(testAcc2).getStatus());

        // Matrix Row 3: Verified -> Closed (via violation)
        createOrResetAccount(testAcc3, "Test3", "test3", "pass", 1000.00, "VERIFIED");
        accountService.processViolation(testAcc3);
        assertEquals(AccountStatus.CLOSED, accountService.getAccount(testAcc3).getStatus());

        // Matrix Row 4: Verified -> Closed (via adminAction)
        createOrResetAccount(testAcc4, "Test4", "test4", "pass", 1000.00, "VERIFIED");
        accountService.processAdminAction(testAcc4);
        assertEquals(AccountStatus.CLOSED, accountService.getAccount(testAcc4).getStatus());

        // Matrix Row 5: Suspended -> Closed (via adminAction)
        createOrResetAccount(testAcc5, "Test5", "test5", "pass", 1000.00, "SUSPENDED");
        accountService.processAdminAction(testAcc5);
        assertEquals(AccountStatus.CLOSED, accountService.getAccount(testAcc5).getStatus());

        // Matrix Row 6: Closed -> Suspended (via appeal)
        createOrResetAccount(testAcc6, "Test6", "test6", "pass", 1000.00, "CLOSED");
        accountService.processAppeal(testAcc6);
        assertEquals(AccountStatus.SUSPENDED, accountService.getAccount(testAcc6).getStatus());
    }

    // =========================================================================
    // EDGE CASES: Multiple Transitions
    // =========================================================================

    @Test
    @DisplayName("Edge Case: Unverified -> Verified -> Closed (via adminAction)")
    public void testMultipleTransitions_UnverifiedToVerifiedToClosed() {
        // Step 1: Unverified -> Verified
        accountService.processVerify(ACC_UNVERIFIED);
        assertEquals(AccountStatus.VERIFIED, accountService.getAccount(ACC_UNVERIFIED).getStatus());
        
        // Step 2: Verified -> Closed
        accountService.processAdminAction(ACC_UNVERIFIED);
        assertEquals(AccountStatus.CLOSED, accountService.getAccount(ACC_UNVERIFIED).getStatus());
    }

    @Test
    @DisplayName("Edge Case: Unverified -> Suspended -> Closed -> Suspended (via appeal)")
    public void testMultipleTransitions_UnverifiedToSuspendedToClosedToSuspended() {
        // Step 1: Unverified -> Suspended
        accountService.processViolation(ACC_UNVERIFIED);
        assertEquals(AccountStatus.SUSPENDED, accountService.getAccount(ACC_UNVERIFIED).getStatus());
        
        // Step 2: Suspended -> Closed
        accountService.processAdminAction(ACC_UNVERIFIED);
        assertEquals(AccountStatus.CLOSED, accountService.getAccount(ACC_UNVERIFIED).getStatus());
        
        // Step 3: Closed -> Suspended (via appeal)
        accountService.processAppeal(ACC_UNVERIFIED);
        assertEquals(AccountStatus.SUSPENDED, accountService.getAccount(ACC_UNVERIFIED).getStatus());
    }

    @Test
    @DisplayName("Edge Case: Verify on already verified account should fail")
    public void testEdgeCase_DoubleVerify() {
        // First verify
        accountService.processVerify(ACC_UNVERIFIED);
        assertEquals(AccountStatus.VERIFIED, accountService.getAccount(ACC_UNVERIFIED).getStatus());
        
        // Second verify should fail
        String result = accountService.processVerify(ACC_UNVERIFIED);
        assertTrue(result.contains("Failed"), "Should not allow verifying an already verified account");
        assertEquals(AccountStatus.VERIFIED, accountService.getAccount(ACC_UNVERIFIED).getStatus());
    }

    // =========================================================================
    // STATE TRANSITION MATRIX DOCUMENTATION
    // =========================================================================
    
    /**
     * STATE TRANSITION MATRIX
     * 
     * Current State | Action        | Next State | Valid? | Test Method
     * --------------|---------------|------------|--------|------------------
     * UNVERIFIED    | verify        | VERIFIED   | Yes    | testUnverified_To_Verified_Verify
     * UNVERIFIED    | violation     | SUSPENDED  | Yes    | testUnverified_To_Suspended_Violation
     * VERIFIED      | violation     | CLOSED     | Yes    | testVerified_To_Closed_Violation
     * VERIFIED      | adminAction   | CLOSED     | Yes    | testVerified_To_Closed_AdminAction
     * SUSPENDED     | adminAction   | CLOSED     | Yes    | testSuspended_To_Closed_AdminAction
     * CLOSED        | appeal        | SUSPENDED  | Yes    | testClosed_To_Suspended_Appeal
     * 
     * INVALID TRANSITIONS:
     * VERIFIED      | verify        | -          | No     | testInvalid_Verify_AlreadyVerified
     * SUSPENDED     | verify        | -          | No     | testInvalid_Verify_Suspended
     * CLOSED        | verify        | -          | No     | testInvalid_Verify_Closed
     * SUSPENDED     | violation     | -          | No     | testInvalid_Violation_Suspended
     * CLOSED        | violation     | -          | No     | testInvalid_Violation_Closed
     * UNVERIFIED    | adminAction   | -          | No     | testInvalid_AdminAction_Unverified
     * CLOSED        | adminAction   | -          | No     | testInvalid_AdminAction_Closed
     * UNVERIFIED    | appeal        | -          | No     | testInvalid_Appeal_Unverified
     * VERIFIED      | appeal        | -          | No     | testInvalid_Appeal_Verified
     * SUSPENDED     | appeal        | -          | No     | testInvalid_Appeal_Suspended
     */
}

