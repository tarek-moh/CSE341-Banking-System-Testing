package com.banking.app.service;

import com.banking.app.dto.PaymentRequest;
import com.banking.app.dto.PaymentResponse;
import com.banking.app.exception.BankingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CreditCardService {

    private final PaymentGatewayService paymentGatewayService;
    private final AccountService accountService;

    @Autowired
    public CreditCardService(PaymentGatewayService paymentGatewayService, AccountService accountService) {
        this.paymentGatewayService = paymentGatewayService;
        this.accountService = accountService;
    }

    /**
     * Processes a deposit into the bank account using an external credit card.
     */
    public String processCardDeposit(String accountNumber, String cardNumber, String cvv, String expiry,
            BigDecimal amount) {
        PaymentRequest request = new PaymentRequest(cardNumber, cvv, expiry, amount);
        PaymentResponse response = paymentGatewayService.process(request);

        if ("SUCCESS".equals(response.getStatus())) {
            // If card payment is successful, update internal bank balance
            return accountService.processDeposit(accountNumber, amount);
        } else {
            throw new BankingException("Card Deposit Failed: " + response.getErrorMessage());
        }
    }

    /**
     * Processes a withdrawal from the bank account to an external credit card.
     */
    public String processCardWithdraw(String accountNumber, String cardNumber, String cvv, String expiry,
            BigDecimal amount) {
        // Reuse the same mock gateway logic for the withdrawal (payout)
        PaymentRequest request = new PaymentRequest(cardNumber, cvv, expiry, amount);
        PaymentResponse response = paymentGatewayService.process(request);

        if ("SUCCESS".equals(response.getStatus())) {
            // If card transaction is successful, update internal bank balance
            return accountService.processWithdraw(accountNumber, amount);
        } else {
            throw new BankingException("Card Withdrawal Failed: " + response.getErrorMessage());
        }
    }
}
