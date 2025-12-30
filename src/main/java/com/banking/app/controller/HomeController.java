package com.banking.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.banking.app.service.AccountService;

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

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("account", accountService.getAccount("123"));
        return "dashboard";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("account", accountService.getAccount("123"));
        return "transactions";
    }
}
