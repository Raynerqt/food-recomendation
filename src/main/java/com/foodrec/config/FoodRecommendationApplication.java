package com.foodrec.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application Class
 * Spring Boot Application Entry Point
 */
@SpringBootApplication
public class FoodRecommendationApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FoodRecommendationApplication.class, args);
        System.out.println("===========================================");
        System.out.println("Food Recommendation API is running!");
        System.out.println("Access at: http://localhost:8080");
        System.out.println("API Health: http://localhost:8080/api/health");
        System.out.println("===========================================");
    }
}