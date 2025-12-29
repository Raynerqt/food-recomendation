package com.foodrec.controller;

import com.foodrec.entity.FollowUpEntity;
import com.foodrec.entity.RecommendationEntity;
import com.foodrec.entity.UserEntity;
import com.foodrec.model.AcuteDisease;
import com.foodrec.model.ChronicDisease;
import com.foodrec.model.Disease;
import com.foodrec.model.FoodRecommendation;
import com.foodrec.repository.FollowUpRepository;
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
import java.util.Map;
import java.util.List;

/**
 * REST Controller for Food Recommendation
 * UPDATED: Sudah support User Profile Context & Severity
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class RecommendationController {

    private final AIService aiService;
    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

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

    // ================== ENDPOINT UTAMA (Fixed) ==================

    @PostMapping("/recommend")
    public ResponseEntity<?> getRecommendation(@RequestBody Map<String, String> request, Principal principal) {
        try {
            // 1. Ambil Input Dasar
            String diseaseName = request.get("diseaseName");
            String diseaseType = request.getOrDefault("diseaseType", "chronic");

            if (diseaseName == null || diseaseName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Disease name is required"));
            }

            // 2. Ambil User Profile (Untuk memperkaya Prompt AI)
            UserEntity currentUser = getCurrentUser(principal);
            String userProfileInfo = "";
            
            if (currentUser != null) {
                // Format data diri menjadi kalimat agar AI paham
                userProfileInfo = String.format(
                    "Patient Profile: [Age: %s, Gender: %s, Weight: %s kg, Height: %s cm, Allergies: %s, Medical History: %s]. ",
                    (currentUser.getAge() != null ? currentUser.getAge() : "Unknown"),
                    (currentUser.getGender() != null ? currentUser.getGender() : "Unknown"),
                    (currentUser.getWeight() != null ? currentUser.getWeight() : "-"),
                    (currentUser.getHeight() != null ? currentUser.getHeight() : "-"),
                    (currentUser.getAllergies() != null ? currentUser.getAllergies() : "None"),
                    (currentUser.getMedicalHistory() != null ? currentUser.getMedicalHistory() : "None")
                );
            }

            // 3. Ambil Severity dari Slider
            String severityLevel = request.getOrDefault("severity", "5");

            // 4. Modifikasi Nama Penyakit untuk Prompt AI
            // Kita "titip" data profil di dalam nama penyakit supaya terbawa ke AIService
            String contextForAI = String.format(
                "%s (Severity Level: %s/10). %s", 
                diseaseName, severityLevel, userProfileInfo
            );

            // 5. Buat Object Disease dengan Konteks Lengkap
            Disease disease;
            if ("acute".equalsIgnoreCase(diseaseType)) {
                disease = new AcuteDisease(contextForAI);
            } else {
                disease = new ChronicDisease(contextForAI);
            }

            // 6. Panggil AI
            // AI akan membaca: "Maag (Severity: 8/10). Patient Profile: [Age: 25, Allergies: Seafood...]"
            FoodRecommendation recommendation = aiService.getRecommendation(disease);

            // 7. [PENTING] Kembalikan Nama Penyakit ke Aslinya
            // Supaya saat disimpan di database, namanya tetap "Maag", bukan kalimat panjang tadi
            recommendation.getDisease().setName(diseaseName);

            // 8. Simpan ke Database
            try {
                recommendationService.saveRecommendation(recommendation, currentUser);
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
    

    // ================== ENDPOINT DETAILED ==================
    
    @PostMapping("/recommend/detailed")
    public ResponseEntity<?> getDetailedRecommendation(@RequestBody Map<String, Object> request, Principal principal) {
        try {
            String diseaseName = (String) request.get("diseaseName");
            String diseaseType = (String) request.getOrDefault("diseaseType", "chronic");

            if (diseaseName == null || diseaseName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Disease name is required"));
            }

            // Untuk endpoint detailed, kita pakai logika sederhana dulu (tanpa profil)
            // Atau kamu bisa copy logika profil di atas ke sini jika mau
            Disease disease;
            if ("acute".equalsIgnoreCase(diseaseType)) {
                disease = new AcuteDisease(diseaseName);
            } else {
                disease = new ChronicDisease(diseaseName);
            }

            FoodRecommendation recommendation = aiService.getRecommendation(disease);
            UserEntity currentUser = getCurrentUser(principal);
            
            try {
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

@Autowired private FollowUpRepository followUpRepository; // Inject ini

    // 1. ENDPOINT: SIMPAN FOLLOW UP BARU (Update Logic)
    @PostMapping("/recommend/feedback/{id}")
    public ResponseEntity<?> submitFeedback(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            // Cari Kasus Induknya
            RecommendationEntity parentCase = recommendationService.getRecommendationById(id)
                    .orElseThrow(() -> new RuntimeException("Case not found"));

            String userCondition = request.get("condition");
            String userNotes = request.get("notes");
            
            // Analisa AI
            String promptData = "Original Disease: " + parentCase.getDiseaseName() + 
                                ". User Update: " + userCondition + " - " + userNotes;
            Map<String, String> aiResult = aiService.analyzeCondition(parentCase.getDiseaseName(), promptData);

            // Simpan ke Tabel FollowUpEntity (Bukan di RecommendationEntity lagi)
            FollowUpEntity entry = new FollowUpEntity();
            entry.setUserCondition(userCondition);
            entry.setUserNotes(userNotes);
            entry.setAiAdvice(aiResult.get("message"));
            
            // Hubungkan & Simpan
            parentCase.addFollowUp(entry);
            recommendationService.saveRecommendationEntity(parentCase); // Ini akan otomatis simpan followUp juga

            return ResponseEntity.ok(aiResult);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage()));
        }
    }

    // 2. ENDPOINT BARU: AMBIL LIST KASUS (Untuk Daftar Isi Jurnal)
    @GetMapping("/cases")
    public ResponseEntity<?> getMyCases(Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        // Gunakan pagination 100 agar semua kasus termuat di daftar isi
        Page<RecommendationEntity> cases = recommendationService.getAllRecommendations(principal.getName(), 0, 100);
        return ResponseEntity.ok(cases.getContent());
    }

    // 3. ENDPOINT BARU: AMBIL TIMELINE PER KASUS
    @GetMapping("/cases/{id}/timeline")
    public ResponseEntity<?> getCaseTimeline(@PathVariable Long id) {
        List<FollowUpEntity> timeline = followUpRepository.findByRecommendationIdOrderByDateDesc(id);
        return ResponseEntity.ok(timeline);
    }
    // ================== HISTORY & HELPER ==================

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) { // Tambahkan Principal
        try {
            // Ambil username dari principal (jika ada)
            String username = (principal != null) ? principal.getName() : null;
            
            // Panggil service dengan 3 parameter
            Page<RecommendationEntity> history = recommendationService.getAllRecommendations(username, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", history.getContent());
            response.put("totalPages", history.getTotalPages());
            response.put("totalElements", history.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage()));
        }
    }

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

    // Helper untuk ambil user dari Principal
    private UserEntity getCurrentUser(Principal principal) {
        if (principal != null) {
            return userRepository.findByUsername(principal.getName()).orElse(null);
        }
        return null;
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return error;
    }

    
}