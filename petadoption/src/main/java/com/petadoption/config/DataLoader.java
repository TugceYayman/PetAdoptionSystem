package com.petadoption.config;

import com.petadoption.model.Pet;
import com.petadoption.model.PetStatus;
import com.petadoption.model.User;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private static final String SPECIES_RABBIT = "Rabbit";
    private static final String SPECIES_CAT = "Cat";
    private static final String SPECIES_DOG = "Dog";

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
                        new Pet(null, "Buddy", SPECIES_DOG, "Golden Retriever", 2, PetStatus.AVAILABLE, "/uploads/dog1.jpg"),
                        new Pet(null, "Max", SPECIES_DOG, "Labrador Retriever", 3, PetStatus.AVAILABLE, "/uploads/dog2.jpg"),
                        new Pet(null, "Bella", SPECIES_DOG, "Beagle", 4, PetStatus.AVAILABLE, "/uploads/dog3.jpg"),
                        new Pet(null, "Rocky", SPECIES_DOG, "Bulldog", 1, PetStatus.AVAILABLE, "/uploads/dog4.jpg"),
                        new Pet(null, "Charlie", SPECIES_DOG, "Poodle", 5, PetStatus.AVAILABLE, "/uploads/dog5.jpg"),

                        new Pet(null, "Whiskers", SPECIES_CAT, "Persian", 3, PetStatus.AVAILABLE, "/uploads/cat1.jpg"),
                        new Pet(null, "Mittens", SPECIES_CAT, "Siamese", 2, PetStatus.AVAILABLE, "/uploads/cat2.jpg"),
                        new Pet(null, "Shadow", SPECIES_CAT, "Maine Coon", 4, PetStatus.AVAILABLE, "/uploads/cat3.jpg"),
                        new Pet(null, "Luna", SPECIES_CAT, "Bengal", 1, PetStatus.AVAILABLE, "/uploads/cat4.jpg"),
                        new Pet(null, "Simba", SPECIES_CAT, "Scottish Fold", 3, PetStatus.AVAILABLE, "/uploads/cat5.jpg"),

                        new Pet(null, "Thumper", SPECIES_RABBIT, "Holland Lop", 2, PetStatus.AVAILABLE, "/uploads/rabbit1.jpg"),
                        new Pet(null, "Coco", SPECIES_RABBIT, "Netherland Dwarf", 1, PetStatus.AVAILABLE, "/uploads/rabbit2.jpg"),
                        new Pet(null, "Snowball", SPECIES_RABBIT, "Lionhead", 3, PetStatus.AVAILABLE, "/uploads/rabbit3.jpg")
                );

                petRepository.saveAll(pets);
                logger.info("🐾 Sample pets loaded into the database!");
            }

            if (userRepository.findByEmail("admin@petadoption.com").isEmpty()) {
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@petadoption.com");
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole("ADMIN");

                userRepository.save(admin);
                logger.info("✅ Admin user created");
                logger.info("   Email: admin@petadoption.com");
            } else {
                logger.info("✅ Admin user already exists.");
            }
        };
    }
}
