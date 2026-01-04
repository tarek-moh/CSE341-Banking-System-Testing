package com.banking.app;

import com.banking.app.controller.AdminController;
import com.banking.app.exception.AccountStatusException;
import com.banking.app.model.Account;
import com.banking.app.model.AccountStatus;
import com.banking.app.policy.TransactionPolicy;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * White-Box Testing for Admin Functionality
 * 
 * Testing with knowledge of internal implementation:
 * - Direct method testing
 * - Code path coverage
 * - Internal state verification
 * - Branch coverage
 * - Condition coverage
 */
@SpringBootTest
@DisplayName("Admin White-Box Tests")
public class AdminWhiteBoxTest {
	@Autowired
    private AccountService accountService;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private AdminController adminController;

	private HttpSession adminSession;
	private HttpSession regularUserSession;
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

		// Mock model
		model = mock(Model.class);

		// Create test accounts with different states
		createTestAccount("TEST_UNV", "Unverified Test", "test_unv", "pass", 1000.00, "UNVERIFIED");
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
	// WHITE-BOX: Account Model - verifyAccount() Method
	// =========================================================================

	@Test
	@DisplayName("TB-01: verifyAccount() - Path 1: Successful verification -> Success")
	public void testVerifyAccount_Path1_Success() {
		String result = adminController.verifyAccount("TEST_UNV", adminSession, model);

		// Expected: Success and redirect
		assertTrue(result.contains("redirect:/admin"));

		// Verify account is now verified
		Account account = accountService.getAccount("TEST_UNV");
		assertEquals(AccountStatus.VERIFIED, account.getStatus());
	}

	@Test
	@DisplayName("TB-02: verifyAccount() - Path 2: Session is null -> Failure")
	public void testVerifyAccount_Path2_Null_Session() {
		String result = adminController.verifyAccount("TEST_UNV", null, model);

		// Expected: redirect to default path
		assertTrue(result.contains("redirect:/login"));

		// Verify account is not verified
		Account account = accountService.getAccount("TEST_UNV");
		assertEquals(AccountStatus.UNVERIFIED, account.getStatus());
	}

	@Test
	@DisplayName("TB-03: verifyAccount() - Path 3: Admin not logged in -> Failure")
	public void testVerifyAccount_Path3_Not_Admin() {
		String result = adminController.verifyAccount("TEST_UNV", regularUserSession, model);

		// Expected: redirect to default path
		assertTrue(result.contains("redirect:/login"));

		// Verify account is not verified
		Account account = accountService.getAccount("TEST_UNV");
		assertEquals(AccountStatus.UNVERIFIED, account.getStatus());
	}
}

