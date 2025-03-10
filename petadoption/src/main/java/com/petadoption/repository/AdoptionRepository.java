package com.petadoption.repository;

import com.petadoption.model.Adoption;
import com.petadoption.model.AdoptionStatus;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdoptionRepository extends JpaRepository<Adoption, Long> {
    
    // ✅ Get all adoption requests for a specific user
    
    // ✅ Find an adoption request for a specific pet by a specific user
    Optional<Adoption> findByUserAndPet(User user, Pet pet);

    // ✅ Get all pending adoption requests for a specific user
    List<Adoption> findByUserAndStatus(User user, AdoptionStatus status);

    // ✅ Check if a user has already requested to adopt a specific pet
    boolean existsByUserAndPet(User user, Pet pet);
    
    List<Adoption> findByUser(User user);
    
    List<Adoption> findByStatus(AdoptionStatus status); // 🔴 This was missing!


}
