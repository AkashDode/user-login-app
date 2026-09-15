package com.loginapp.controller;

import com.loginapp.dto.LoginForm;
import com.loginapp.dto.RegisterForm;
import com.loginapp.entity.User;
import com.loginapp.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private static final String SESSION_USER = "loggedInUser";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ---------------------------------------------------------------------
    // Page 1: Login
    // ---------------------------------------------------------------------
    @GetMapping("/")
    public String root(HttpSession session) {
        // Already signed in? Skip straight to the welcome page.
        if (session.getAttribute(SESSION_USER) != null) {
            return "redirect:/welcome";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLogin(HttpSession session, Model model) {
        if (session.getAttribute(SESSION_USER) != null) {
            return "redirect:/welcome";
        }
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(
            @Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        Optional<User> user = userService.authenticate(loginForm.getUsername(), loginForm.getPassword());

        if (user.isEmpty()) {
            model.addAttribute("loginError", "Invalid username or password.");
            return "login";
        }

        session.setAttribute(SESSION_USER, user.get().getUsername());
        return "redirect:/welcome";
    }

    // ---------------------------------------------------------------------
    // Page 2: Register
    // ---------------------------------------------------------------------
    @GetMapping("/register")
    public String showRegister(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(
            @Valid @ModelAttribute("registerForm") RegisterForm registerForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (userService.usernameExists(registerForm.getUsername())) {
            bindingResult.rejectValue("username", "duplicate", "That username is already taken.");
        }
        if (userService.emailExists(registerForm.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "That email is already registered.");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(registerForm);
        redirectAttributes.addFlashAttribute("registerSuccess",
                "Account created successfully. Please sign in.");
        return "redirect:/login";
    }

    // ---------------------------------------------------------------------
    // Page 3: Welcome
    // ---------------------------------------------------------------------
    @GetMapping("/welcome")
    public String welcome(HttpSession session, Model model) {
        Object username = session.getAttribute(SESSION_USER);
        if (username == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        return "welcome";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
