package com.foodrec.service;

import com.foodrec.model.Disease;
import com.foodrec.model.FoodRecommendation;
import com.foodrec.util.APIClient;
import java.util.Map; // Perbaikan: Huruf 'M' harus besar

/**
 * Abstract AIService Class
 * Demonstrates: Abstract Class, Template Method Pattern, Encapsulation
 */
public abstract class AIService {
    protected String apiKey;
    protected String model;
    protected String apiUrl;
    protected APIClient apiClient;
    
    // Constructor
    public AIService(APIClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Template Method - defines the skeleton of the algorithm
     * This is the main method that will be called
     */
    public FoodRecommendation getRecommendation(Disease disease) {
        try {
            // Step 1: Validate input
            validateDisease(disease);
            
            // Step 2: Build prompt (abstract - implemented by subclasses)
            String prompt = buildPrompt(disease);
            
            // Step 3: Call AI API (abstract - implemented by subclasses)
            String response = callAI(prompt);
            
            // Step 4: Parse response
            FoodRecommendation recommendation = parseResponse(response, disease);
            
            // Step 5: Add metadata
            recommendation.setAiProvider(getProviderName());
            
            return recommendation;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get recommendation: " + e.getMessage(), e);
        }
    }

    // === METHOD BARU UNTUK ANALISA KONDISI (FEEDBACK LOOP) ===
    // Ini harus ada di sini agar Controller bisa memanggilnya
    public abstract Map<String, String> analyzeCondition(String diseaseName, String userFeedback);
    
    /**
     * Validate disease input
     */
    protected void validateDisease(Disease disease) {
        if (disease == null) {
            throw new IllegalArgumentException("Disease cannot be null");
        }
        if (disease.getName() == null || disease.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Disease name cannot be empty");
        }
    }
    
    /**
     * Abstract method - must be implemented by subclasses
     * Build the prompt for the AI
     */
    protected abstract String buildPrompt(Disease disease);
    
    /**
     * Abstract method - must be implemented by subclasses
     * Call the AI API with the prompt
     */
    protected abstract String callAI(String prompt) throws Exception;
    
    /**
     * Abstract method - must be implemented by subclasses
     * Parse the AI response into FoodRecommendation object
     */
    protected abstract FoodRecommendation parseResponse(String response, Disease disease);
    
    /**
     * Abstract method - must be implemented by subclasses
     * Get the provider name (OpenAI, Claude, Gemini, etc.)
     */
    protected abstract String getProviderName();
    
    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    /**
     * Common utility method for all subclasses
     */
    protected String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[^a-zA-Z0-9\\s-]", "");
    }
    
}