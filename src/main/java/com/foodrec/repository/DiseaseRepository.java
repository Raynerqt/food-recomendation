package com.foodrec.repository;

import com.foodrec.entity.DiseaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Disease Repository Interface
 * Demonstrates: JPA Repository, Custom Queries
 */
@Repository
public interface DiseaseRepository extends JpaRepository<DiseaseEntity, Long> {
    
    // Spring Data JPA will auto-implement these methods
    
    /**
     * Find disease by name (case-insensitive)
     */
    Optional<DiseaseEntity> findByNameIgnoreCase(String name);
    
    /**
     * Find all diseases by type
     */
    List<DiseaseEntity> findByType(DiseaseEntity.DiseaseType type);
    
    /**
     * Find diseases by category
     */
    List<DiseaseEntity> findByCategory(String category);
    
    /**
     * Check if disease exists by name
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Find diseases by severity
     */
    List<DiseaseEntity> findBySeverity(String severity);
    
    /**
     * Search diseases by name containing (partial match)
     */
    List<DiseaseEntity> findByNameContainingIgnoreCase(String keyword);
    
    /**
     * Custom query - find chronic diseases
     */
    @Query("SELECT d FROM DiseaseEntity d WHERE d.type = 'CHRONIC'")
    List<DiseaseEntity> findAllChronicDiseases();
    
    /**
     * Custom query - find acute diseases
     */
    @Query("SELECT d FROM DiseaseEntity d WHERE d.type = 'ACUTE'")
    List<DiseaseEntity> findAllAcuteDiseases();
    
    /**
     * Custom query - count diseases by type
     */
    @Query("SELECT COUNT(d) FROM DiseaseEntity d WHERE d.type = :type")
    long countByType(DiseaseEntity.DiseaseType type);
}