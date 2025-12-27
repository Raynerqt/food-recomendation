package com.foodrec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Matikan CSRF agar mudah testing API lewat Postman/Frontend
            .csrf(csrf -> csrf.disable())
            
            // 2. Aturan: Semua halaman WAJIB Login
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            
            // 3. GUNAKAN HALAMAN LOGIN BAWAAN SPRING (Yang "tiba-tiba muncul" itu)
            // Jangan gunakan .loginPage("/login"), biarkan default saja.
            .formLogin(Customizer.withDefaults())
            
            // 4. Logout juga default
            .logout(Customizer.withDefaults());

        return http.build();
    }
}