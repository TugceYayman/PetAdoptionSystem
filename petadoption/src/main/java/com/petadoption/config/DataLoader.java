package com.petadoption.config;

import com.petadoption.model.Pet;
import com.petadoption.model.PetStatus;
import com.petadoption.model.User;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initData(
            PetRepository petRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.password}") String adminPassword
    ) {
        return args -> {
            // Load sample pets (only if no pets exist)
            if (petRepository.count() == 0) {
                List<Pet> pets = List.of(
                    new Pet(null, "Buddy", "Dog", "Golden Retriever", 2, PetStatus.AVAILABLE, "https://example.com/dog1.jpg"),
                    new Pet(null, "Whiskers", "Cat", "Persian", 3, PetStatus.AVAILABLE, "https://example.com/cat1.jpg"),
                    new Pet(null, "Charlie", "Dog", "Labrador", 1, PetStatus.AVAILABLE, "https://example.com/dog2.jpg"),
                    new Pet(null, "Luna", "Cat", "Siamese", 4, PetStatus.AVAILABLE, "https://example.com/cat2.jpg")
                );
                petRepository.saveAll(pets);
                System.out.println("🐾 Sample pets loaded into the database!");
            }

            // Load admin user (only if no admin exists)
            if (userRepository.findByEmail("admin@petadoption.com").isEmpty()) {
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@petadoption.com");
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole("ADMIN");

                userRepository.save(admin);
                System.out.println("✅ Admin user created");
                System.out.println("   Email: admin@petadoption.com");
                System.out.println("   Password: (from application.properties)");
            } else {
                System.out.println("✅ Admin user already exists.");
            }
        };
    }
}
