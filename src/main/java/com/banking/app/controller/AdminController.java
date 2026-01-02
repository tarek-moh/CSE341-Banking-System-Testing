package com.banking.app.controller;

import com.banking.app.model.Account;
import com.banking.app.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Admin controller for the banking application.
 * Handles requests to the admin page and admin operations.
 */
@Controller
public class AdminController {

    private final AccountService accountService;

    @Autowired
    public AdminController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/admin")
    public String admin(HttpSession session, Model model) {
        // Check if user is logged in and is admin
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            return "redirect:/login";
        }

        // Get all accounts (excluding admin accounts from the list)
        List<Account> allAccounts = accountService.getAllAccounts();
        List<Account> accounts = allAccounts.stream()
                .filter(acc -> !acc.isAdmin())
                .collect(java.util.stream.Collectors.toList());
        
        // Calculate counts
        long unverifiedCount = accounts.stream()
                .filter(acc -> acc.getStatus().name().equals("UNVERIFIED"))
                .count();
        long verifiedCount = accounts.stream()
                .filter(acc -> acc.getStatus().name().equals("VERIFIED"))
                .count();
        long suspendedCount = accounts.stream()
                .filter(acc -> acc.getStatus().name().equals("SUSPENDED"))
                .count();
        long closedCount = accounts.stream()
                .filter(acc -> acc.getStatus().name().equals("CLOSED"))
                .count();
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("unverifiedCount", unverifiedCount);
        model.addAttribute("verifiedCount", verifiedCount);
        model.addAttribute("suspendedCount", suspendedCount);
        model.addAttribute("closedCount", closedCount);
        return "admin";
    }

    @PostMapping("/admin/verify")
    public String verifyAccount(@RequestParam String accountNumber, HttpSession session, Model model) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            return "redirect:/login";
        }

        String result = accountService.processVerify(accountNumber);
        model.addAttribute("message", result);
        return "redirect:/admin?message=" + java.net.URLEncoder.encode(result, java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/admin/suspend")
    public String suspendAccount(@RequestParam String accountNumber, HttpSession session, Model model) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            return "redirect:/login";
        }

        String result = accountService.processSuspend(accountNumber);
        model.addAttribute("message", result);
        return "redirect:/admin?message=" + java.net.URLEncoder.encode(result, java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/admin/close")
    public String closeAccount(@RequestParam String accountNumber, HttpSession session, Model model) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            return "redirect:/login";
        }

        String result = accountService.processClose(accountNumber);
        model.addAttribute("message", result);
        return "redirect:/admin?message=" + java.net.URLEncoder.encode(result, java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/admin/violation")
    public String applyViolation(@RequestParam String accountNumber, HttpSession session, Model model) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            return "redirect:/login";
        }

        String result = accountService.processViolation(accountNumber);
        model.addAttribute("message", result);
        return "redirect:/admin?message=" + java.net.URLEncoder.encode(result, java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/admin/admin-action")
    public String applyAdminAction(@RequestParam String accountNumber, HttpSession session, Model model) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            return "redirect:/login";
        }

        String result = accountService.processAdminAction(accountNumber);
        model.addAttribute("message", result);
        return "redirect:/admin?message=" + java.net.URLEncoder.encode(result, java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/admin/appeal")
    public String processAppeal(@RequestParam String accountNumber, HttpSession session, Model model) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            return "redirect:/login";
        }

        String result = accountService.processAppeal(accountNumber);
        model.addAttribute("message", result);
        return "redirect:/admin?message=" + java.net.URLEncoder.encode(result, java.nio.charset.StandardCharsets.UTF_8);
    }
}
