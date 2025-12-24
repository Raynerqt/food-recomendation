package com.foodrec.model;

/**
 * Abstract base class for Disease
 * Demonstrates: Abstraction, Encapsulation
 */
public abstract class Disease {
    private String name;
    private String category;
    private String severity;
    
    // Constructor
    public Disease(String name, String category) {
        this.name = name;
        this.category = category;
        this.severity = "Unknown";
    }
    
    // Encapsulation: Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
    
    // Abstract method - must be implemented by subclasses
    public abstract String getDescription();
    
    // Abstract method for dietary restrictions
    public abstract String getDietaryRestrictions();
    
    // Concrete method available to all subclasses
    public String getFullInfo() {
        return String.format("Disease: %s, Category: %s, Severity: %s", 
                           name, category, severity);
    }
    
    @Override
    public String toString() {
        return "Disease{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", severity='" + severity + '\'' +
                '}';
    }
}