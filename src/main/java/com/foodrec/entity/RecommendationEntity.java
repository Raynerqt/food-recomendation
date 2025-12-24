package com.foodrec.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Recommendation History Entity
 * Demonstrates: JPA Entity, One-to-Many relationship
 */
@Entity
@Table(name = "recommendations")
public class RecommendationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id")
    private DiseaseEntity disease;
    
    @Column(name = "disease_name", nullable = false, length = 200)
    private String diseaseName;
    
    @Column(name = "disease_type", length = 50)
    private String diseaseType;
    
    @Column(name = "ai_provider", length = 50)
    private String aiProvider;
    
    @Column(name = "foods_to_eat", columnDefinition = "TEXT")
    private String foodsToEat;  // Stored as JSON string
    
    @Column(name = "foods_to_avoid", columnDefinition = "TEXT")
    private String foodsToAvoid;  // Stored as JSON string
    
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;
    
    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;
    
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;  // Original request as JSON
    
    @Column(name = "severity", length = 50)
    private String severity;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public RecommendationEntity() {
        this.createdAt = LocalDateTime.now();
    }
    
    public RecommendationEntity(String diseaseName, String diseaseType, String aiProvider) {
        this();
        this.diseaseName = diseaseName;
        this.diseaseType = diseaseType;
        this.aiProvider = aiProvider;
    }
    
    // JPA Callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public DiseaseEntity getDisease() {
        return disease;
    }
    
    public void setDisease(DiseaseEntity disease) {
        this.disease = disease;
    }
    
    public String getDiseaseName() {
        return diseaseName;
    }
    
    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }
    
    public String getDiseaseType() {
        return diseaseType;
    }
    
    public void setDiseaseType(String diseaseType) {
        this.diseaseType = diseaseType;
    }
    
    public String getAiProvider() {
        return aiProvider;
    }
    
    public void setAiProvider(String aiProvider) {
        this.aiProvider = aiProvider;
    }
    
    public String getFoodsToEat() {
        return foodsToEat;
    }
    
    public void setFoodsToEat(String foodsToEat) {
        this.foodsToEat = foodsToEat;
    }
    
    public String getFoodsToAvoid() {
        return foodsToAvoid;
    }
    
    public void setFoodsToAvoid(String foodsToAvoid) {
        this.foodsToAvoid = foodsToAvoid;
    }
    
    public String getAdditionalNotes() {
        return additionalNotes;
    }
    
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
    
    public String getRawResponse() {
        return rawResponse;
    }
    
    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
    
    public String getRequestData() {
        return requestData;
    }
    
    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "RecommendationEntity{" +
                "id=" + id +
                ", diseaseName='" + diseaseName + '\'' +
                ", diseaseType='" + diseaseType + '\'' +
                ", aiProvider='" + aiProvider + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}