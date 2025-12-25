package com.banking.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Admin controller for the banking application.
 * Handles requests to the admin page.
 */
@Controller
public class AdminController {

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
