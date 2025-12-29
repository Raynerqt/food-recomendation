package com.foodrec.config;

import com.foodrec.entity.DoctorEntity;
import com.foodrec.repository.DoctorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDoctors(DoctorRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new DoctorEntity("Dr. Sarah Johnson", "Nutritionist", "Siloam Hospital", "+628123456789", "Jakarta Selatan", "https://cdn-icons-png.flaticon.com/512/3304/3304567.png"));
                repository.save(new DoctorEntity("Dr. Budi Santoso", "Gastroenterologist", "RS Cipto Mangunkusumo", "+628198765432", "Jakarta Pusat", "https://cdn-icons-png.flaticon.com/512/3774/3774299.png"));
                repository.save(new DoctorEntity("Dr. Linda Wijaya", "General Practitioner", "Klinik Sehat", "+628122334455", "Bandung", "https://cdn-icons-png.flaticon.com/512/607/607414.png"));
                repository.save(new DoctorEntity("Dr. Kevin Lim", "Nutritionist", "Bali Royal Hospital", "+628133445566", "Denpasar", "https://cdn-icons-png.flaticon.com/512/4825/4825038.png"));
                System.out.println("✅ Dummy Doctors Loaded!");
            }
        };
    }
}