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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.IOException;

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
        // Kita tidak perlu lagi cek 'if (principal != null)' untuk redirect login
        // karena SecurityConfig sudah menahannya di depan pintu.
        
        String username = principal.getName();
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null) {
            // Cek apakah user baru (belum isi umur)? Lempar ke Profile
            if (user.getAge() == null) {
                return "redirect:/profile";
            }
            model.addAttribute("username", user.getUsername());
        }
        
        return "index"; // Buka Dashboard
    }
    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        String username = principal.getName();
        UserEntity user = userRepository.findByUsername(username).orElseThrow();
        
        model.addAttribute("user", user); // Kirim data user ke HTML
        return "profile";
    }

    @GetMapping("/journal")
    public String showJournal(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            UserEntity user = userRepository.findByUsername(username).orElse(null);
            model.addAttribute("user", user);
            // Nanti di sini kita load data history follow-up dari database temanmu
        } else {
            return "redirect:/login";
        }
        return "history"; // Mengarah ke templates/history.html
    }
    // -----------------------------------------------------

    @PostMapping("/profile")
    public String saveProfile(
            @RequestParam Integer age,
            @RequestParam String gender,
            @RequestParam(required = false) Double weight,
            @RequestParam(required = false) Double height,
            @RequestParam(required = false) String medicalHistory,
            @RequestParam(required = false) String allergies,
            Principal principal) {
        
        if (principal != null) {
            String username = principal.getName();
            UserEntity user = userRepository.findByUsername(username).orElseThrow();
            
            user.setAge(age);
            user.setGender(gender);
            user.setWeight(weight);
            user.setHeight(height);
            user.setMedicalHistory(medicalHistory);
            user.setAllergies(allergies);
            
            userRepository.save(user); // Simpan ke DB
        }
        
        return "redirect:/"; // Balik ke dashboard setelah simpan
    }
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
    // 1. ENDPOINT UPLOAD FOTO
    @PostMapping("/profile/upload")
    public String uploadProfilePhoto(@RequestParam("photo") MultipartFile file, Principal principal) {
        if (principal != null && !file.isEmpty()) {
            try {
                String username = principal.getName();
                UserEntity user = userRepository.findByUsername(username).orElseThrow();
                
                // Konversi file gambar menjadi byte[] dan simpan ke database
                user.setProfileImage(file.getBytes());
                userRepository.save(user);
                
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/journal?error=upload_failed";
            }
        }
        return "redirect:/journal"; // Refresh halaman jurnal
    }

    // 2. ENDPOINT MENAMPILKAN FOTO (Agar bisa dilihat di <img>)
    @GetMapping("/profile/image")
    @ResponseBody
    public ResponseEntity<byte[]> getProfileImage(Principal principal) {
        if (principal != null) {
            UserEntity user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null && user.getProfileImage() != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // Atau PNG, browser biasanya pintar deteksi
                        .body(user.getProfileImage());
            }
        }
        return ResponseEntity.notFound().build();
    }
}