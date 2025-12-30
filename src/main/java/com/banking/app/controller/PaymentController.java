package com.banking.app.controller;

import com.banking.app.service.AccountService;
import com.banking.app.service.CreditCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class PaymentController {

    private final CreditCardService creditCardService;
    private final AccountService accountService;

    @Autowired
    public PaymentController(CreditCardService creditCardService, AccountService accountService) {
        this.creditCardService = creditCardService;
        this.accountService = accountService;
    }

    @PostMapping("/payment/deposit")
    public String depositWithCard(@RequestParam String cardNumber,
            @RequestParam String cvv,
            @RequestParam String expiry,
            @RequestParam BigDecimal amount,
            Model model) {
        try {
            // Using a hardcoded account ID for now as per the current dashboard logic
            String result = creditCardService.processCardDeposit("123", cardNumber, cvv, expiry, amount);
            model.addAttribute("success", result);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("account", accountService.getAccount("123"));
        return "dashboard";
    }

    @PostMapping("/payment/withdraw")
    public String withdrawToCard(@RequestParam String cardNumber,
            @RequestParam String cvv,
            @RequestParam String expiry,
            @RequestParam BigDecimal amount,
            Model model) {
        try {
            String result = creditCardService.processCardWithdraw("123", cardNumber, cvv, expiry, amount);
            model.addAttribute("success", result);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("account", accountService.getAccount("123"));
        return "dashboard";
    }
}
