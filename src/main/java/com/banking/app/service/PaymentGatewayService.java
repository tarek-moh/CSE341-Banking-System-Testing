package com.banking.app.service;

import com.banking.app.dto.PaymentRequest;
import com.banking.app.dto.PaymentResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentGatewayService {

    /**
     * Mocks a payment gateway API call.
     * Uses card number suffixes to trigger different scenarios.
     */
    public PaymentResponse process(PaymentRequest request) {
        String cardNumber = request.getCardNumber();
        String txId = UUID.randomUUID().toString();

        if (cardNumber == null || cardNumber.isEmpty()) {
            return new PaymentResponse("FAILURE", txId, "INVALID_CARD", "Card number is required.");
        }

        // Scenario: Card Expired
        if (cardNumber.endsWith("0000")) {
            return new PaymentResponse("FAILURE", txId, "CARD_EXPIRED", "The provided card has expired.");
        }

        // Scenario: Insufficient Funds
        if (cardNumber.endsWith("1111")) {
            return new PaymentResponse("FAILURE", txId, "INSUFFICIENT_FUNDS", "Declined: Insufficient funds on card.");
        }

        // Scenario: Fraud Detected
        if (cardNumber.endsWith("9999")) {
            return new PaymentResponse("FAILURE", txId, "FRAUD_DETECTED",
                    "Security Alert: High risk transaction blocked.");
        }

        // Scenario: Network Error
        if (cardNumber.endsWith("8888")) {
            return new PaymentResponse("FAILURE", txId, "NETWORK_ERROR", "Could not connect to the banking network.");
        }

        // Scenario: Timeout (Mocking a delay and then failure)
        if (cardNumber.endsWith("7777")) {
            return new PaymentResponse("FAILURE", txId, "GATEWAY_TIMEOUT", "The payment gateway timed out.");
        }

        // Default: Success
        return new PaymentResponse("SUCCESS", txId, null, "Payment processed successfully.");
    }
}
