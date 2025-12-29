package com.foodrec.repository;

import com.foodrec.entity.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoctorRepository extends JpaRepository<DoctorEntity, Long> {
    List<DoctorEntity> findBySpecialization(String specialization);
}