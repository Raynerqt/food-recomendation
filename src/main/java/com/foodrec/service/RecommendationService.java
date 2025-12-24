package com.foodrec.service;

import com.foodrec.entity.DiseaseEntity;
import com.foodrec.entity.RecommendationEntity;
import com.foodrec.model.Disease;
import com.foodrec.model.FoodRecommendation;
import com.foodrec.repository.DiseaseRepository;
import com.foodrec.repository.RecommendationRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Recommendation Service for Database Operations
 * Demonstrates: Service Layer, Transaction Management
 */
@Service
@Transactional
public class RecommendationService {
    
    private final RecommendationRepository recommendationRepository;
    private final DiseaseRepository diseaseRepository;
    private final Gson gson;
    
    @Autowired
    public RecommendationService(
            RecommendationRepository recommendationRepository,
            DiseaseRepository diseaseRepository) {
        this.recommendationRepository = recommendationRepository;
        this.diseaseRepository = diseaseRepository;
        this.gson = new Gson();
    }
    
    /**
     * Save recommendation to database
     */
    public RecommendationEntity saveRecommendation(FoodRecommendation recommendation) {
        RecommendationEntity entity = new RecommendationEntity();
        
        // Map data from FoodRecommendation to Entity
        if (recommendation.getDisease() != null) {
            entity.setDiseaseName(recommendation.getDisease().getName());
            entity.setDiseaseType(recommendation.getDisease().getCategory());
            
            // Try to find or create disease entity
            Optional<DiseaseEntity> diseaseOpt = diseaseRepository
                .findByNameIgnoreCase(recommendation.getDisease().getName());
            
            if (diseaseOpt.isPresent()) {
                entity.setDisease(diseaseOpt.get());
            } else {
                // Create new disease entity
                DiseaseEntity newDisease = new DiseaseEntity();
                newDisease.setName(recommendation.getDisease().getName());
                newDisease.setType(recommendation.getDisease().getCategory().equalsIgnoreCase("chronic") 
                    ? DiseaseEntity.DiseaseType.CHRONIC 
                    : DiseaseEntity.DiseaseType.ACUTE);
                newDisease.setDescription(recommendation.getDisease().getDescription());
                newDisease.setDietaryRestrictions(recommendation.getDisease().getDietaryRestrictions());
                
                DiseaseEntity savedDisease = diseaseRepository.save(newDisease);
                entity.setDisease(savedDisease);
            }
        }
        
        entity.setAiProvider(recommendation.getAiProvider());
        
        // Convert lists to JSON strings
        if (recommendation.getFoodsToEat() != null && !recommendation.getFoodsToEat().isEmpty()) {
            entity.setFoodsToEat(gson.toJson(recommendation.getFoodsToEat()));
        }
        
        if (recommendation.getFoodsToAvoid() != null && !recommendation.getFoodsToAvoid().isEmpty()) {
            entity.setFoodsToAvoid(gson.toJson(recommendation.getFoodsToAvoid()));
        }
        
        entity.setAdditionalNotes(recommendation.getAdditionalNotes());
        entity.setRawResponse(recommendation.getRecommendations());
        
        return recommendationRepository.save(entity);
    }
    
    /**
     * Get all recommendations with pagination
     */
    public Page<RecommendationEntity> getAllRecommendations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return recommendationRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * Get recommendation by ID
     */
    public Optional<RecommendationEntity> getRecommendationById(Long id) {
        return recommendationRepository.findById(id);
    }
    
    /**
     * Get recommendations by disease name
     */
    public List<RecommendationEntity> getRecommendationsByDiseaseName(String diseaseName) {
        return recommendationRepository.findByDiseaseName(diseaseName);
    }
    
    /**
     * Get recommendations by AI provider
     */
    public List<RecommendationEntity> getRecommendationsByAiProvider(String aiProvider) {
        return recommendationRepository.findByAiProvider(aiProvider);
    }
    
    /**
     * Search recommendations by keyword
     */
    public List<RecommendationEntity> searchRecommendations(String keyword) {
        return recommendationRepository.searchByDiseaseName(keyword);
    }
    
    /**
     * Get today's recommendations
     */
    public List<RecommendationEntity> getTodayRecommendations() {
        return recommendationRepository.findTodayRecommendations();
    }
    
    /**
     * Get recommendations between dates
     */
    public List<RecommendationEntity> getRecommendationsBetweenDates(
            LocalDateTime start, LocalDateTime end) {
        return recommendationRepository.findByCreatedAtBetween(start, end);
    }
    
    /**
     * Get statistics - recommendations count by AI provider
     */
    public List<Object[]> getRecommendationStatsByAiProvider() {
        return recommendationRepository.countByAiProvider();
    }
    
    /**
     * Get most searched diseases
     */
    public List<Object[]> getMostSearchedDiseases() {
        return recommendationRepository.getMostSearchedDiseases();
    }
    
    /**
     * Get total count of recommendations
     */
    public long getTotalRecommendationsCount() {
        return recommendationRepository.countAllRecommendations();
    }
    
    /**
     * Delete recommendation by ID
     */
    public void deleteRecommendation(Long id) {
        recommendationRepository.deleteById(id);
    }
    
    /**
     * Delete all recommendations (use carefully!)
     */
    public void deleteAllRecommendations() {
        recommendationRepository.deleteAll();
    }
}