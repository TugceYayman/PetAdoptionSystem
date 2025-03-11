package com.petadoption.controller;

import com.petadoption.model.Pet;
import com.petadoption.model.PetStatus;
import com.petadoption.repository.AdoptionRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.service.FileStorageService;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private static final Logger logger = LoggerFactory.getLogger(PetController.class);

    private final FileStorageService fileStorageService;
    private final PetRepository petRepository;
    private final AdoptionRepository adoptionRepository;

    public PetController(FileStorageService fileStorageService, PetRepository petRepository, AdoptionRepository adoptionRepository) {
        this.fileStorageService = fileStorageService;
        this.petRepository = petRepository;
        this.adoptionRepository = adoptionRepository;
    }

    // ✅ Fetch all pets
    @GetMapping
    public ResponseEntity<List<Pet>> getAllPets() {
        List<Pet> pets = petRepository.findAll();
        return ResponseEntity.ok(pets);
    }

    // ✅ Get a single pet with HATEOAS links
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Pet>> getPetById(@PathVariable Long id) {
        Optional<Pet> optionalPet = petRepository.findById(id);

        if (optionalPet.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Pet pet = optionalPet.get();
        EntityModel<Pet> petResource = EntityModel.of(pet);

        // ✅ Add HATEOAS links
        petResource.add(linkTo(methodOn(PetController.class).getPetById(id)).withSelfRel());
        petResource.add(linkTo(methodOn(PetController.class).getAllPets()).withRel("all-pets"));
        petResource.add(linkTo(methodOn(PetController.class)
                .updatePet(id, pet.getName(), pet.getType(), pet.getBreed(), pet.getAge(), pet.getStatus().toString(), null))
                .withRel("update-pet"));
        petResource.add(linkTo(methodOn(PetController.class).deletePet(id)).withRel("delete-pet"));

        return ResponseEntity.ok(petResource);
    }

    // ✅ Add a new pet
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> addPet(
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("breed") String breed,
            @RequestParam("age") int age,
            @RequestParam("status") String status,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        try {
            Pet pet = new Pet();
            pet.setName(name);
            pet.setType(type);
            pet.setBreed(breed);
            pet.setAge(age);
            pet.setStatus(PetStatus.valueOf(status.toUpperCase()));

            // ✅ Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = fileStorageService.uploadFile(imageFile);
                pet.setImageUrl(imageUrl);
            }

            petRepository.save(pet);
            return ResponseEntity.ok("Pet added successfully!");
        } catch (Exception e) {
            logger.error("Error adding pet: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add pet: " + e.getMessage());
        }
    }

    // ✅ Update pet details
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updatePet(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("breed") String breed,
            @RequestParam("age") int age,
            @RequestParam("status") String status,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        Optional<Pet> optionalPet = petRepository.findById(id);
        if (optionalPet.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pet not found.");
        }

        try {
            Pet pet = optionalPet.get();
            pet.setName(name);
            pet.setType(type);
            pet.setBreed(breed);
            pet.setAge(age);
            pet.setStatus(PetStatus.valueOf(status.toUpperCase()));

            // ✅ Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = fileStorageService.uploadFile(imageFile);
                pet.setImageUrl(imageUrl);
            }

            petRepository.save(pet);
            return ResponseEntity.ok("Pet updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating pet: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update pet: " + e.getMessage());
        }
    }

    // ✅ Safe pet deletion (removes related adoptions first)
    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePet(@PathVariable Long id) {
        if (!petRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pet not found.");
        }

        try {
            // ✅ Step 1: Delete related adoption records first
            adoptionRepository.deleteByPetId(id);

            // ✅ Step 2: Delete the pet itself
            petRepository.deleteById(id);
            return ResponseEntity.ok("✅ Pet deleted successfully!");
        } catch (Exception e) {
            logger.error("Error deleting pet: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete pet: " + e.getMessage());
        }
    }
}
