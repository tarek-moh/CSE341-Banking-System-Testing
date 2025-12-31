package com.banking.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.banking.app.model.Account;
import com.banking.app.service.AccountService;
import jakarta.servlet.http.HttpSession;

/**
 * Home controller for the banking application.
 * Handles requests to the home page.
 */
@Controller
public class HomeController {

    private final AccountService accountService;

    @Autowired
    public HomeController(AccountService accountService) {
        this.accountService = accountService;
    }

    // @GetMapping("/signup")
    // public String signup() {
    // return "signup";
    // }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Account sessionUser = (Account) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        Account freshAccountData = accountService.getAccount(sessionUser.getAccountNumber());

        model.addAttribute("account", freshAccountData);
        return "dashboard";
    }

    @GetMapping("/transactions")
    public String transactions(Model model, HttpSession session) {
        Account sessionUser = (Account) session.getAttribute("loggedInUser");

        if (sessionUser == null) {
            return "redirect:/login";
        }

        Account freshAccountData = accountService.getAccount(sessionUser.getAccountNumber());
        model.addAttribute("account", freshAccountData);
        return "transactions";
    }

}
