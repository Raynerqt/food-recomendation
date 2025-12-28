package com.foodrec.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Recommendation History Entity
 * Updated: Added relation to UserEntity
 */
@Entity
@Table(name = "recommendations")
public class RecommendationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === UPDATE RELASI USER (ONE-TO-MANY) ===
    // Kita set nullable = true DULU. 
    // Kenapa? Karena saat ini kita belum bikin fitur login.
    // Kalau diset false, aplikasi akan error karena belum ada user_id yang dikirim.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) 
    private UserEntity user;

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
    private String foodsToEat;  
    
    @Column(name = "foods_to_avoid", columnDefinition = "TEXT")
    private String foodsToAvoid;  
    
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;
    
    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;
    
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData; 
    
    @Column(name = "severity", length = 50)
    private String severity;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
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

    // === GETTER SETTER UNTUK USER ===
    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
    // ================================
    
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
    
    // FIELD BARU FEEDBACK
    @jakarta.persistence.Column(name = "user_feedback", length = 1000)
    private String userFeedback;

    @jakarta.persistence.Column(name = "follow_up_status")
    private String followUpStatus;

    @jakarta.persistence.Column(name = "ai_final_advice", length = 2000)
    private String aiFinalAdvice;

    @jakarta.persistence.Column(name = "is_session_closed")
    private boolean isSessionClosed = false;

    // GETTER SETTER FEEDBACK
    public String getUserFeedback() { return userFeedback; }
    public void setUserFeedback(String userFeedback) { this.userFeedback = userFeedback; }

    public String getFollowUpStatus() { return followUpStatus; }
    public void setFollowUpStatus(String followUpStatus) { this.followUpStatus = followUpStatus; }

    public String getAiFinalAdvice() { return aiFinalAdvice; }
    public void setAiFinalAdvice(String aiFinalAdvice) { this.aiFinalAdvice = aiFinalAdvice; }

    public boolean isSessionClosed() { return isSessionClosed; }
    public void setSessionClosed(boolean sessionClosed) { isSessionClosed = sessionClosed; }
}