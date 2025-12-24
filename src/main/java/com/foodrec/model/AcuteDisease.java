package com.foodrec.model;

/**
 * Acute Disease class
 * Demonstrates: Inheritance, Method Overriding
 */
public class AcuteDisease extends Disease {
    private int expectedRecoveryDays;
    private boolean requiresImmediateCare;
    
    public AcuteDisease(String name) {
        super(name, "Acute");
        this.expectedRecoveryDays = 7; // default
        this.requiresImmediateCare = false;
    }
    
    public AcuteDisease(String name, int expectedRecoveryDays) {
        super(name, "Acute");
        this.expectedRecoveryDays = expectedRecoveryDays;
        this.requiresImmediateCare = false;
    }
    
    // Getters and Setters
    public int getExpectedRecoveryDays() {
        return expectedRecoveryDays;
    }
    
    public void setExpectedRecoveryDays(int expectedRecoveryDays) {
        this.expectedRecoveryDays = expectedRecoveryDays;
    }
    
    public boolean isRequiresImmediateCare() {
        return requiresImmediateCare;
    }
    
    public void setRequiresImmediateCare(boolean requiresImmediateCare) {
        this.requiresImmediateCare = requiresImmediateCare;
    }
    
    // Implementation of abstract methods
    @Override
    public String getDescription() {
        return String.format("Acute condition with expected recovery in %d days: %s", 
                           expectedRecoveryDays, getName());
    }
    
    @Override
    public String getDietaryRestrictions() {
        return "Temporary dietary modifications during recovery period.";
    }
    
    // Method Overriding
    @Override
    public String getFullInfo() {
        return super.getFullInfo() + 
               String.format(", Recovery Days: %d, Immediate Care: %s", 
                           expectedRecoveryDays, requiresImmediateCare);
    }
    
    @Override
    public String toString() {
        return "AcuteDisease{" +
                "name='" + getName() + '\'' +
                ", expectedRecoveryDays=" + expectedRecoveryDays +
                ", requiresImmediateCare=" + requiresImmediateCare +
                '}';
    }
}