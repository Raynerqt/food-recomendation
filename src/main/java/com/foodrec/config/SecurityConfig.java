package com.foodrec.config;

import com.foodrec.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Matikan CSRF sementara biar aman buat dev
            .authorizeHttpRequests(auth -> auth
                // 1. DAFTAR HALAMAN PUBLIK (Boleh diakses tanpa login)
                .requestMatchers(
                    "/login",           // Halaman Login
                    "/register",        // Halaman Register
                    "/style.css",       // CSS
                    "/script.js",       // JS
                    "/images/**",       // Gambar
                    "/api/health"       // Cek status server
                ).permitAll()
                
                // 2. SELAIN DI ATAS, WAJIB LOGIN (Termasuk "/" Dashboard, "/journal", "/profile")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")          // Jika belum login, lempar ke sini
                .defaultSuccessUrl("/", true) // Jika sukses login, masuk ke Dashboard
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout") // Setelah logout, balik ke login
                .invalidateHttpSession(true)       // Hapus sesi
                .deleteCookies("JSESSIONID")       // Hapus cookie
                .permitAll()
            );

        return http.build();
    }
}