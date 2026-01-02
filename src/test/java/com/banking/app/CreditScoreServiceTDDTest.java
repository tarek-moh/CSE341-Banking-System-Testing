package com.banking.app;

import com.banking.app.model.Account;
import com.banking.app.service.CreditScoreService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Implementation: Credit Score Check Feature
 *
 * BUSINESS REQUIREMENT:
 * The bank needs to assess client creditworthiness before approving loans or
 * increasing transaction limits. Implement a credit score system that:
 * 1. Calculates score based on account history
 * 2. Categorizes clients (Excellent, Good, Fair, Poor)
 * 3. Determines eligibility for premium features
 *
 * TDD APPROACH:
 * - Write tests FIRST
 * - Implement code to make tests pass
 * - Refactor for quality
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreditScoreServiceTDDTest {

    @Autowired
    private CreditScoreService creditScoreService;

    // ==========================================
    // STEP 1: Write Tests FIRST (RED Phase)
    // ==========================================

    @Test
    @Order(1)
    @DisplayName("TDD-01: Calculate credit score for new account")
    public void testCalculateCreditScore_NewAccount() {
        // GIVEN: A new unverified account with no transaction history
        Account account = new Account("NEW123", "Test User", "testuser", "pass",
                0.0, "UNVERIFIED");

        // WHEN: We calculate credit score
        int score = creditScoreService.calculateCreditScore(account);

        // THEN: Score should be initial/default (300)
        assertEquals(300, score, "New accounts should have base credit score of 300");
    }

    @Test
    @Order(2)
    @DisplayName("TDD-02: Credit score increases with account balance")
    public void testCreditScore_IncreasesWithBalance() {
        // GIVEN: Account with high balance
        Account richAccount = new Account("RICH123", "Rich User", "richuser", "pass",
                50000.0, "VERIFIED");

        // WHEN: Calculate score
        int score = creditScoreService.calculateCreditScore(richAccount);

        // THEN: Score should be higher than base
        assertTrue(score > 300, "High balance should increase credit score");
        assertTrue(score <= 850, "Credit score should not exceed maximum (850)");
    }

    @Test
    @Order(3)
    @DisplayName("TDD-03: Verified status improves credit score")
    public void testCreditScore_VerifiedBonus() {
        // GIVEN: Two identical accounts, one verified, one not
        Account unverified = new Account("UN123", "User A", "usera", "pass",
                10000.0, "UNVERIFIED");
        Account verified = new Account("VE123", "User B", "userb", "pass",
                10000.0, "VERIFIED");

        // WHEN: Calculate scores
        int scoreUnverified = creditScoreService.calculateCreditScore(unverified);
        int scoreVerified = creditScoreService.calculateCreditScore(verified);

        // THEN: Verified should have higher score
        assertTrue(scoreVerified > scoreUnverified,
                "Verified accounts should have higher credit score");
    }

    @Test
    @Order(4)
    @DisplayName("TDD-04: Suspended accounts have reduced score")
    public void testCreditScore_SuspendedPenalty() {
        // GIVEN: Suspended account
        Account suspended = new Account("SUS123", "Suspended User", "sususer", "pass",
                10000.0, "SUSPENDED");

        // WHEN: Calculate score
        int score = creditScoreService.calculateCreditScore(suspended);

        // THEN: Score should be significantly reduced
        assertTrue(score < 500, "Suspended accounts should have low credit score");
    }

    @Test
    @Order(5)
    @DisplayName("TDD-05: Closed accounts have no credit score")
    public void testCreditScore_ClosedAccount() {
        // GIVEN: Closed account
        Account closed = new Account("CLS123", "Closed User", "clsuser", "pass",
                1000.0, "CLOSED");

        // WHEN: Calculate score
        int score = creditScoreService.calculateCreditScore(closed);

        // THEN: Score should be minimum or zero
        assertEquals(0, score, "Closed accounts should have zero credit score");
    }

    @Test
    @Order(6)
    @DisplayName("TDD-06: Get credit rating category - Excellent")
    public void testGetCreditRating_Excellent() {
        // GIVEN: High score
        int excellentScore = 800;

        // WHEN: Get rating
        String rating = creditScoreService.getCreditRating(excellentScore);

        // THEN: Should be Excellent
        assertEquals("EXCELLENT", rating, "Score 800 should be EXCELLENT");
    }

    @Test
    @Order(7)
    @DisplayName("TDD-07: Get credit rating category - Good")
    public void testGetCreditRating_Good() {
        // GIVEN: Good score
        int goodScore = 700;

        // WHEN: Get rating
        String rating = creditScoreService.getCreditRating(goodScore);

        // THEN: Should be Good
        assertEquals("GOOD", rating, "Score 700 should be GOOD");
    }

    @Test
    @Order(8)
    @DisplayName("TDD-08: Get credit rating category - Fair")
    public void testGetCreditRating_Fair() {
        // GIVEN: Fair score
        int fairScore = 600;

        // WHEN: Get rating
        String rating = creditScoreService.getCreditRating(fairScore);

        // THEN: Should be Fair
        assertEquals("FAIR", rating, "Score 600 should be FAIR");
    }

    @Test
    @Order(9)
    @DisplayName("TDD-09: Get credit rating category - Poor")
    public void testGetCreditRating_Poor() {
        // GIVEN: Poor score
        int poorScore = 400;

        // WHEN: Get rating
        String rating = creditScoreService.getCreditRating(poorScore);

        // THEN: Should be Poor
        assertEquals("POOR", rating, "Score 400 should be POOR");
    }

    @Test
    @Order(10)
    @DisplayName("TDD-10: Check eligibility for premium features")
    public void testIsEligibleForPremium_HighScore() {
        // GIVEN: Account with excellent credit
        Account account = new Account("PREM123", "Premium User", "premuser", "pass",
                100000.0, "VERIFIED");

        // WHEN: Check premium eligibility
        boolean eligible = creditScoreService.isEligibleForPremium(account);

        // THEN: Should be eligible
        assertTrue(eligible, "High credit score accounts should be eligible for premium");
    }

    @Test
    @Order(11)
    @DisplayName("TDD-11: Check ineligibility for premium features")
    public void testIsEligibleForPremium_LowScore() {
        // GIVEN: Account with poor credit
        Account account = new Account("NONPREM123", "Poor User", "pooruser", "pass",
                100.0, "UNVERIFIED");

        // WHEN: Check premium eligibility
        boolean eligible = creditScoreService.isEligibleForPremium(account);

        // THEN: Should NOT be eligible
        assertFalse(eligible, "Low credit score accounts should NOT be eligible for premium");
    }

    @Test
    @Order(12)
    @DisplayName("TDD-12: Get recommended transaction limit")
    public void testGetRecommendedLimit() {
        // Given
        Account account = new Account("LIM123", "User", "limuser", "pass",
                20000.0, "VERIFIED");

        // When
        BigDecimal limit = creditScoreService.getRecommendedTransactionLimit(account);

        // Then
        assertNotNull(limit, "Limit should not be null");

        // Should be greater than 10,000
        assertTrue(limit.compareTo(new BigDecimal("10000")) > 0,
                "Good credit should allow higher transaction limit. Got: " + limit);

        // Should be at most 100,000
        assertTrue(limit.compareTo(new BigDecimal("100000")) <= 0,
                "Transaction limit should have an upper bound of 100,000. Got: " + limit);
    }

    // ==========================================
    // BOUNDARY VALUE TESTS FOR CREDIT SCORE
    // ==========================================

    @Test
    @Order(13)
    @DisplayName("TDD-13: Boundary - Minimum credit score")
    public void testCreditScore_Minimum() {
        Account account = new Account("MIN123", "Min User", "minuser", "pass",
                0.0, "CLOSED");

        int score = creditScoreService.calculateCreditScore(account);

        assertTrue(score >= 0, "Credit score should never be negative");
    }

    @Test
    @Order(14)
    @DisplayName("TDD-14: Boundary - Maximum credit score")
    public void testCreditScore_Maximum() {
        Account account = new Account("MAX123", "Max User", "maxuser", "pass",
                1000000.0, "VERIFIED");

        int score = creditScoreService.calculateCreditScore(account);

        assertTrue(score <= 850, "Credit score should not exceed 850");
    }

    @Test
    @Order(15)
    @DisplayName("TDD-15: Rating boundary - Excellent threshold (750)")
    public void testRating_ExcellentBoundary() {
        assertEquals("GOOD", creditScoreService.getCreditRating(749));
        assertEquals("EXCELLENT", creditScoreService.getCreditRating(750));
    }

    @Test
    @Order(16)
    @DisplayName("TDD-16: Rating boundary - Good threshold (650)")
    public void testRating_GoodBoundary() {
        assertEquals("FAIR", creditScoreService.getCreditRating(649));
        assertEquals("GOOD", creditScoreService.getCreditRating(650));
    }

    @Test
    @Order(17)
    @DisplayName("TDD-17: Rating boundary - Fair threshold (550)")
    public void testRating_FairBoundary() {
        assertEquals("POOR", creditScoreService.getCreditRating(549));
        assertEquals("FAIR", creditScoreService.getCreditRating(550));
    }

    // ==========================================
    // INTEGRATION TESTS
    // ==========================================

    @Test
    @Order(18)
    @DisplayName("TDD-18: Full credit assessment flow")
    public void testFullCreditAssessment() {
        // GIVEN: Real account from system
        Account account = new Account("123", "John Doe", "johndoe", "123456",
                12450.0, "VERIFIED");

        // WHEN: Perform full assessment
        int score = creditScoreService.calculateCreditScore(account);
        String rating = creditScoreService.getCreditRating(score);
        boolean premium = creditScoreService.isEligibleForPremium(account);
        BigDecimal limit = creditScoreService.getRecommendedTransactionLimit(account);

        // THEN: All values should be consistent
        assertNotNull(rating, "Rating should not be null");
        assertNotNull(limit, "Limit should not be null");

        if (rating.equals("EXCELLENT") || rating.equals("GOOD")) {
            assertTrue(premium, "Good/Excellent ratings should qualify for premium");
        }

        System.out.println("=================================");
        System.out.println("CREDIT ASSESSMENT REPORT");
        System.out.println("=================================");
        System.out.println("Account: " + account.getAccountNumber());
        System.out.println("Balance: $" + account.getBalance());
        System.out.println("Status: " + account.getStatus());
        System.out.println("---------------------------------");
        System.out.println("Credit Score: " + score);
        System.out.println("Credit Rating: " + rating);
        System.out.println("Premium Eligible: " + premium);
        System.out.println("Transaction Limit: $" + limit);
        System.out.println("=================================");
    }
}

// ==========================================
// STEP 2: NOW IMPLEMENT THE SERVICE (GREEN Phase)
// ==========================================
// Create this file: src/main/java/com/banking/app/service/CreditScoreService.java