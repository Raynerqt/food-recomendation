package com.foodrec.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String username;
    
    @Column(unique = true, nullable = false, length = 200)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    // --- TAMBAHAN KITA (Agar Controller tidak error) ---
    @Column(name = "role")
    private String role;
    // ----------------------------------------------------
    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    private String gender; // Male/Female

    @Column(name = "height")
    private Double height; // cm

    @Column(name = "weight")
    private Double weight; // kg

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies; // Contoh: Kacang, Seafood

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory; // Contoh: Diabetes, Hipertensi

    @Column(name = "full_name", length = 200)
    private String fullName;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    public UserEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor kita update sedikit biar support role
    public UserEntity(String username, String email, String password, String role) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // --- GETTERS & SETTERS ---
    // (Getter Setter lama biarkan saja, tambahkan yang Role)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // GETTER SETTER ROLE (PENTING!)
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    @Lob // Menandakan ini data besar (Large Object)
    @Column(name = "profile_image", columnDefinition = "LONGBLOB") 
    private byte[] profileImage;

    // Getter & Setter Wajib
    public byte[] getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }
}