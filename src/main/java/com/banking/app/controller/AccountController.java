package com.banking.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

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
}
