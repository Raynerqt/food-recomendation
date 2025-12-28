package com.foodrec.controller;

import com.foodrec.entity.UserEntity;
import com.foodrec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class WebController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    // --- INI METHOD HOME YANG BENAR (Hanya Boleh Satu) ---
    @GetMapping("/")
    public String home(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("username", username);
        } else {
            model.addAttribute("username", "Guest");
        }
        return "index";
    }
    // -----------------------------------------------------

    @PostMapping("/register")
    public String processRegister(
            @RequestParam String username, 
            @RequestParam String email, 
            @RequestParam String password) {
        
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/register?error";
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("USER");
        newUser.setIsActive(true);

        userRepository.save(newUser);

        return "redirect:/login";
    }
}