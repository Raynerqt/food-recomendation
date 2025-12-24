package com.foodrec.repository;

import com.foodrec.entity.RecommendationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Recommendation Repository Interface
 * Demonstrates: JPA Repository, Pagination, Custom Queries
 */
@Repository
public interface RecommendationRepository extends JpaRepository<RecommendationEntity, Long> {
    
    /**
     * Find all recommendations by disease name
     */
    List<RecommendationEntity> findByDiseaseName(String diseaseName);
    
    /**
     * Find recommendations by AI provider
     */
    List<RecommendationEntity> findByAiProvider(String aiProvider);
    
    /**
     * Find recommendations by disease type
     */
    List<RecommendationEntity> findByDiseaseType(String diseaseType);
    
    /**
     * Find recommendations created after certain date
     */
    List<RecommendationEntity> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find recommendations between dates
     */
    List<RecommendationEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Find latest N recommendations with pagination
     */
    Page<RecommendationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Custom query - find recommendations by disease name (case-insensitive)
     */
    @Query("SELECT r FROM RecommendationEntity r WHERE LOWER(r.diseaseName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<RecommendationEntity> searchByDiseaseName(String keyword);
    
    /**
     * Custom query - count recommendations by AI provider
     */
    @Query("SELECT r.aiProvider, COUNT(r) FROM RecommendationEntity r GROUP BY r.aiProvider")
    List<Object[]> countByAiProvider();
    
    /**
     * Custom query - get most searched diseases
     */
    @Query("SELECT r.diseaseName, COUNT(r) as cnt FROM RecommendationEntity r GROUP BY r.diseaseName ORDER BY cnt DESC")
    List<Object[]> getMostSearchedDiseases();
    
    /**
     * Custom query - find today's recommendations
     */
    @Query("SELECT r FROM RecommendationEntity r WHERE DATE(r.createdAt) = CURRENT_DATE")
    List<RecommendationEntity> findTodayRecommendations();
    
    /**
     * Count total recommendations
     */
    @Query("SELECT COUNT(r) FROM RecommendationEntity r")
    long countAllRecommendations();
}