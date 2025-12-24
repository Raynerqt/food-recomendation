package com.foodrec.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Disease Entity for Database
 * Demonstrates: JPA Entity, Database Mapping
 */
@Entity
@Table(name = "diseases")
public class DiseaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DiseaseType type;  // CHRONIC or ACUTE
    
    @Column(length = 100)
    private String category;
    
    @Column(length = 50)
    private String severity;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "dietary_restrictions", columnDefinition = "TEXT")
    private String dietaryRestrictions;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum for Disease Type
    public enum DiseaseType {
        CHRONIC, ACUTE
    }
    
    // Constructors
    public DiseaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public DiseaseEntity(String name, DiseaseType type) {
        this();
        this.name = name;
        this.type = type;
    }
    
    // JPA Callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public DiseaseType getType() {
        return type;
    }
    
    public void setType(DiseaseType type) {
        this.type = type;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }
    
    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "DiseaseEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", category='" + category + '\'' +
                ", severity='" + severity + '\'' +
                '}';
    }
}