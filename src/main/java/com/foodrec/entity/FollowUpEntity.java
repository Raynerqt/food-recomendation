package com.foodrec.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "follow_ups")
public class FollowUpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "user_condition")
    private String userCondition; // Feeling Better/Worse

    @Column(name = "user_notes", columnDefinition = "TEXT")
    private String userNotes;

    @Column(name = "ai_advice", columnDefinition = "TEXT")
    private String aiAdvice; // Saran AI spesifik untuk update ini

    // Relasi ke Tabel Rekomendasi (Induknya)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    @JsonIgnore // Biar gak looping saat convert ke JSON
    private RecommendationEntity recommendation;

    public FollowUpEntity() {
        this.date = LocalDateTime.now();
    }

    // Constructor, Getter, Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getUserCondition() { return userCondition; }
    public void setUserCondition(String userCondition) { this.userCondition = userCondition; }
    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }
    public String getAiAdvice() { return aiAdvice; }
    public void setAiAdvice(String aiAdvice) { this.aiAdvice = aiAdvice; }
    public RecommendationEntity getRecommendation() { return recommendation; }
    public void setRecommendation(RecommendationEntity recommendation) { this.recommendation = recommendation; }
}