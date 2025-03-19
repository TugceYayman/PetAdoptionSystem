package com.petadoption.repository;

import com.petadoption.model.Adoption;
import com.petadoption.model.AdoptionStatus;
import com.petadoption.model.Pet;
import com.petadoption.model.User;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdoptionRepository extends JpaRepository<Adoption, Long> {
    
    
    // Find an adoption request for a specific pet by a specific user
    Optional<Adoption> findByUserAndPet(User user, Pet pet);


    // Check if a user has already requested to adopt a specific pet
    boolean existsByUserAndPet(User user, Pet pet);
    
    List<Adoption> findByUser(User user);
    
    List<Adoption> findByStatus(AdoptionStatus status); 
    
    //Get all pending adoption requests for a specific user
    List<Adoption> findByUserAndStatus(User user, AdoptionStatus status);
    
    boolean existsByUserAndPetAndStatusIn(User user, Pet pet, List<AdoptionStatus> statuses);

    
    @Modifying
    @Transactional
    @Query("DELETE FROM Adoption a WHERE a.pet.id = :petId")
    void deleteByPetId(@Param("petId") Long petId);

    
    Optional<Adoption> findByUserAndPetAndStatus(User user, Pet pet, AdoptionStatus status);
    
    @Query("SELECT a FROM Adoption a WHERE a.id IN " +
    	       "(SELECT MAX(a2.id) FROM Adoption a2 GROUP BY a2.pet)")
    	List<Adoption> findLatestAdoptionRecords();






}
