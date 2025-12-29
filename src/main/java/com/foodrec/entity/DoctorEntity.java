package com.foodrec.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class DoctorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String specialization; // e.g., "Gastroenterologist", "Nutritionist"
    private String hospital;
    private String phoneNumber;
    private String location;
    private String imageUrl; // URL foto dokter (bisa link online)

    public DoctorEntity() {}
    public DoctorEntity(String name, String specialization, String hospital, String phoneNumber, String location, String imageUrl) {
        this.name = name;
        this.specialization = specialization;
        this.hospital = hospital;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    // Getter & Setter (Penting!)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}