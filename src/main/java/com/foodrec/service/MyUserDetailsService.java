package com.foodrec.service;

import com.foodrec.entity.UserEntity;
import com.foodrec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Cari user di database
        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + username));

        // 2. Cek apakah akun aktif (karena ada field isActive)
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("Akun tidak aktif");
        }

        // 3. Kembalikan user ke Spring Security
        // Kita pakai {noop} agar password tidak perlu dienkripsi (agar kamu mudah insert manual di DB)
        return User.withUsername(user.getUsername())
                .password("{noop}" + user.getPassword()) 
                .roles("USER") 
                .build();
    }
}