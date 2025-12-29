package com.foodrec.service;

import com.foodrec.model.Disease;
import com.foodrec.model.FoodRecommendation;
import com.foodrec.util.APIClient;
import java.util.Map; // Pastikan Import Map ada

/**
 * Abstract AIService Class
 */
public abstract class AIService {
    protected String apiKey;
    protected String model;
    protected String apiUrl;
    protected APIClient apiClient;
    
    public AIService(APIClient apiClient) {
        this.apiClient = apiClient;
    }
    
    public FoodRecommendation getRecommendation(Disease disease) {
        try {
            validateDisease(disease);
            String prompt = buildPrompt(disease);
            String response = callAI(prompt);
            FoodRecommendation recommendation = parseResponse(response, disease);
            recommendation.setAiProvider(getProviderName());
            return recommendation;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get recommendation: " + e.getMessage(), e);
        }
    }

    // === [TAMBAHAN WAJIB] ===
    // Method abstrak ini diperlukan agar Controller bisa memanggil fitur Follow Up
    public abstract Map<String, String> analyzeCondition(String diseaseName, String userFeedback);
    // ========================
    
    protected void validateDisease(Disease disease) {
        if (disease == null) throw new IllegalArgumentException("Disease cannot be null");
        if (disease.getName() == null || disease.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Disease name cannot be empty");
        }
    }
    
    protected abstract String buildPrompt(Disease disease);
    protected abstract String callAI(String prompt) throws Exception;
    protected abstract FoodRecommendation parseResponse(String response, Disease disease);
    protected abstract String getProviderName();
    
    // Getters Setters
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    
    protected String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[^a-zA-Z0-9\\s-]", "");
    }
}