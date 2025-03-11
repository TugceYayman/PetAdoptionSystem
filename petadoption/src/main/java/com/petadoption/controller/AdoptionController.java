package com.petadoption.controller;

import com.petadoption.model.*;
import com.petadoption.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/adoptions")
public class AdoptionController {

    private final AdoptionRepository adoptionRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public AdoptionController(AdoptionRepository adoptionRepository, PetRepository petRepository, UserRepository userRepository) {
        this.adoptionRepository = adoptionRepository;
        this.petRepository = petRepository;
        this.userRepository = userRepository;
    }

    // ✅ Allow users to request adoption for a pet
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/request/{petId}")
    public ResponseEntity<?> requestAdoption(@PathVariable Long petId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        Optional<Pet> petOptional = petRepository.findById(petId);

        if (userOptional.isEmpty() || petOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or Pet not found.");
        }

        User user = userOptional.get();
        Pet pet = petOptional.get();

        if (pet.getStatus() != PetStatus.AVAILABLE) { // ✅ Only allow requests for available pets
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pet is already adopted.");
        }

        // ✅ Prevent duplicate requests only if the previous one was not rejected or unadopted
        List<AdoptionStatus> blockedStatuses = List.of(AdoptionStatus.PENDING, AdoptionStatus.APPROVED);
        if (adoptionRepository.existsByUserAndPetAndStatusIn(user, pet, blockedStatuses)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have already requested adoption for this pet.");
        }

        // ✅ Create a new adoption request
        Adoption adoptionRequest = new Adoption(user, pet, AdoptionStatus.PENDING);
        adoptionRepository.save(adoptionRequest);

        return ResponseEntity.ok("Adoption request sent successfully.");
    }

    
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/my-pets")
    public ResponseEntity<List<Pet>> getMyAdoptedPets(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Adoption> approvedAdoptions = adoptionRepository.findByUserAndStatus(user, AdoptionStatus.APPROVED);

        List<Pet> adoptedPets = approvedAdoptions.stream()
                .map(Adoption::getPet)
                .toList();

        System.out.println("✅ Fetching My Pets for: " + userEmail);
        System.out.println("✅ Adopted Pets Found: " + adoptedPets.size());

        return ResponseEntity.ok(adoptedPets);
    }


    // ✅ Get a user's adoption requests
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyAdoptionRequests(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(adoptionRepository.findByUser(user));
    }

    // ✅ Get all pending adoption requests for the logged-in user
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/pending-requests")
    public ResponseEntity<List<Adoption>> getPendingRequests(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Adoption> pendingRequests = adoptionRepository.findByUserAndStatus(user, AdoptionStatus.PENDING);
        return ResponseEntity.ok(pendingRequests);
    }
    
    
    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/unadopt/{petId}")
    public ResponseEntity<String> unadoptPet(@PathVariable Long petId, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        Optional<Adoption> adoptionOptional = adoptionRepository.findByUserAndPetAndStatus(user, pet, AdoptionStatus.APPROVED);

        if (adoptionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Adoption record not found or pet is not adopted.");
        }

        Adoption adoption = adoptionOptional.get();

        // ✅ Ensure the pet is actually adopted before un-adopting
        if (pet.getStatus() != PetStatus.ADOPTED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pet is not currently adopted.");
        }

        // ✅ Mark pet as available again
        pet.setStatus(PetStatus.AVAILABLE);
        petRepository.save(pet);

        // ✅ Remove adoption record
        adoptionRepository.delete(adoption);

        return ResponseEntity.ok("Pet successfully un-adopted.");
    }

    
    
}
