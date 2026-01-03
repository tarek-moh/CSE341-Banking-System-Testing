package com.banking.app;

import com.banking.app.controller.AdminController;
import com.banking.app.model.Account;
import com.banking.app.model.AccountStatus;
import com.banking.app.repository.AccountRepository;
import com.banking.app.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Black-Box Testing for Admin Functionality
 * 
 * Testing from the user's perspective without knowledge of internal implementation.
 * Focus on:
 * - Input/Output behavior
 * - Equivalence partitioning
 * - Boundary value analysis
 * - Error handling from user perspective
 */
@SpringBootTest
@DisplayName("Admin Black-Box Tests")
public class AdminBlackBoxTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AdminController adminController;

    private HttpSession adminSession;
    private HttpSession regularUserSession;
    private HttpSession noSession;
    private Model model;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String REGULAR_USERNAME = "johndoe";
    private static final String REGULAR_PASSWORD = "123456";

    @BeforeEach
    public void setup() {
        // Create admin session
        adminSession = new MockHttpSession();
        Account adminAccount = accountService.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        adminSession.setAttribute("loggedInUser", adminAccount);

        // Create regular user session
        regularUserSession = new MockHttpSession();
        Account regularAccount = accountService.login(REGULAR_USERNAME, REGULAR_PASSWORD);
        regularUserSession.setAttribute("loggedInUser", regularAccount);

        // Empty session
        noSession = new MockHttpSession();

        // Mock model
        model = mock(Model.class);

        // Create test accounts with different states
        createTestAccount("TEST_UNV", "Unverified Test", "test_unv", "pass", 1000.00, "UNVERIFIED");
        createTestAccount("TEST_VER", "Verified Test", "test_ver", "pass", 1000.00, "VERIFIED");
        createTestAccount("TEST_SUS", "Suspended Test", "test_sus", "pass", 1000.00, "SUSPENDED");
        createTestAccount("TEST_CLO", "Closed Test", "test_clo", "pass", 1000.00, "CLOSED");
    }

    private void createTestAccount(String accNum, String name, String user, String pass, double balance, String status) {
        Account acc = accountService.getAccount(accNum);
        if (acc == null) {
            acc = new Account(accNum, name, user, pass, balance, status);
            accountRepository.save(acc);
        } else {
            acc.setStatus(AccountStatus.valueOf(status));
            acc.setBalance(new BigDecimal(balance));
            accountRepository.save(acc);
        }
    }

    // =========================================================================
    // EQUIVALENCE PARTITIONING: Admin Login
    // =========================================================================

    @Test
    @DisplayName("EP-01: Valid admin credentials - should login successfully")
    public void testAdminLogin_ValidCredentials() {
        // Input: Valid admin username and password
        Account result = accountService.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        // Expected: Admin account returned
        assertNotNull(result);
        assertTrue(result.isAdmin());
        assertEquals(ADMIN_USERNAME, result.getUsername());
    }

    @Test
    @DisplayName("EP-02: Invalid admin username - should fail")
    public void testAdminLogin_InvalidUsername() {
        // Input: Invalid username
        Exception exception = assertThrows(Exception.class, () -> {
            accountService.login("invalid_admin", ADMIN_PASSWORD);
        });

        // Expected: Error about user not found
        assertTrue(exception.getMessage().contains("User not found") || 
                   exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("EP-03: Valid username, wrong password - should fail")
    public void testAdminLogin_WrongPassword() {
        // Input: Valid username but wrong password
        Exception exception = assertThrows(Exception.class, () -> {
            accountService.login(ADMIN_USERNAME, "wrong_password");
        });

        // Expected: Error about invalid password
        assertTrue(exception.getMessage().contains("Invalid password") ||
                   exception.getMessage().contains("password"));
    }

    @Test
    @DisplayName("EP-04: Empty username - should fail")
    public void testAdminLogin_EmptyUsername() {
        // Input: Empty username
        Exception exception = assertThrows(Exception.class, () -> {
            accountService.login("", ADMIN_PASSWORD);
        });

        // Expected: Error about user not found
        assertNotNull(exception);
    }

    @Test
    @DisplayName("EP-05: Empty password - should fail")
    public void testAdminLogin_EmptyPassword() {
        // Input: Valid username but empty password
        Exception exception = assertThrows(Exception.class, () -> {
            accountService.login(ADMIN_USERNAME, "");
        });

        // Expected: Error about invalid password
        assertNotNull(exception);
    }

    // =========================================================================
    // EQUIVALENCE PARTITIONING: Admin Dashboard Access
    // =========================================================================

    @Test
    @DisplayName("EP-06: Admin user accessing dashboard - should succeed")
    public void testAdminDashboard_AdminAccess() {
        // Input: Admin session
        String result = adminController.admin(adminSession, model);

        // Expected: Admin dashboard view
        assertEquals("admin", result);
        verify(model, atLeastOnce()).addAttribute(eq("accounts"), any());
    }

    @Test
    @DisplayName("EP-07: Regular user accessing admin dashboard - should redirect")
    public void testAdminDashboard_RegularUserAccess() {
        // Input: Regular user session
        String result = adminController.admin(regularUserSession, model);

        // Expected: Redirect to login
        assertEquals("redirect:/login", result);
    }

    @Test
    @DisplayName("EP-08: No session accessing admin dashboard - should redirect")
    public void testAdminDashboard_NoSession() {
        // Input: No session
        String result = adminController.admin(noSession, model);

        // Expected: Redirect to login
        assertEquals("redirect:/login", result);
    }

    @Test
    @DisplayName("EP-09: Null session accessing admin dashboard - should redirect")
    public void testAdminDashboard_NullSession() {
        // Input: Null session
        String result = adminController.admin(null, model);

        // Expected: Redirect to login
        assertEquals("redirect:/login", result);
    }

    // =========================================================================
    // EQUIVALENCE PARTITIONING: Admin Actions - Verify
    // =========================================================================

    @Test
    @DisplayName("EP-10: Admin verifies unverified account - should succeed")
    public void testAdminVerify_UnverifiedAccount() {
        // Input: Unverified account number
        String result = adminController.verifyAccount("TEST_UNV", adminSession, model);

        // Expected: Success and redirect
        assertTrue(result.contains("redirect:/admin"));
        
        // Verify account is now verified
        Account account = accountService.getAccount("TEST_UNV");
        assertEquals(AccountStatus.VERIFIED, account.getStatus());
    }

    @Test
    @DisplayName("EP-11: Admin verifies already verified account - should fail")
    public void testAdminVerify_AlreadyVerified() {
        // Input: Already verified account
        String result = adminController.verifyAccount("TEST_VER", adminSession, model);

        // Expected: Failure message
        assertTrue(result.contains("redirect:/admin"));
        // The account should remain verified
        Account account = accountService.getAccount("TEST_VER");
        assertEquals(AccountStatus.VERIFIED, account.getStatus());
    }

    @Test
    @DisplayName("EP-12: Regular user tries to verify - should redirect")
    public void testAdminVerify_RegularUser() {
        // Input: Regular user session
        String result = adminController.verifyAccount("TEST_UNV", regularUserSession, model);

        // Expected: Redirect to login
        assertEquals("redirect:/login", result);
    }

    @Test
    @DisplayName("EP-13: Admin verifies non-existent account - should fail")
    public void testAdminVerify_NonExistentAccount() {
        // Input: Non-existent account number
        String result = adminController.verifyAccount("NON_EXISTENT", adminSession, model);

        // Expected: Failure message
        assertTrue(result.contains("redirect:/admin"));
    }

    // =========================================================================
    // EQUIVALENCE PARTITIONING: Admin Actions - Violation
    // =========================================================================

    @Test
    @DisplayName("EP-14: Admin applies violation to unverified account - should suspend")
    public void testAdminViolation_UnverifiedAccount() {
        // Input: Unverified account
        String result = adminController.applyViolation("TEST_UNV", adminSession, model);

        // Expected: Account becomes suspended
        assertTrue(result.contains("redirect:/admin"));
        Account account = accountService.getAccount("TEST_UNV");
        assertEquals(AccountStatus.SUSPENDED, account.getStatus());
    }

    @Test
    @DisplayName("EP-15: Admin applies violation to verified account - should close")
    public void testAdminViolation_VerifiedAccount() {
        // Input: Verified account
        String result = adminController.applyViolation("TEST_VER", adminSession, model);

        // Expected: Account becomes closed
        assertTrue(result.contains("redirect:/admin"));
        Account account = accountService.getAccount("TEST_VER");
        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    @DisplayName("EP-16: Admin applies violation to suspended account - should fail")
    public void testAdminViolation_SuspendedAccount() {
        // Input: Suspended account
        String result = adminController.applyViolation("TEST_SUS", adminSession, model);

        // Expected: Failure (violation cannot be applied to suspended)
        assertTrue(result.contains("redirect:/admin"));
        // Account should remain suspended
        Account account = accountService.getAccount("TEST_SUS");
        assertEquals(AccountStatus.SUSPENDED, account.getStatus());
    }

    // =========================================================================
    // EQUIVALENCE PARTITIONING: Admin Actions - Admin Action (Close)
    // =========================================================================

    @Test
    @DisplayName("EP-17: Admin closes verified account - should succeed")
    public void testAdminAction_VerifiedAccount() {
        // Input: Verified account
        String result = adminController.applyAdminAction("TEST_VER", adminSession, model);

        // Expected: Account becomes closed
        assertTrue(result.contains("redirect:/admin"));
        Account account = accountService.getAccount("TEST_VER");
        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    @DisplayName("EP-18: Admin closes suspended account - should succeed")
    public void testAdminAction_SuspendedAccount() {
        // Input: Suspended account
        String result = adminController.applyAdminAction("TEST_SUS", adminSession, model);

        // Expected: Account becomes closed
        assertTrue(result.contains("redirect:/admin"));
        Account account = accountService.getAccount("TEST_SUS");
        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    @DisplayName("EP-19: Admin closes unverified account - should fail")
    public void testAdminAction_UnverifiedAccount() {
        // Input: Unverified account
        String result = adminController.applyAdminAction("TEST_UNV", adminSession, model);

        // Expected: Failure (admin action cannot be applied to unverified)
        assertTrue(result.contains("redirect:/admin"));
        // Account should remain unverified
        Account account = accountService.getAccount("TEST_UNV");
        assertEquals(AccountStatus.UNVERIFIED, account.getStatus());
    }

    // =========================================================================
    // EQUIVALENCE PARTITIONING: Admin Actions - Appeal
    // =========================================================================

    @Test
    @DisplayName("EP-20: Admin processes appeal for closed account - should suspend")
    public void testAdminAppeal_ClosedAccount() {
        // Input: Closed account
        String result = adminController.processAppeal("TEST_CLO", adminSession, model);

        // Expected: Account becomes suspended
        assertTrue(result.contains("redirect:/admin"));
        Account account = accountService.getAccount("TEST_CLO");
        assertEquals(AccountStatus.SUSPENDED, account.getStatus());
    }

    @Test
    @DisplayName("EP-21: Admin processes appeal for verified account - should fail")
    public void testAdminAppeal_VerifiedAccount() {
        // Input: Verified account (not closed)
        String result = adminController.processAppeal("TEST_VER", adminSession, model);

        // Expected: Failure (appeal only works on closed accounts)
        assertTrue(result.contains("redirect:/admin"));
        // Account should remain verified
        Account account = accountService.getAccount("TEST_VER");
        assertEquals(AccountStatus.VERIFIED, account.getStatus());
    }

    // =========================================================================
    // BOUNDARY VALUE ANALYSIS
    // =========================================================================

    @Test
    @DisplayName("BVA-01: Account number with minimum length")
    public void testBoundary_MinimumAccountNumber() {
        // Input: Single character account number
        String result = adminController.verifyAccount("1", adminSession, model);
        
        // Expected: Should handle gracefully (either success or failure, but no crash)
        assertNotNull(result);
    }

    @Test
    @DisplayName("BVA-02: Account number with maximum reasonable length")
    public void testBoundary_MaximumAccountNumber() {
        // Input: Very long account number
        String longAccountNumber = "A".repeat(100);
        String result = adminController.verifyAccount(longAccountNumber, adminSession, model);
        
        // Expected: Should handle gracefully
        assertNotNull(result);
    }

    @Test
    @DisplayName("BVA-03: Empty account number")
    public void testBoundary_EmptyAccountNumber() {
        // Input: Empty string
        String result = adminController.verifyAccount("", adminSession, model);
        
        // Expected: Should handle gracefully
        assertNotNull(result);
    }

    // =========================================================================
    // ERROR GUESSING: Common Error Scenarios
    // =========================================================================

    @Test
    @DisplayName("EG-01: Admin action with null account number")
    public void testErrorGuessing_NullAccountNumber() {
        // Input: Null account number
        String result = adminController.verifyAccount(null, adminSession, model);
        
        // Expected: Should handle gracefully (no crash)
        assertNotNull(result);
    }

    @Test
    @DisplayName("EG-02: Multiple rapid admin actions on same account")
    public void testErrorGuessing_RapidActions() {
        // Input: Multiple verify attempts in quick succession
        adminController.verifyAccount("TEST_UNV", adminSession, model);
        String result = adminController.verifyAccount("TEST_UNV", adminSession, model);
        
        // Expected: Second attempt should fail (already verified)
        assertTrue(result.contains("redirect:/admin"));
        Account account = accountService.getAccount("TEST_UNV");
        assertEquals(AccountStatus.VERIFIED, account.getStatus());
    }

    @Test
    @DisplayName("EG-03: Admin action after account state change")
    public void testErrorGuessing_StateChangeDuringAction() {
        // Input: Verify account, then immediately try to verify again
        adminController.verifyAccount("TEST_UNV", adminSession, model);
        
        // Account is now verified, try to verify again
        String result = adminController.verifyAccount("TEST_UNV", adminSession, model);
        
        // Expected: Should fail (already verified)
        assertTrue(result.contains("redirect:/admin"));
    }

    // =========================================================================
    // INTEGRATION SCENARIOS: End-to-End User Flows
    // =========================================================================

    @Test
    @DisplayName("INT-01: Complete admin workflow: Login -> View Dashboard -> Verify Account")
    public void testIntegration_CompleteAdminWorkflow() {
        // Step 1: Admin logs in
        Account admin = accountService.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertNotNull(admin);
        assertTrue(admin.isAdmin());

        // Step 2: Admin accesses dashboard
        HttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", admin);
        String dashboardResult = adminController.admin(session, model);
        assertEquals("admin", dashboardResult);

        // Step 3: Admin verifies an account
        String verifyResult = adminController.verifyAccount("TEST_UNV", session, model);
        assertTrue(verifyResult.contains("redirect:/admin"));

        // Step 4: Verify the account state changed
        Account verifiedAccount = accountService.getAccount("TEST_UNV");
        assertEquals(AccountStatus.VERIFIED, verifiedAccount.getStatus());
    }

    @Test
    @DisplayName("INT-02: Admin workflow: Violation -> Admin Action -> Appeal")
    public void testIntegration_ViolationToAppealWorkflow() {
        // Step 1: Apply violation to verified account (should close it)
        adminController.applyViolation("TEST_VER", adminSession, model);
        Account account = accountService.getAccount("TEST_VER");
        assertEquals(AccountStatus.CLOSED, account.getStatus());

        // Step 2: Process appeal (should suspend it)
        adminController.processAppeal("TEST_VER", adminSession, model);
        account = accountService.getAccount("TEST_VER");
        assertEquals(AccountStatus.SUSPENDED, account.getStatus());

        // Step 3: Apply admin action again (should close it)
        adminController.applyAdminAction("TEST_VER", adminSession, model);
        account = accountService.getAccount("TEST_VER");
        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    @DisplayName("INT-03: Regular user cannot access admin functions")
    public void testIntegration_RegularUserBlocked() {
        // Regular user tries to access admin dashboard
        String dashboardResult = adminController.admin(regularUserSession, model);
        assertEquals("redirect:/login", dashboardResult);

        // Regular user tries to verify account
        String verifyResult = adminController.verifyAccount("TEST_UNV", regularUserSession, model);
        assertEquals("redirect:/login", verifyResult);

        // Regular user tries to apply violation
        String violationResult = adminController.applyViolation("TEST_VER", regularUserSession, model);
        assertEquals("redirect:/login", violationResult);
    }
}

