package com.foodrec.controller;

import com.foodrec.entity.RecommendationEntity;
import com.foodrec.entity.UserEntity;
import com.foodrec.model.AcuteDisease;
import com.foodrec.model.ChronicDisease;
import com.foodrec.model.Disease;
import com.foodrec.model.FoodRecommendation;
import com.foodrec.repository.UserRepository;
import com.foodrec.service.AIService;
import com.foodrec.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Food Recommendation
 * FIXED: Sekarang sinkron dengan RecommendationService yang butuh UserEntity
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class RecommendationController {

    private final AIService aiService;
    private final RecommendationService recommendationService;
    private final UserRepository userRepository; // ✅ Tambahan Wajib

    // ✅ Constructor Injection yang Benar
    @Autowired
    public RecommendationController(AIService aiService, 
                                    RecommendationService recommendationService,
                                    UserRepository userRepository) {
        this.aiService = aiService;
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Food Recommendation API is running");
        return ResponseEntity.ok(response);
    }

    // ================== ENDPOINT UTAMA ==================

    @PostMapping("/recommend")
    public ResponseEntity<?> getRecommendation(@RequestBody Map<String, String> request, Principal principal) {
        try {
            String diseaseName = request.get("diseaseName");
            String diseaseType = request.getOrDefault("diseaseType", "chronic");

            if (diseaseName == null || diseaseName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Disease name is required"));
            }

            // 1. Buat Object Disease
            Disease disease;
            if ("acute".equalsIgnoreCase(diseaseType)) {
                disease = new AcuteDisease(diseaseName);
            } else {
                disease = new ChronicDisease(diseaseName);
            }

            // 2. Tanya AI
            FoodRecommendation recommendation = aiService.getRecommendation(disease);

            // 3. Ambil User yang Login (Kalau ada)
            UserEntity currentUser = getCurrentUser(principal);

            // 4. Simpan ke Database (Kirim 2 Parameter: Rekomendasi + User)
            try {
                // ✅ INI PERBAIKANNYA: Kirim 'currentUser' ke service
                RecommendationEntity savedEntity = recommendationService.saveRecommendation(recommendation, currentUser);
                System.out.println("✅ Saved recommendation for user: " + (currentUser != null ? currentUser.getUsername() : "Guest"));
            } catch (Exception dbError) {
                System.err.println("⚠️ Database save failed: " + dbError.getMessage());
            }

            return ResponseEntity.ok(recommendation);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Server Error: " + e.getMessage()));
        }
    }

    @PostMapping("/recommend/detailed")
    public ResponseEntity<?> getDetailedRecommendation(@RequestBody Map<String, Object> request, Principal principal) {
        try {
            String diseaseName = (String) request.get("diseaseName");
            String diseaseType = (String) request.getOrDefault("diseaseType", "chronic");

            if (diseaseName == null || diseaseName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Disease name is required"));
            }

            // Logic Disease Detail
            Disease disease;
            if ("acute".equalsIgnoreCase(diseaseType)) {
                AcuteDisease acuteDisease = new AcuteDisease(diseaseName);
                if (request.containsKey("recoveryDays")) {
                    acuteDisease.setExpectedRecoveryDays(Integer.parseInt(request.get("recoveryDays").toString()));
                }
                if (request.containsKey("severity")) acuteDisease.setSeverity((String) request.get("severity"));
                disease = acuteDisease;
            } else {
                ChronicDisease chronicDisease = new ChronicDisease(diseaseName);
                if (request.containsKey("managementType")) chronicDisease.setManagementType((String) request.get("managementType"));
                if (request.containsKey("severity")) chronicDisease.setSeverity((String) request.get("severity"));
                disease = chronicDisease;
            }

            // Tanya AI
            FoodRecommendation recommendation = aiService.getRecommendation(disease);

            // Ambil User & Simpan
            UserEntity currentUser = getCurrentUser(principal);
            try {
                // ✅ FIX JUGA DI SINI
                recommendationService.saveRecommendation(recommendation, currentUser);
            } catch (Exception dbError) {
                System.err.println("⚠️ Database save failed: " + dbError.getMessage());
            }

            return ResponseEntity.ok(recommendation);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed: " + e.getMessage()));
        }
    }

    // ================== HISTORY & STATS ==================

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<RecommendationEntity> history = recommendationService.getAllRecommendations(page, size);
            
            // Bungkus response manual biar rapi
            Map<String, Object> response = new HashMap<>();
            response.put("content", history.getContent());
            response.put("totalPages", history.getTotalPages());
            response.put("totalElements", history.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage()));
        }
    }

    // Helper untuk ambil user dari Principal
    private UserEntity getCurrentUser(Principal principal) {
        if (principal != null) {
            return userRepository.findByUsername(principal.getName()).orElse(null);
        }
        return null;
    }

    // Endpoint lainnya (GetById, Search, Stats, Delete) tetap sama,
    // karena mereka tidak memanggil saveRecommendation.
    // Copy paste saja method-method di bawah ini dari file lama jika butuh, 
    // atau biarkan method di atas yang paling krusial.

    @GetMapping("/history/{id}")
    public ResponseEntity<?> getRecommendationById(@PathVariable Long id) {
        return recommendationService.getRecommendationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<?> deleteRecommendation(@PathVariable Long id) {
        recommendationService.deleteRecommendation(id);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return error;
    }
}