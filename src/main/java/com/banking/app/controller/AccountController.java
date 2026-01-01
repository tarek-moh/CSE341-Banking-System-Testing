package com.banking.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.banking.app.model.Account;
import com.banking.app.service.AccountService;

import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;

@Controller
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/account")
    public String account(Model model, HttpSession session) {
        Account sessionUser = (Account) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("account", accountService.getAccount(sessionUser.getAccountNumber()));
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

    @PostMapping("/account/withdraw")
    public String handleWithdraw(@RequestParam(required = true) BigDecimal amount, Model model, HttpSession session) {
        Account sessionUser = (Account) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        String accountNum = sessionUser.getAccountNumber();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("error", "Invalid withdrawal amount");
            model.addAttribute("account", accountService.getAccount(accountNum));
            return "dashboard";
        }

        String result = accountService.processWithdraw(accountNum, amount);
        if (result.contains("Failed")) {
            model.addAttribute("error", result);
        } else {
            model.addAttribute("success", result);
        }
        model.addAttribute("account", accountService.getAccount(accountNum));
        return "dashboard";
    }

    @PostMapping("/account/deposit")
    public String handleDeposit(@RequestParam(required = true) BigDecimal amount, Model model, HttpSession session) {
        Account sessionUser = (Account) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        String accountNum = sessionUser.getAccountNumber();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("error", "Invalid deposit amount");
            model.addAttribute("account", accountService.getAccount(accountNum));
            return "dashboard";
        }

        String result = accountService.processDeposit(accountNum, amount);

        if (result.contains("Failed")) {
            model.addAttribute("error", result);
        } else {
            model.addAttribute("success", result);
        }

        model.addAttribute("account", accountService.getAccount(accountNum));
        return "dashboard";
    }

    @PostMapping("/account/transfer")
    public String handleTransfer(@RequestParam(required = true) String recipientAccount,
            @RequestParam(required = true) BigDecimal amount,
            Model model, HttpSession session) {

        Account sessionUser = (Account) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        String accountNum = sessionUser.getAccountNumber();

        String result = accountService.processTransfer(accountNum, recipientAccount, amount);

        if (result.contains("Failed")) {
            model.addAttribute("error", result);
        } else {
            model.addAttribute("success", result);
        }

        model.addAttribute("account", accountService.getAccount(accountNum));
        return "dashboard";
    }

    @PostMapping("/account/verify")
    public String handleVerify(Model model, HttpSession session) {
        Account sessionUser = (Account) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        accountService.processVerify(sessionUser.getAccountNumber());

        return "redirect:/dashboard";
    }

    @PostMapping("/account/suspend")
    public String handleSuspend(Model model, HttpSession session) {
        Account sessionUser = (Account) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        accountService.processSuspend(sessionUser.getAccountNumber());
        return "redirect:/dashboard";
    }

    @PostMapping("/account/close")
    public String handleClose(Model model, HttpSession session) {
        Account sessionUser = (Account) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        accountService.processClose(sessionUser.getAccountNumber());
        return "redirect:/dashboard";
    }
}
