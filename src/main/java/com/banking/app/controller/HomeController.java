package com.banking.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home controller for the banking application.
 * Handles requests to the home page.
 */
@Controller
public class HomeController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        return "dashboard";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {

        return "transactions";
    }
}
