package com.petadoption.controller;

import com.petadoption.model.Pet;
import com.petadoption.model.PetStatus;
import com.petadoption.repository.AdoptionRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.service.FileStorageService;

import jakarta.transaction.Transactional;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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
    public List<Pet> getAllPets() {
        return petRepository.findAll();
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
        Link selfLink = linkTo(methodOn(PetController.class).getPetById(id)).withSelfRel();
        Link allPetsLink = linkTo(methodOn(PetController.class).getAllPets()).withRel("all-pets");
        
        Link updatePetLink = linkTo(methodOn(PetController.class)
                .updatePet(id, pet.getName(), pet.getType(), pet.getBreed(), pet.getAge(), pet.getStatus().toString(), null))
                .withRel("update-pet");

        Link deletePetLink = linkTo(methodOn(PetController.class).deletePet(id)).withRel("delete-pet");

        petResource.add(selfLink, allPetsLink, updatePetLink, deletePetLink);

        return ResponseEntity.ok(petResource);
    }

    // ✅ Add a new pet
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> addPet(
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("breed") String breed,
            @RequestParam("age") int age,
            @RequestParam("status") String status,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        Pet pet = new Pet();
        pet.setName(name);
        pet.setType(type);
        pet.setBreed(breed);
        pet.setAge(age);
        pet.setStatus(PetStatus.valueOf(status));

        // ✅ Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = fileStorageService.uploadFile(imageFile);
                pet.setImageUrl(imageUrl);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload image: " + e.getMessage());
            }
        }

        petRepository.save(pet);
        return ResponseEntity.ok("Pet added successfully!");
    }

    // ✅ Update pet details
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePet(
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

        Pet pet = optionalPet.get();
        pet.setName(name);
        pet.setType(type);
        pet.setBreed(breed);
        pet.setAge(age);
        pet.setStatus(PetStatus.valueOf(status));

        // ✅ Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = fileStorageService.uploadFile(imageFile);
                pet.setImageUrl(imageUrl);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload image: " + e.getMessage());
            }
        }

        petRepository.save(pet);
        return ResponseEntity.ok("Pet updated successfully!");
    }

    // ✅ Safe pet deletion (removes related adoptions first)
    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePet(@PathVariable Long id) {
        if (!petRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pet not found.");
        }

        // ✅ Step 1: Delete related adoption records first
        adoptionRepository.deleteByPetId(id);

        // ✅ Step 2: Delete the pet itself
        petRepository.deleteById(id);

        return ResponseEntity.ok("✅ Pet deleted successfully!");
    }
}
