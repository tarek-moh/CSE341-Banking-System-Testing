package com.banking.app.service;

import com.banking.app.model.Account;
import com.banking.app.model.AccountStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CreditScoreService {

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 850;
    private static final int BASE_SCORE = 300;

    private static final int EXCELLENT_THRESHOLD = 750;
    private static final int GOOD_THRESHOLD = 650;
    private static final int FAIR_THRESHOLD = 550;

    private static final int PREMIUM_THRESHOLD = 650;  // ← CHANGED from 700

    private static final BigDecimal BALANCE_MULTIPLIER = new BigDecimal("0.01");
    private static final int MAX_BALANCE_SCORE = 400;

    public int calculateCreditScore(Account account) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            return 0;
        }

        int score = BASE_SCORE;
        score += calculateBalanceScore(account.getBalance());
        score += calculateStatusScore(account.getStatus());

        if (account.getStatus() == AccountStatus.VERIFIED) {
            score += 150;
        }

        score = Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
        return score;
    }

    private int calculateBalanceScore(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        int balanceScore = balance.multiply(BALANCE_MULTIPLIER).intValue();
        return Math.min(balanceScore, MAX_BALANCE_SCORE);
    }

    private int calculateStatusScore(AccountStatus status) {
        switch (status) {
            case VERIFIED:
                return 100;
            case UNVERIFIED:
                return 0;
            case SUSPENDED:
                return -200;
            case CLOSED:
                return -300;
            default:
                return 0;
        }
    }

    public String getCreditRating(int score) {
        if (score >= EXCELLENT_THRESHOLD) {
            return "EXCELLENT";
        } else if (score >= GOOD_THRESHOLD) {
            return "GOOD";
        } else if (score >= FAIR_THRESHOLD) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }

    public boolean isEligibleForPremium(Account account) {
        int score = calculateCreditScore(account);
        return score >= PREMIUM_THRESHOLD &&
                account.getStatus() == AccountStatus.VERIFIED;
    }

    public BigDecimal getRecommendedTransactionLimit(Account account) {
        int score = calculateCreditScore(account);
        String rating = getCreditRating(score);

        BigDecimal baseLimit;
        switch (rating) {
            case "EXCELLENT":
                baseLimit = new BigDecimal("50000");
                break;
            case "GOOD":
                baseLimit = new BigDecimal("25000");
                break;
            case "FAIR":
                baseLimit = new BigDecimal("10000");
                break;
            case "POOR":
                baseLimit = new BigDecimal("5000");
                break;
            default:
                baseLimit = new BigDecimal("5000");
        }

        BigDecimal balanceMultiplier = account.getBalance()
                .divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP)
                .add(BigDecimal.ONE);

        balanceMultiplier = balanceMultiplier.min(new BigDecimal("3"));

        BigDecimal finalLimit = baseLimit.multiply(balanceMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal maxLimit = new BigDecimal("99999");  // ← CHANGED from 100000
        return finalLimit.min(maxLimit);
    }

    public String generateCreditReport(Account account) {
        int score = calculateCreditScore(account);
        String rating = getCreditRating(score);
        boolean premium = isEligibleForPremium(account);
        BigDecimal limit = getRecommendedTransactionLimit(account);

        StringBuilder report = new StringBuilder();
        report.append("=== CREDIT ASSESSMENT REPORT ===\n");
        report.append("Account Number: ").append(account.getAccountNumber()).append("\n");
        report.append("Client Name: ").append(account.getClientName()).append("\n");
        report.append("Account Status: ").append(account.getStatus()).append("\n");
        report.append("Current Balance: $").append(account.getBalance()).append("\n");
        report.append("--------------------------------\n");
        report.append("Credit Score: ").append(score).append(" / 850\n");
        report.append("Credit Rating: ").append(rating).append("\n");
        report.append("Premium Eligible: ").append(premium ? "Yes" : "No").append("\n");
        report.append("Recommended Limit: $").append(limit).append("\n");
        report.append("================================\n");

        return report.toString();
    }
}