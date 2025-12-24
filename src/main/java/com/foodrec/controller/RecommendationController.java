package com.foodrec.controller;

import com.foodrec.entity.RecommendationEntity;
import com.foodrec.model.AcuteDisease;
import com.foodrec.model.ChronicDisease;
import com.foodrec.model.Disease;
import com.foodrec.model.FoodRecommendation;
import com.foodrec.service.AIService;
import com.foodrec.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Food Recommendation
 * Demonstrates: Composition, Dependency Injection, Polymorphism
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend to access
public class RecommendationController {
    
    // Dependency Injection - demonstrates Polymorphism
    private final AIService openAIService;
    private final AIService claudeAIService;
    private final AIService geminiAIService;
    private final RecommendationService recommendationService;
    
    @Autowired
    public RecommendationController(
            @Qualifier("openAIService") AIService openAIService,
            @Qualifier("claudeAIService") AIService claudeAIService,
            @Qualifier("geminiAIService") AIService geminiAIService,
            RecommendationService recommendationService) {
        this.openAIService = openAIService;
        this.claudeAIService = claudeAIService;
        this.geminiAIService = geminiAIService;
        this.recommendationService = recommendationService;
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Food Recommendation API is running");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get food recommendation based on disease
     * POST /api/recommend
     */
    @PostMapping("/recommend")
    public ResponseEntity<?> getRecommendation(@RequestBody Map<String, String> request) {
        try {
            String diseaseName = request.get("diseaseName");
            String diseaseType = request.getOrDefault("diseaseType", "chronic");
            String aiProvider = request.getOrDefault("aiProvider", "gemini"); // Default Gemini!
            
            if (diseaseName == null || diseaseName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Disease name is required"));
            }
            
            // Create Disease object based on type
            Disease disease;
            if ("acute".equalsIgnoreCase(diseaseType)) {
                disease = new AcuteDisease(diseaseName);
            } else {
                disease = new ChronicDisease(diseaseName);
            }
            
            // Select AI service (Gemini by default)
            AIService selectedService = selectAIService(aiProvider);
            
            // Get recommendation
            FoodRecommendation recommendation = selectedService.getRecommendation(disease);
            
            // Save to database
            try {
                RecommendationEntity savedEntity = recommendationService.saveRecommendation(recommendation);
                System.out.println("✅ Saved to database with ID: " + savedEntity.getId());
            } catch (Exception dbError) {
                System.err.println("⚠️ Failed to save to database: " + dbError.getMessage());
            }
            
            return ResponseEntity.ok(recommendation);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get recommendation: " + e.getMessage()));
        }
    }
    
    /**
     * Get recommendation with detailed disease info
     * POST /api/recommend/detailed
     */
    @PostMapping("/recommend/detailed")
    public ResponseEntity<?> getDetailedRecommendation(@RequestBody Map<String, Object> request) {
        try {
            String diseaseName = (String) request.get("diseaseName");
            String diseaseType = (String) request.getOrDefault("diseaseType", "chronic");
            String aiProvider = (String) request.getOrDefault("aiProvider", "gemini"); // Default Gemini!
            
            if (diseaseName == null || diseaseName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Disease name is required"));
            }
            
            // Create Disease object with additional details
            Disease disease;
            if ("acute".equalsIgnoreCase(diseaseType)) {
                AcuteDisease acuteDisease = new AcuteDisease(diseaseName);
                
                if (request.containsKey("recoveryDays")) {
                    acuteDisease.setExpectedRecoveryDays(
                        Integer.parseInt(request.get("recoveryDays").toString())
                    );
                }
                if (request.containsKey("severity")) {
                    acuteDisease.setSeverity((String) request.get("severity"));
                }
                
                disease = acuteDisease;
            } else {
                ChronicDisease chronicDisease = new ChronicDisease(diseaseName);
                
                if (request.containsKey("managementType")) {
                    chronicDisease.setManagementType((String) request.get("managementType"));
                }
                if (request.containsKey("severity")) {
                    chronicDisease.setSeverity((String) request.get("severity"));
                }
                
                disease = chronicDisease;
            }
            
            // Select AI service
            AIService selectedService = selectAIService(aiProvider);
            
            // Get recommendation
            FoodRecommendation recommendation = selectedService.getRecommendation(disease);
            
            // Save to database
            try {
                RecommendationEntity savedEntity = recommendationService.saveRecommendation(recommendation);
                System.out.println("✅ Saved to database with ID: " + savedEntity.getId());
            } catch (Exception dbError) {
                System.err.println("⚠️ Failed to save to database: " + dbError.getMessage());
            }
            
            return ResponseEntity.ok(recommendation);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get recommendation: " + e.getMessage()));
        }
    }
    
    /**
     * Select AI service based on provider name
     */
    private AIService selectAIService(String provider) {
        switch (provider.toLowerCase()) {
            case "openai":
                return openAIService;
            case "claude":
                return claudeAIService;
            case "gemini":
            default:
                return geminiAIService; // Default to Gemini!
        }
    }
    
    // ========== DATABASE ENDPOINTS ==========
    
    /**
     * Get all recommendation history with pagination
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<RecommendationEntity> history = recommendationService.getAllRecommendations(page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", history.getContent());
            response.put("totalElements", history.getTotalElements());
            response.put("totalPages", history.getTotalPages());
            response.put("currentPage", history.getNumber());
            response.put("pageSize", history.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch history: " + e.getMessage()));
        }
    }
    
    /**
     * Get recommendation by ID
     */
    @GetMapping("/history/{id}")
    public ResponseEntity<?> getRecommendationById(@PathVariable Long id) {
        try {
            return recommendationService.getRecommendationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch recommendation: " + e.getMessage()));
        }
    }
    
    /**
     * Search recommendations by disease name
     */
    @GetMapping("/history/search")
    public ResponseEntity<?> searchHistory(@RequestParam String keyword) {
        try {
            List<RecommendationEntity> results = recommendationService.searchRecommendations(keyword);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to search: " + e.getMessage()));
        }
    }
    
    /**
     * Get today's recommendations
     */
    @GetMapping("/history/today")
    public ResponseEntity<?> getTodayHistory() {
        try {
            List<RecommendationEntity> today = recommendationService.getTodayRecommendations();
            return ResponseEntity.ok(today);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch today's history: " + e.getMessage()));
        }
    }
    
    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("totalRecommendations", recommendationService.getTotalRecommendationsCount());
            
            List<Object[]> byProvider = recommendationService.getRecommendationStatsByAiProvider();
            Map<String, Long> providerStats = new HashMap<>();
            for (Object[] row : byProvider) {
                providerStats.put((String) row[0], (Long) row[1]);
            }
            stats.put("byAiProvider", providerStats);
            
            List<Object[]> topDiseases = recommendationService.getMostSearchedDiseases();
            stats.put("topDiseases", topDiseases);
            
            stats.put("todayCount", recommendationService.getTodayRecommendations().size());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Delete recommendation by ID
     */
    @DeleteMapping("/history/{id}")
    public ResponseEntity<?> deleteRecommendation(@PathVariable Long id) {
        try {
            if (!recommendationService.getRecommendationById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            recommendationService.deleteRecommendation(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Recommendation deleted successfully");
            response.put("id", id.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to delete: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method to create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return error;
    }
}