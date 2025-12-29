package com.foodrec.service;

import com.foodrec.entity.RecommendationEntity;
import com.foodrec.entity.UserEntity;
import com.foodrec.model.FoodRecommendation;
import com.foodrec.repository.RecommendationRepository;
import com.foodrec.repository.UserRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RecommendationService {
    
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final Gson gson;
    
    @Autowired
    public RecommendationService(
            RecommendationRepository recommendationRepository,
            UserRepository userRepository) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.gson = new Gson();
    }
    
    // === [FIX UTAMA: Parameter diganti jadi UserEntity] ===
    public RecommendationEntity saveRecommendation(FoodRecommendation recommendation, UserEntity user) {
        RecommendationEntity entity = new RecommendationEntity();
        
        // Mapping data dari AI Model ke Entity Database
        entity.setDiseaseName(recommendation.getDisease().getName());
        entity.setDiseaseType(recommendation.getDisease().getCategory());
        entity.setAiProvider(recommendation.getAiProvider());
        
        // Convert List ke JSON String agar bisa masuk Database
        if (recommendation.getFoodsToEat() != null) {
            entity.setFoodsToEat(gson.toJson(recommendation.getFoodsToEat()));
        }
        if (recommendation.getFoodsToAvoid() != null) {
            entity.setFoodsToAvoid(gson.toJson(recommendation.getFoodsToAvoid()));
        }
        entity.setAdditionalNotes(recommendation.getAdditionalNotes());
        
        // SET USER (Penting agar muncul di Jurnal User tersebut)
        if (user != null) {
            entity.setUser(user);
        }
        
        return recommendationRepository.save(entity);
    }

    // Method untuk menyimpan update Follow Up
    public RecommendationEntity saveRecommendationEntity(RecommendationEntity entity) {
        return recommendationRepository.save(entity);
    }
    
    // Ambil History berdasarkan User yang Login
    public Page<RecommendationEntity> getAllRecommendations(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (username != null && !username.isEmpty()) {
            Optional<UserEntity> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                // Cari data milik User ID tersebut
                return recommendationRepository.findByUserId(userOptional.get().getId(), pageable);
            }
        }
        // Fallback: Jika error user tidak ketemu, return kosong (bukan semua data orang lain)
        return Page.empty();
    }
    
    public Optional<RecommendationEntity> getRecommendationById(Long id) {
        return recommendationRepository.findById(id);
    }
    
    public void deleteRecommendation(Long id) {
        recommendationRepository.deleteById(id);
    }

    // --- Dummy Methods untuk Kompatibilitas Controller Temanmu ---
    public List<RecommendationEntity> searchRecommendations(String keyword) { return List.of(); }
    public List<RecommendationEntity> getTodayRecommendations() { return List.of(); }
    public long getTotalRecommendationsCount() { return recommendationRepository.count(); }
    public List<Object[]> getRecommendationStatsByAiProvider() { return List.of(); }
    public List<Object[]> getMostSearchedDiseases() { return List.of(); }
    public List<RecommendationEntity> getRecommendationsBetweenDates(LocalDateTime start, LocalDateTime end) { return List.of(); }
}