package com.foodrec.model;

import java.util.List;
import java.util.ArrayList;

/**
 * FoodRecommendation Model
 * Demonstrates: Encapsulation, Composition
 */
public class FoodRecommendation {
    private Disease disease;
    private String aiProvider;
    private String recommendations;
    private List<String> foodsToEat;
    private List<String> foodsToAvoid;
    private String additionalNotes;
    private long timestamp;
    
    public FoodRecommendation() {
        this.foodsToEat = new ArrayList<>();
        this.foodsToAvoid = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    public FoodRecommendation(Disease disease, String aiProvider) {
        this();
        this.disease = disease;
        this.aiProvider = aiProvider;
    }
    
    // Getters and Setters
    public Disease getDisease() {
        return disease;
    }
    
    public void setDisease(Disease disease) {
        this.disease = disease;
    }
    
    public String getAiProvider() {
        return aiProvider;
    }
    
    public void setAiProvider(String aiProvider) {
        this.aiProvider = aiProvider;
    }
    
    public String getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }
    
    public List<String> getFoodsToEat() {
        return foodsToEat;
    }
    
    public void setFoodsToEat(List<String> foodsToEat) {
        this.foodsToEat = foodsToEat;
    }
    
    public List<String> getFoodsToAvoid() {
        return foodsToAvoid;
    }
    
    public void setFoodsToAvoid(List<String> foodsToAvoid) {
        this.foodsToAvoid = foodsToAvoid;
    }
    
    public String getAdditionalNotes() {
        return additionalNotes;
    }
    
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    // Utility methods
    public void addFoodToEat(String food) {
        this.foodsToEat.add(food);
    }
    
    public void addFoodToAvoid(String food) {
        this.foodsToAvoid.add(food);
    }
    
    @Override
    public String toString() {
        return "FoodRecommendation{" +
                "disease=" + (disease != null ? disease.getName() : "null") +
                ", aiProvider='" + aiProvider + '\'' +
                ", foodsToEat=" + foodsToEat.size() +
                ", foodsToAvoid=" + foodsToAvoid.size() +
                ", timestamp=" + timestamp +
                '}';
    }
}