package com.foodrec.model;

/**
 * Chronic Disease class
 * Demonstrates: Inheritance, Method Overriding
 */
public class ChronicDisease extends Disease {
    private boolean requiresLongTermManagement;
    private String managementType;
    
    public ChronicDisease(String name) {
        super(name, "Chronic");
        this.requiresLongTermManagement = true;
        this.managementType = "Lifestyle and Diet";
    }
    
    public ChronicDisease(String name, String managementType) {
        super(name, "Chronic");
        this.requiresLongTermManagement = true;
        this.managementType = managementType;
    }
    
    // Getters and Setters
    public boolean isRequiresLongTermManagement() {
        return requiresLongTermManagement;
    }
    
    public void setRequiresLongTermManagement(boolean requiresLongTermManagement) {
        this.requiresLongTermManagement = requiresLongTermManagement;
    }
    
    public String getManagementType() {
        return managementType;
    }
    
    public void setManagementType(String managementType) {
        this.managementType = managementType;
    }
    
    // Implementation of abstract methods
    @Override
    public String getDescription() {
        return String.format("Chronic condition requiring long-term management: %s", getName());
    }
    
    @Override
    public String getDietaryRestrictions() {
        return "Long-term dietary modifications recommended for chronic condition management.";
    }
    
    // Method Overriding
    @Override
    public String getFullInfo() {
        return super.getFullInfo() + 
               String.format(", Management: %s, Long-term: %s", 
                           managementType, requiresLongTermManagement);
    }
    
    @Override
    public String toString() {
        return "ChronicDisease{" +
                "name='" + getName() + '\'' +
                ", managementType='" + managementType + '\'' +
                ", requiresLongTermManagement=" + requiresLongTermManagement +
                '}';
    }
}