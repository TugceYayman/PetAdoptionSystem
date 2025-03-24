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
            if (petRepository.count() == 0) {
            	List<Pet> pets = List.of(
            		    new Pet(null, "Buddy", "Dog", "Golden Retriever", 2, PetStatus.AVAILABLE, "/uploads/dog1.jpg"),
            		    new Pet(null, "Max", "Dog", "Labrador Retriever", 3, PetStatus.AVAILABLE, "/uploads/dog2.jpg"),
            		    new Pet(null, "Bella", "Dog", "Beagle", 4, PetStatus.AVAILABLE, "/uploads/dog3.jpg"),
            		    new Pet(null, "Rocky", "Dog", "Bulldog", 1, PetStatus.AVAILABLE, "/uploads/dog4.jpg"),
            		    new Pet(null, "Charlie", "Dog", "Poodle", 5, PetStatus.AVAILABLE, "/uploads/dog5.jpg"),

            		    new Pet(null, "Whiskers", "Cat", "Persian", 3, PetStatus.AVAILABLE, "/uploads/cat1.jpg"),
            		    new Pet(null, "Mittens", "Cat", "Siamese", 2, PetStatus.AVAILABLE, "/uploads/cat2.jpg"),
            		    new Pet(null, "Shadow", "Cat", "Maine Coon", 4, PetStatus.AVAILABLE, "/uploads/cat3.jpg"),
            		    new Pet(null, "Luna", "Cat", "Bengal", 1, PetStatus.AVAILABLE, "/uploads/cat4.jpg"),
            		    new Pet(null, "Simba", "Cat", "Scottish Fold", 3, PetStatus.AVAILABLE, "/uploads/cat5.jpg"),

            		    new Pet(null, "Thumper", "Rabbit", "Holland Lop", 2, PetStatus.AVAILABLE, "/uploads/rabbit1.jpg"),
            		    new Pet(null, "Coco", "Rabbit", "Netherland Dwarf", 1, PetStatus.AVAILABLE, "/uploads/rabbit2.jpg"),
            		    new Pet(null, "Snowball", "Rabbit", "Lionhead", 3, PetStatus.AVAILABLE, "/uploads/rabbit3.jpg")
            		 
            		);

                petRepository.saveAll(pets);
                System.out.println("🐾 Sample pets loaded into the database!");
            }

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
