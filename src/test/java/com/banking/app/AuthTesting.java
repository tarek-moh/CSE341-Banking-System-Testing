package com.banking.app;

import com.banking.app.controller.AuthController;
import com.banking.app.dto.UserRegistrationDto;
import com.banking.app.model.Account;
import com.banking.app.service.AccountService;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

//import java.util.Set;

@SpringBootTest
public class AuthTesting {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthController authController;

    // Mock objects for Controller testing
    private Model model;
    private BindingResult bindingResult;

    // VALIDATOR FOR BOUNDARY TESTS
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @BeforeEach
    public void setup() {
        model = mock(Model.class);
        bindingResult = mock(BindingResult.class);
    }

    // ==========================================
    // 2.1 Black-Box Tests (Sign Up & Login)
    // ==========================================

    @Test
    public void testBlackBox_ValidRegistration() {
        // Partition: Valid Inputs
        String uniqueUser = "test_user_" + System.currentTimeMillis();

        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("Test User");
        dto.setUsername(uniqueUser);
        dto.setPassword("Pass123");
        dto.setConfirmPassword("Pass123");

        // Simulate no validation errors
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = authController.handleSignup(dto, bindingResult, model);

        // Expected: Redirect to login
        assertEquals("redirect:/login?success", viewName);
    }

    @Test
    public void testBlackBox_LoginFlow() {
        // Create user first
        String user = "login_test_" + System.currentTimeMillis();
        accountService.registerUser("Login Test", user, "Pass123");

        // Test Login
        Account loggedIn = accountService.login(user, "Pass123");

        assertNotNull(loggedIn);
        assertEquals(user, loggedIn.getUsername());
    }

    @Test
    public void testBlackBox_InvalidPasswordBoundary() {
        // Boundary Value: 5 chars (Fail)
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("boundary_user");
        dto.setPassword("12345"); // Too short

        // In a real integration test, the validator would catch this.
        // Here we simulate the validator reporting an error.
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = authController.handleSignup(dto, bindingResult, model);

        // Expected: Return to signup page
        assertEquals("signup", viewName);
    }

    @Test
    public void testLogin_Failure_WrongPassword() {
        // 1. Register a user
        String user = "login_fail_" + System.currentTimeMillis();
        accountService.registerUser("Test", user, "Pass123");

        // 2. Try to login with wrong password
        Exception exception = assertThrows(Exception.class, () -> {
            accountService.login(user, "WrongPass");
        });

        // 3. Verify exact error message from the Report Table
        assertTrue(exception.getMessage().contains("Invalid password"),
                "Expected 'Invalid password' but got: " + exception.getMessage());
    }

    @Test
    public void testLogin_Failure_UserNotFound() {
        // Try to login with non-existent user
        Exception exception = assertThrows(Exception.class, () -> {
            accountService.login("ghost_user", "Pass123");
        });

        // Verify exact error message
        // assertTrue(exception.getMessage().contains("Login failed:"));
        assertTrue(exception.getMessage().contains("User not found"),
                "Expected 'User not found' but got: " + exception.getMessage());
    }

    @Test
    public void testBoundary_Username_Lower() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("Test User");
        dto.setPassword("Pass123");
        dto.setConfirmPassword("Pass123");

        // Case 1: 3 Characters (Should Fail - BV-User-01)
        dto.setUsername("abc");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Username 'abc' should fail validation");
        assertEquals("Username must be between 4 and 20 characters", violations.iterator().next().getMessage());

        // Case 2: 4 Characters (Should Pass - BV-User-02)
        dto.setUsername("abcd");
        violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Username 'abcd' should pass validation");
    }

    @Test
    public void testBoundary_Username_Upper() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("Test User");
        dto.setPassword("Pass123");
        dto.setConfirmPassword("Pass123");

        // Case 1: 20 Characters (Should Pass - BV-User-03)
        // Creating a string of 20 'a's
        String user20 = new String(new char[20]).replace('\0', 'a');
        dto.setUsername(user20);
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Username of 20 chars should pass validation");

        // Case 2: 21 Characters (Should Fail - BV-User-04)
        String user21 = new String(new char[21]).replace('\0', 'a');
        dto.setUsername(user21);
        violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Username of 21 chars should fail");
        assertEquals("Username must be between 4 and 20 characters", violations.iterator().next().getMessage());
    }

    // ==========================================
    // 3.4 White-Box Tests (Logic Paths)
    // ==========================================
    @SuppressWarnings("null")
    @Test
    public void testWhiteBox_Path_PasswordMismatch() {
        // Path: !password.equals(confirmPassword)
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("mismatch_user");
        dto.setPassword("Pass123");
        dto.setConfirmPassword("Pass999"); // Mismatch

        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = authController.handleSignup(dto, bindingResult, model);

        // Expected: Return to signup, Add error to model
        assertEquals("signup", viewName);
        verify(model).addAttribute(eq("error"), contains("match"));
    }

    @SuppressWarnings("null")
    @Test
    public void testWhiteBox_Path_DuplicateUser() {
        // Path: Catch Exception (Duplicate)
        String existingUser = "existing_user";
        accountService.registerUser("Existing", existingUser, "Pass123");

        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername(existingUser); // Duplicate
        dto.setPassword("Pass123");
        dto.setConfirmPassword("Pass123");

        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = authController.handleSignup(dto, bindingResult, model);

        // Expected: Return to signup (caught exception)
        assertEquals("signup", viewName);
        verify(model).addAttribute(eq("error"), contains("Signup Failed"));
    }
}
