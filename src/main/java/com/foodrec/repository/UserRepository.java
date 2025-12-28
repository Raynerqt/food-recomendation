package com.foodrec.repository;

import com.foodrec.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // Spring Data JPA akan otomatis membuat query SQL dari nama method ini
    Optional<UserEntity> findByUsername(String username);
}