package com.banking.app.controller;

import com.banking.app.dto.UserRegistrationDto;
import com.banking.app.model.Account;
import com.banking.app.service.AccountService;
import jakarta.servlet.http.HttpSession; // Crucial for storing the user
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final AccountService accountService;

    @Autowired
    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    // ***********************************login**********************************//
    @GetMapping("/login") // renders index
    public String showLoginPage(HttpSession session) {
        // If user is already logged in, redirect to dashboard
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "index"; // to index.html (login page)
    }

    @GetMapping("/") // renders index
    public String showRootPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "index";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String identifier,
            @RequestParam String password, HttpSession session, Model model) {
        try {
            // 1. Validate credentials via Service
            Account loggedInAccount = accountService.login(identifier, password);
            // 2. Save the user object into the session
            session.setAttribute("loggedInUser", loggedInAccount);
            // 3. Redirect to the dashboard
            return "redirect:/dashboard";
        } catch (Exception e) {
            // If login fails, stay on page and show error
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "index";
        }
    }

    // ***********************************logout**********************************//
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear session
        return "redirect:/login";
    }

    // ***********************************sign
    // up**********************************//
    @GetMapping("/signup")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignup(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
            BindingResult result, Model model) {
        // DEBUGGING LINE: Check the console when you submit "n"
        System.out.println("Validation Errors Count: " + result.getErrorCount());
        if (result.hasErrors()) {
            // Your Requirement: If a field is wrong, clear ONLY that field.
            if (result.hasFieldErrors("fullName"))
                userDto.setFullName("");
            if (result.hasFieldErrors("username"))
                userDto.setUsername("");
            if (result.hasFieldErrors("password"))
                userDto.setPassword("");
            // Return to signup.html
            return "signup";
        }

        // Validation: Check passwords match
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            // Add a global error or a specific field error manually
            model.addAttribute("error", "Passwords do not match!");
            userDto.setPassword(""); // Clear password so they have to retype
            userDto.setConfirmPassword("");
            return "signup";
        }
        try {
            // use getters from the dto
            accountService.registerUser(
                    userDto.getFullName(),
                    userDto.getUsername(),
                    userDto.getPassword());

            // Success: Redirect to login to prevent form resubmission
            return "redirect:/login?success";

        } catch (Exception e) {
            model.addAttribute("error", "Signup Failed: " + e.getMessage());
            return "signup";
        }
    }
}
