package com.petadoption.controller;

import com.petadoption.model.Pet;
import com.petadoption.repository.PetRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetRepository petRepository;

    public PetController(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @GetMapping
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public Pet addPet(@RequestBody Pet pet) {
        return petRepository.save(pet);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public Pet updatePet(@PathVariable Long id, @RequestBody Pet petDetails) {
        return petRepository.findById(id).map(pet -> {
            pet.setName(petDetails.getName());
            pet.setType(petDetails.getType());
            pet.setBreed(petDetails.getBreed());
            pet.setAge(petDetails.getAge());
            pet.setImageUrl(petDetails.getImageUrl());
            return petRepository.save(pet);
        }).orElseThrow(() -> new RuntimeException("Pet not found"));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletePet(@PathVariable Long id) {
        petRepository.deleteById(id);
    }
}
