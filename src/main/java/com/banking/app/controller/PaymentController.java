package com.banking.app.controller;

import com.banking.app.model.Account;
import com.banking.app.service.AccountService;
import com.banking.app.service.CreditCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

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
            Model model, HttpSession session) {

        Account sessionUser = (Account) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        String accountNum = sessionUser.getAccountNumber();
        try {
            // Using a hardcoded account ID for now as per the current dashboard logic
            String result = creditCardService.processCardDeposit(accountNum, cardNumber, cvv, expiry, amount);
            if (result.toLowerCase().contains("failed")) {
                model.addAttribute("error", result);
            } else {
                model.addAttribute("success", result);
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("account", accountService.getAccount(accountNum));
        return "dashboard";
    }

    @PostMapping("/payment/withdraw")
    public String withdrawToCard(@RequestParam String cardNumber,
            @RequestParam String cvv,
            @RequestParam String expiry,
            @RequestParam BigDecimal amount,
            Model model, HttpSession session) {

        Account sessionUser = (Account) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        String accountNum = sessionUser.getAccountNumber();

        try {
            String result = creditCardService.processCardWithdraw(accountNum, cardNumber, cvv, expiry, amount);
            model.addAttribute("success", result);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("account", accountService.getAccount(accountNum));
        return "dashboard";
    }
}
