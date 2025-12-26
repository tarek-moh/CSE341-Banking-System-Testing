package com.banking.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.banking.app.service.AccountService;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/account")
    public String account(Model model) {
        return "account";
    }

    @GetMapping("/account/signup")
    public String createAccount(Model model) {
        return "signup";
    }

    @GetMapping("/account/delete")
    public String deleteAccount(Model model) {
        return "account-delete";
    }

    @GetMapping("/account/withdraw")
    public String handleWithdraw(@RequestParam(required = true) Double amount, Model model) {
        if (amount <= 0) {
            model.addAttribute("error", "Invalid withdrawal amount");
            model.addAttribute("account", AccountService.getAccount("123"));
            return "dashboard";
        }

        String result = accountService.processWithdraw(amount);
        if (result.contains("Failed")) {
            model.addAttribute("error", result);
        } else {
            model.addAttribute("success", result);
        }
        model.addAttribute("account", AccountService.getAccount("123"));
        return "dashboard";
    }

    @GetMapping("/account/deposit")
    public String handleDeposit(@RequestParam(required = true) Double amount, Model model) {
        if (amount <= 0) {
            model.addAttribute("error", "Invalid deposit amount");
            model.addAttribute("account", AccountService.getAccount("123"));
            return "dashboard";
        }

        String result = accountService.processDeposit(amount);
        if (result.contains("Failed")) {
            model.addAttribute("error", result);
        } else {
            model.addAttribute("success", result);
        }
        model.addAttribute("account", AccountService.getAccount("123"));
        return "dashboard"; // Reload the page
    }

    @GetMapping("/account/transfer")
    public String handleTransfer(Model model) {
        return "transfer";
    }

    @GetMapping("/account/verify")
    public String handleVerify(Model model) {
        return "verify";
    }

    @GetMapping("/account/suspend")
    public String handleSuspend(Model model) {
        return "suspend";
    }

    @GetMapping("/account/close")
    public String handleClose(Model model) {
        return "close";
    }
}
