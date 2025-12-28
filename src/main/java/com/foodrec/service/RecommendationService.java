package com.foodrec.service;

import com.foodrec.entity.DiseaseEntity;
import com.foodrec.entity.RecommendationEntity;
import com.foodrec.entity.UserEntity;
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
     * ✅ UPDATED: Sekarang benar-benar menyimpan UserEntity ke database
     */
    public RecommendationEntity saveRecommendation(FoodRecommendation recommendation, UserEntity user) {
        RecommendationEntity entity = new RecommendationEntity();
        
        entity.setDiseaseName(recommendation.getDisease().getName());
        
        // Map data from FoodRecommendation to Entity
        if (recommendation.getDisease() != null) {
            entity.setDiseaseName(recommendation.getDisease().getName());
            entity.setDiseaseType(recommendation.getDisease().getCategory());
            
            // Logic Temanmu: Cek apakah penyakit sudah ada di kamus DB?
            Optional<DiseaseEntity> diseaseOpt = diseaseRepository
                .findByNameIgnoreCase(recommendation.getDisease().getName());
            
            if (diseaseOpt.isPresent()) {
                entity.setDisease(diseaseOpt.get());
            } else {
                // Logic Temanmu: Kalau belum ada, simpan sebagai penyakit baru
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
        
        // Convert lists to JSON strings (Fitur GSON Temanmu)
        if (recommendation.getFoodsToEat() != null && !recommendation.getFoodsToEat().isEmpty()) {
            entity.setFoodsToEat(gson.toJson(recommendation.getFoodsToEat()));
        }
        
        if (recommendation.getFoodsToAvoid() != null && !recommendation.getFoodsToAvoid().isEmpty()) {
            entity.setFoodsToAvoid(gson.toJson(recommendation.getFoodsToAvoid()));
        }
        
        entity.setAdditionalNotes(recommendation.getAdditionalNotes());
        // entity.setRawResponse(recommendation.getRecommendations()); // Optional kalau method ini tidak ada di entity
        
        // --- 🔥 BAGIAN PENTING YANG KITA TAMBAHKAN 🔥 ---
        // Menghubungkan Rekomendasi dengan User yang Login
        if (user != null) {
            entity.setUser(user); 
        }
        // ------------------------------------------------
        
        return recommendationRepository.save(entity);
    }
    
    // ... (Metode di bawah ini tidak berubah, tetap sesuai rancangan temanmu) ...
    
    public Page<RecommendationEntity> getAllRecommendations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return recommendationRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    public Optional<RecommendationEntity> getRecommendationById(Long id) {
        return recommendationRepository.findById(id);
    }
    
    public List<RecommendationEntity> getRecommendationsByDiseaseName(String diseaseName) {
        return recommendationRepository.findByDiseaseName(diseaseName);
    }
    
    public List<RecommendationEntity> getRecommendationsByAiProvider(String aiProvider) {
        return recommendationRepository.findByAiProvider(aiProvider);
    }
    
    public List<RecommendationEntity> searchRecommendations(String keyword) {
        return recommendationRepository.searchByDiseaseName(keyword);
    }
    
    public List<RecommendationEntity> getTodayRecommendations() {
        return recommendationRepository.findTodayRecommendations();
    }
    
    public List<RecommendationEntity> getRecommendationsBetweenDates(LocalDateTime start, LocalDateTime end) {
        return recommendationRepository.findByCreatedAtBetween(start, end);
    }
    
    public List<Object[]> getRecommendationStatsByAiProvider() {
        return recommendationRepository.countByAiProvider();
    }
    
    public List<Object[]> getMostSearchedDiseases() {
        return recommendationRepository.getMostSearchedDiseases();
    }
    
    public long getTotalRecommendationsCount() {
        return recommendationRepository.countAllRecommendations();
    }
    
    public void deleteRecommendation(Long id) {
        recommendationRepository.deleteById(id);
    }
    
    public void deleteAllRecommendations() {
        recommendationRepository.deleteAll();
    }
}