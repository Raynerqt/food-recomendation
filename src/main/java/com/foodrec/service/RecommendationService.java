package com.foodrec.service;

import com.foodrec.entity.DiseaseEntity;
import com.foodrec.entity.RecommendationEntity;
import com.foodrec.entity.UserEntity; // Import UserEntity
import com.foodrec.model.FoodRecommendation;
import com.foodrec.repository.DiseaseRepository;
import com.foodrec.repository.RecommendationRepository;
import com.foodrec.repository.UserRepository; // Import UserRepository
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
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository; // === PERUBAHAN 1: Tambah UserRepository ===
    private final Gson gson = new Gson();

    @Autowired
    public RecommendationService(RecommendationRepository recommendationRepository, UserRepository userRepository) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository; // === PERUBAHAN 1: Inisialisasi ===
    }

    // 1. Simpan Rekomendasi Baru (UPDATE: Terima Username)
    public RecommendationEntity saveRecommendation(FoodRecommendation recommendation, String username) { // === PERUBAHAN 2: Parameter username ===
        RecommendationEntity entity = new RecommendationEntity();
        
        entity.setDiseaseName(recommendation.getDisease().getName());
        entity.setDiseaseType(recommendation.getDisease() instanceof com.foodrec.model.ChronicDisease ? "chronic" : "acute");
        entity.setAiProvider(recommendation.getAiProvider());
        
        // Convert List ke JSON String
        entity.setFoodsToEat(gson.toJson(recommendation.getFoodsToEat()));
        entity.setFoodsToAvoid(gson.toJson(recommendation.getFoodsToAvoid()));
        
        entity.setAdditionalNotes(recommendation.getAdditionalNotes());
        entity.setRawResponse(recommendation.getAdditionalNotes());

        // === PERUBAHAN 2: LOGIKA SIMPAN USER ===
        // Jika ada username (User sedang Login), simpan relasinya
        if (username != null && !username.isEmpty()) {
            Optional<UserEntity> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                entity.setUser(userOptional.get()); // Set Pemilik Rekomendasi
            }
        }
        // Jika username null (Guest), user_id akan null (karena nullable=true di Entity)

        return recommendationRepository.save(entity);
    }

    // 2. Simpan Entity Mentah (Tetap sama)
    public RecommendationEntity saveRecommendationEntity(RecommendationEntity entity) {
        return recommendationRepository.save(entity);
    }

    // 3. Ambil History
    public Page<RecommendationEntity> getAllRecommendations(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // LOGIKA FILTER USER
        if (username != null && !username.isEmpty()) {
            Optional<UserEntity> userOptional = userRepository.findByUsername(username);
            
            // Jika user ketemu (Login), ambil history miliknya saja
            if (userOptional.isPresent()) {
                return recommendationRepository.findByUserId(userOptional.get().getId(), pageable);
            }
        }

        // === PERBAIKAN DI SINI ===
        // Jika Guest (tidak login), kita TAMPILKAN SEMUA DATA saja dulu (Mode Testing)
        // Agar history tetap muncul di sidebar meskipun belum login.
        return recommendationRepository.findAll(pageable);
    }

    // 4. Ambil Satu Data berdasarkan ID
    public Optional<RecommendationEntity> getRecommendationById(Long id) {
        return recommendationRepository.findById(id);
    }

    // 5. Cari Berdasarkan Nama Penyakit (Bisa diupdate nanti agar search per user juga)
    public List<RecommendationEntity> searchRecommendations(String keyword) {
        return recommendationRepository.findByDiseaseNameContainingIgnoreCase(keyword);
    }

    // 6. Hapus Data
    public void deleteRecommendation(Long id) {
        recommendationRepository.deleteById(id);
    }
    
    // --- Method Tambahan untuk Statistik ---
    
    public List<RecommendationEntity> getTodayRecommendations() {
        return List.of(); 
    }

    public long getTotalRecommendationsCount() {
        return recommendationRepository.count();
    }

    public List<Object[]> getRecommendationStatsByAiProvider() {
        return List.of();
    }

    public List<Object[]> getMostSearchedDiseases() {
        return List.of();
    }
}