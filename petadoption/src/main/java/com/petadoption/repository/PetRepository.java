package com.petadoption.repository;

import com.petadoption.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    // JpaRepository provides basic CRUD operations automatically
}
