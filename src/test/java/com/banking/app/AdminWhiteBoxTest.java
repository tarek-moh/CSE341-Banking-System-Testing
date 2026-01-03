package com.banking.app;

import com.banking.app.exception.AccountStatusException;
import com.banking.app.model.Account;
import com.banking.app.model.AccountStatus;
import com.banking.app.policy.TransactionPolicy;
import com.banking.app.repository.AccountRepository;
import com.banking.app.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
@DisplayName("Admin White-Box Tests")
public class AdminWhiteBoxTest {

    private StubAccountRepository accountRepository;
    private StubTransactionPolicy transactionPolicy;
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        accountRepository = new StubAccountRepository();
        transactionPolicy = new StubTransactionPolicy();
        accountService = new AccountService(accountRepository, transactionPolicy);
    }

    // =========================================================================
    // WHITE-BOX: Account Model - verify() Method
    // =========================================================================

    @Test
    @DisplayName("WB-01: verify() - Path 1: Status is UNVERIFIED -> Success")
    public void testVerify_Path1_Unverified_Success() {
        // Setup: Account in UNVERIFIED state
        Account account = new Account("ACC001", "Test", "test", "pass", 1000.0, "UNVERIFIED");
        accountRepository.save(account);

        // Execute: Call verify
        account.verify();

        // Verify: Status changed to VERIFIED
        assertEquals(AccountStatus.VERIFIED, account.getStatus());
    }

    @Test
    @DisplayName("WB-02: verify() - Path 2: Status is NOT UNVERIFIED -> Exception")
    public void testVerify_Path2_NotUnverified_Exception() {
        // Setup: Account in VERIFIED state
        Account account = new Account("ACC002", "Test", "test", "pass", 1000.0, "VERIFIED");
        accountRepository.save(account);

        // Execute & Verify: Should throw exception
        AccountStatusException exception = assertThrows(AccountStatusException.class, () -> {
            account.verify();
        });

        assertTrue(exception.getMessage().contains("already"));
        assertEquals(AccountStatus.VERIFIED, account.getStatus()); // Status unchanged
    }

    // =========================================================================
    // WHITE-BOX: Account Model - violation() Method
    // =========================================================================

    @Test
    @DisplayName("WB-03: violation() - Path 1: Status is UNVERIFIED -> SUSPENDED")
    public void testViolation_Path1_Unverified_ToSuspended() {
        // Setup: Account in UNVERIFIED state
        Account account = new Account("ACC003", "Test", "test", "pass", 1000.0, "UNVERIFIED");
        accountRepository.save(account);

        // Execute: Call violation
        account.violation();

        // Verify: Status changed to SUSPENDED
        assertEquals(AccountStatus.SUSPENDED, account.getStatus());
    }

    @Test
    @DisplayName("WB-04: violation() - Path 2: Status is VERIFIED -> CLOSED")
    public void testViolation_Path2_Verified_ToClosed() {
        // Setup: Account in VERIFIED state
        Account account = new Account("ACC004", "Test", "test", "pass", 1000.0, "VERIFIED");
        accountRepository.save(account);

        // Execute: Call violation
        account.violation();

        // Verify: Status changed to CLOSED
        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    @DisplayName("WB-05: violation() - Path 3: Status is SUSPENDED -> Exception")
    public void testViolation_Path3_Suspended_Exception() {
        // Setup: Account in SUSPENDED state
        Account account = new Account("ACC005", "Test", "test", "pass", 1000.0, "SUSPENDED");
        accountRepository.save(account);

        // Execute & Verify: Should throw exception
        AccountStatusException exception = assertThrows(AccountStatusException.class, () -> {
            account.violation();
        });

        assertTrue(exception.getMessage().contains("violation"));
        assertEquals(AccountStatus.SUSPENDED, account.getStatus()); // Status unchanged
    }

    @Test
    @DisplayName("WB-06: violation() - Path 4: Status is CLOSED -> Exception")
    public void testViolation_Path4_Closed_Exception() {
        // Setup: Account in CLOSED state
        Account account = new Account("ACC006", "Test", "test", "pass", 1000.0, "CLOSED");
        accountRepository.save(account);

        // Execute & Verify: Should throw exception
        AccountStatusException exception = assertThrows(AccountStatusException.class, () -> {
            account.violation();
        });

        assertTrue(exception.getMessage().contains("violation"));
        assertEquals(AccountStatus.CLOSED, account.getStatus()); // Status unchanged
    }

    // =========================================================================
    // WHITE-BOX: Account Model - adminAction() Method
    // =========================================================================

    @Test
    @DisplayName("WB-07: adminAction() - Path 1: Status is SUSPENDED -> CLOSED")
    public void testAdminAction_Path1_Suspended_ToClosed() {
        // Setup: Account in SUSPENDED state
        Account account = new Account("ACC007", "Test", "test", "pass", 1000.0, "SUSPENDED");
        accountRepository.save(account);

        // Execute: Call adminAction
        account.adminAction();

        // Verify: Status changed to CLOSED
        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    @DisplayName("WB-08: adminAction() - Path 2: Status is VERIFIED -> CLOSED")
    public void testAdminAction_Path2_Verified_ToClosed() {
        // Setup: Account in VERIFIED state
        Account account = new Account("ACC008", "Test", "test", "pass", 1000.0, "VERIFIED");
        accountRepository.save(account);

        // Execute: Call adminAction
        account.adminAction();

        // Verify: Status changed to CLOSED
        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    @DisplayName("WB-09: adminAction() - Path 3: Status is UNVERIFIED -> Exception")
    public void testAdminAction_Path3_Unverified_Exception() {
        // Setup: Account in UNVERIFIED state
        Account account = new Account("ACC009", "Test", "test", "pass", 1000.0, "UNVERIFIED");
        accountRepository.save(account);

        // Execute & Verify: Should throw exception
        AccountStatusException exception = assertThrows(AccountStatusException.class, () -> {
            account.adminAction();
        });

        assertTrue(exception.getMessage().contains("admin action"));
        assertEquals(AccountStatus.UNVERIFIED, account.getStatus()); // Status unchanged
    }

    @Test
    @DisplayName("WB-10: adminAction() - Path 4: Status is CLOSED -> Exception")
    public void testAdminAction_Path4_Closed_Exception() {
        // Setup: Account in CLOSED state
        Account account = new Account("ACC010", "Test", "test", "pass", 1000.0, "CLOSED");
        accountRepository.save(account);

        // Execute & Verify: Should throw exception
        AccountStatusException exception = assertThrows(AccountStatusException.class, () -> {
            account.adminAction();
        });

        assertTrue(exception.getMessage().contains("admin action"));
        assertEquals(AccountStatus.CLOSED, account.getStatus()); // Status unchanged
    }

    // =========================================================================
    // WHITE-BOX: Account Model - appeal() Method
    // =========================================================================

    @Test
    @DisplayName("WB-11: appeal() - Path 1: Status is CLOSED -> SUSPENDED")
    public void testAppeal_Path1_Closed_ToSuspended() {
        // Setup: Account in CLOSED state
        Account account = new Account("ACC011", "Test", "test", "pass", 1000.0, "CLOSED");
        accountRepository.save(account);

        // Execute: Call appeal
        account.appeal();

        // Verify: Status changed to SUSPENDED
        assertEquals(AccountStatus.SUSPENDED, account.getStatus());
    }

    @Test
    @DisplayName("WB-12: appeal() - Path 2: Status is NOT CLOSED -> Exception")
    public void testAppeal_Path2_NotClosed_Exception() {
        // Test all non-CLOSED states
        AccountStatus[] invalidStates = {
            AccountStatus.UNVERIFIED,
            AccountStatus.VERIFIED,
            AccountStatus.SUSPENDED
        };

        for (AccountStatus state : invalidStates) {
            Account account = new Account("ACC_" + state.name(), "Test", "test", "pass", 1000.0, state.name());
            accountRepository.save(account);

            // Execute & Verify: Should throw exception
            AccountStatusException exception = assertThrows(AccountStatusException.class, () -> {
                account.appeal();
            });

            assertTrue(exception.getMessage().contains("Appeal") || exception.getMessage().contains("closed"));
            assertEquals(state, account.getStatus()); // Status unchanged
        }
    }

    // =========================================================================
    // WHITE-BOX: AccountService - processVerify() Method
    // =========================================================================

    @Test
    @DisplayName("WB-13: processVerify() - Path 1: Account not found")
    public void testProcessVerify_Path1_AccountNotFound() {
        // Setup: Account does not exist
        // Execute
        String result = accountService.processVerify("NON_EXISTENT");

        // Verify: Error message
        assertTrue(result.contains("Failed"));
        assertTrue(result.contains("not found"));
    }

    @Test
    @DisplayName("WB-14: processVerify() - Path 2: Account found, verify succeeds")
    public void testProcessVerify_Path2_Success() {
        // Setup: Account in UNVERIFIED state
        Account account = new Account("ACC014", "Test", "test", "pass", 1000.0, "UNVERIFIED");
        accountRepository.save(account);

        // Execute
        String result = accountService.processVerify("ACC014");

        // Verify: Success message and status changed
        assertEquals("Verification successful", result);
        Account savedAccount = accountRepository.findByAccountNumber("ACC014").orElse(null);
        assertNotNull(savedAccount);
        assertEquals(AccountStatus.VERIFIED, savedAccount.getStatus());
    }

    @Test
    @DisplayName("WB-15: processVerify() - Path 3: Account found, verify fails (exception)")
    public void testProcessVerify_Path3_Exception() {
        // Setup: Account in VERIFIED state (cannot verify again)
        Account account = new Account("ACC015", "Test", "test", "pass", 1000.0, "VERIFIED");
        accountRepository.save(account);

        // Execute
        String result = accountService.processVerify("ACC015");

        // Verify: Error message
        assertTrue(result.contains("Failed"));
    }

    // =========================================================================
    // WHITE-BOX: AccountService - processViolation() Method
    // =========================================================================

    @Test
    @DisplayName("WB-16: processViolation() - Path 1: Account not found")
    public void testProcessViolation_Path1_AccountNotFound() {
        // Execute
        String result = accountService.processViolation("NON_EXISTENT");

        // Verify: Error message
        assertTrue(result.contains("Failed"));
        assertTrue(result.contains("not found"));
    }

    @Test
    @DisplayName("WB-17: processViolation() - Path 2: Unverified account -> Suspended")
    public void testProcessViolation_Path2_Unverified_Success() {
        // Setup: Account in UNVERIFIED state
        Account account = new Account("ACC017", "Test", "test", "pass", 1000.0, "UNVERIFIED");
        accountRepository.save(account);

        // Execute
        String result = accountService.processViolation("ACC017");

        // Verify: Success and status changed to SUSPENDED
        assertEquals("Violation processed successfully", result);
        Account savedAccount = accountRepository.findByAccountNumber("ACC017").orElse(null);
        assertEquals(AccountStatus.SUSPENDED, savedAccount.getStatus());
    }

    @Test
    @DisplayName("WB-18: processViolation() - Path 3: Verified account -> Closed")
    public void testProcessViolation_Path3_Verified_Success() {
        // Setup: Account in VERIFIED state
        Account account = new Account("ACC018", "Test", "test", "pass", 1000.0, "VERIFIED");
        accountRepository.save(account);

        // Execute
        String result = accountService.processViolation("ACC018");

        // Verify: Success and status changed to CLOSED
        assertEquals("Violation processed successfully", result);
        Account savedAccount = accountRepository.findByAccountNumber("ACC018").orElse(null);
        assertEquals(AccountStatus.CLOSED, savedAccount.getStatus());
    }

    @Test
    @DisplayName("WB-19: processViolation() - Path 4: Invalid state -> Exception")
    public void testProcessViolation_Path4_InvalidState() {
        // Setup: Account in SUSPENDED state
        Account account = new Account("ACC019", "Test", "test", "pass", 1000.0, "SUSPENDED");
        accountRepository.save(account);

        // Execute
        String result = accountService.processViolation("ACC019");

        // Verify: Error message
        assertTrue(result.contains("Failed"));
    }

    // =========================================================================
    // WHITE-BOX: AccountService - processAdminAction() Method
    // =========================================================================

    @Test
    @DisplayName("WB-20: processAdminAction() - Path 1: Account not found")
    public void testProcessAdminAction_Path1_AccountNotFound() {
        // Execute
        String result = accountService.processAdminAction("NON_EXISTENT");

        // Verify: Error message
        assertTrue(result.contains("Failed"));
        assertTrue(result.contains("not found"));
    }

    @Test
    @DisplayName("WB-21: processAdminAction() - Path 2: Suspended account -> Closed")
    public void testProcessAdminAction_Path2_Suspended_Success() {
        // Setup: Account in SUSPENDED state
        Account account = new Account("ACC021", "Test", "test", "pass", 1000.0, "SUSPENDED");
        accountRepository.save(account);

        // Execute
        String result = accountService.processAdminAction("ACC021");

        // Verify: Success and status changed to CLOSED
        assertEquals("Admin action processed successfully", result);
        Account savedAccount = accountRepository.findByAccountNumber("ACC021").orElse(null);
        assertEquals(AccountStatus.CLOSED, savedAccount.getStatus());
    }

    @Test
    @DisplayName("WB-22: processAdminAction() - Path 3: Verified account -> Closed")
    public void testProcessAdminAction_Path3_Verified_Success() {
        // Setup: Account in VERIFIED state
        Account account = new Account("ACC022", "Test", "test", "pass", 1000.0, "VERIFIED");
        accountRepository.save(account);

        // Execute
        String result = accountService.processAdminAction("ACC022");

        // Verify: Success and status changed to CLOSED
        assertEquals("Admin action processed successfully", result);
        Account savedAccount = accountRepository.findByAccountNumber("ACC022").orElse(null);
        assertEquals(AccountStatus.CLOSED, savedAccount.getStatus());
    }

    @Test
    @DisplayName("WB-23: processAdminAction() - Path 4: Invalid state -> Exception")
    public void testProcessAdminAction_Path4_InvalidState() {
        // Setup: Account in UNVERIFIED state
        Account account = new Account("ACC023", "Test", "test", "pass", 1000.0, "UNVERIFIED");
        accountRepository.save(account);

        // Execute
        String result = accountService.processAdminAction("ACC023");

        // Verify: Error message
        assertTrue(result.contains("Failed"));
    }

    // =========================================================================
    // WHITE-BOX: AccountService - processAppeal() Method
    // =========================================================================

    @Test
    @DisplayName("WB-24: processAppeal() - Path 1: Account not found")
    public void testProcessAppeal_Path1_AccountNotFound() {
        // Execute
        String result = accountService.processAppeal("NON_EXISTENT");

        // Verify: Error message
        assertTrue(result.contains("Failed"));
        assertTrue(result.contains("not found"));
    }

    @Test
    @DisplayName("WB-25: processAppeal() - Path 2: Closed account -> Suspended")
    public void testProcessAppeal_Path2_Closed_Success() {
        // Setup: Account in CLOSED state
        Account account = new Account("ACC025", "Test", "test", "pass", 1000.0, "CLOSED");
        accountRepository.save(account);

        // Execute
        String result = accountService.processAppeal("ACC025");

        // Verify: Success and status changed to SUSPENDED
        assertEquals("Appeal processed successfully", result);
        Account savedAccount = accountRepository.findByAccountNumber("ACC025").orElse(null);
        assertEquals(AccountStatus.SUSPENDED, savedAccount.getStatus());
    }

    @Test
    @DisplayName("WB-26: processAppeal() - Path 3: Invalid state -> Exception")
    public void testProcessAppeal_Path3_InvalidState() {
        // Setup: Account in VERIFIED state
        Account account = new Account("ACC026", "Test", "test", "pass", 1000.0, "VERIFIED");
        accountRepository.save(account);

        // Execute
        String result = accountService.processAppeal("ACC026");

        // Verify: Error message
        assertTrue(result.contains("Failed"));
    }

    // =========================================================================
    // WHITE-BOX: Branch Coverage - All Condition Combinations
    // =========================================================================

    @Test
    @DisplayName("WB-27: Branch Coverage - violation() all branches")
    public void testBranchCoverage_Violation_AllBranches() {
        // Branch 1: UNVERIFIED -> SUSPENDED
        Account acc1 = new Account("BR1", "Test", "test", "pass", 1000.0, "UNVERIFIED");
        acc1.violation();
        assertEquals(AccountStatus.SUSPENDED, acc1.getStatus());

        // Branch 2: VERIFIED -> CLOSED
        Account acc2 = new Account("BR2", "Test", "test", "pass", 1000.0, "VERIFIED");
        acc2.violation();
        assertEquals(AccountStatus.CLOSED, acc2.getStatus());

        // Branch 3: SUSPENDED -> Exception
        Account acc3 = new Account("BR3", "Test", "test", "pass", 1000.0, "SUSPENDED");
        assertThrows(AccountStatusException.class, () -> acc3.violation());

        // Branch 4: CLOSED -> Exception
        Account acc4 = new Account("BR4", "Test", "test", "pass", 1000.0, "CLOSED");
        assertThrows(AccountStatusException.class, () -> acc4.violation());
    }

    @Test
    @DisplayName("WB-28: Branch Coverage - adminAction() all branches")
    public void testBranchCoverage_AdminAction_AllBranches() {
        // Branch 1: SUSPENDED -> CLOSED
        Account acc1 = new Account("BR5", "Test", "test", "pass", 1000.0, "SUSPENDED");
        acc1.adminAction();
        assertEquals(AccountStatus.CLOSED, acc1.getStatus());

        // Branch 2: VERIFIED -> CLOSED
        Account acc2 = new Account("BR6", "Test", "test", "pass", 1000.0, "VERIFIED");
        acc2.adminAction();
        assertEquals(AccountStatus.CLOSED, acc2.getStatus());

        // Branch 3: UNVERIFIED -> Exception
        Account acc3 = new Account("BR7", "Test", "test", "pass", 1000.0, "UNVERIFIED");
        assertThrows(AccountStatusException.class, () -> acc3.adminAction());

        // Branch 4: CLOSED -> Exception
        Account acc4 = new Account("BR8", "Test", "test", "pass", 1000.0, "CLOSED");
        assertThrows(AccountStatusException.class, () -> acc4.adminAction());
    }

    // =========================================================================
    // WHITE-BOX: Internal State Verification
    // =========================================================================

    @Test
    @DisplayName("WB-29: Internal State - Account balance preserved during state transitions")
    public void testInternalState_BalancePreserved() {
        // Setup: Account with balance
        BigDecimal initialBalance = new BigDecimal("5000.00");
        Account account = new Account("ACC029", "Test", "test", "pass", 5000.0, "UNVERIFIED");
        accountRepository.save(account);

        // Execute: Multiple state transitions
        account.verify(); // UNVERIFIED -> VERIFIED
        assertEquals(initialBalance, account.getBalance());

        account.violation(); // VERIFIED -> CLOSED
        assertEquals(initialBalance, account.getBalance());

        account.appeal(); // CLOSED -> SUSPENDED (should fail, but test balance preservation)
        // Actually appeal requires CLOSED state, so let's test properly
        account = new Account("ACC029B", "Test", "test", "pass", 5000.0, "CLOSED");
        account.appeal();
        assertEquals(initialBalance, account.getBalance());
    }

    @Test
    @DisplayName("WB-30: Internal State - Account details preserved during state transitions")
    public void testInternalState_DetailsPreserved() {
        // Setup: Account with details
        String accountNumber = "ACC030";
        String clientName = "John Doe";
        String username = "johndoe";
        Account account = new Account(accountNumber, clientName, username, "pass", 1000.0, "UNVERIFIED");
        accountRepository.save(account);

        // Execute: State transitions
        account.verify();
        account.violation();

        // Verify: Details unchanged
        assertEquals(accountNumber, account.getAccountNumber());
        assertEquals(clientName, account.getClientName());
        assertEquals(username, account.getUsername());
    }

    // =========================================================================
    // Stub Classes for White-Box Testing
    // =========================================================================

    class StubAccountRepository extends AccountRepository {
        private Map<String, Account> db = new HashMap<>();

        public StubAccountRepository() {
            super("admin", "admin123", "000", "Admin User");
        }

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
                throw new com.banking.app.exception.BankingException("Insufficient funds");
            }
        }

        @Override
        public void validateDeposit(Account account, BigDecimal amount) {
            if (shouldThrowDepositError) {
                throw new com.banking.app.exception.InvalidAmountException("Invalid amount");
            }
        }

        @Override
        public void validateTransfer(Account source, Account destination, BigDecimal amount) {
            if (shouldThrowTransferError) {
                throw new com.banking.app.exception.BankingException("Transfer failed: limit exceeded");
            }
        }
    }
}

