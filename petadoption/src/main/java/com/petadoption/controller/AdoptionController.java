package com.petadoption.controller;

import com.petadoption.model.*;
import com.petadoption.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/adoptions")
public class AdoptionController {

    private final AdoptionRepository adoptionRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;

    private static final String USER_NOT_FOUND = "User not found.";
    private static final String PET_NOT_FOUND = "Pet not found.";
    private static final String PET_ALREADY_ADOPTED = "Pet is already adopted.";
    private static final String ADOPTION_EXISTS = "You have already requested adoption for this pet.";
    private static final String ADOPTION_REQUEST_SUCCESS = "Adoption request sent successfully.";
    private static final String ADOPTION_NOT_FOUND = "Adoption record not found or pet is not adopted.";
    private static final String PET_NOT_ADOPTED = "Pet is not currently adopted.";
    private static final String UNADOPT_SUCCESS = "Pet successfully un-adopted.";

    public AdoptionController(AdoptionRepository adoptionRepository, PetRepository petRepository, UserRepository userRepository) {
        this.adoptionRepository = adoptionRepository;
        this.petRepository = petRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/request/{petId}")
    public ResponseEntity<String> requestAdoption(@PathVariable Long petId, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PET_NOT_FOUND));

        if (pet.getStatus() != PetStatus.AVAILABLE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PET_ALREADY_ADOPTED);
        }

        List<AdoptionStatus> blockedStatuses = List.of(AdoptionStatus.PENDING, AdoptionStatus.APPROVED);
        if (adoptionRepository.existsByUserAndPetAndStatusIn(user, pet, blockedStatuses)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ADOPTION_EXISTS);
        }

        adoptionRepository.save(new Adoption(user, pet, AdoptionStatus.PENDING));
        return ResponseEntity.ok(ADOPTION_REQUEST_SUCCESS);
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/my-pets")
    public ResponseEntity<List<Pet>> getMyAdoptedPets(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        List<Pet> adoptedPets = adoptionRepository.findByUserAndStatus(user, AdoptionStatus.APPROVED)
                .stream().map(Adoption::getPet).toList();

        return ResponseEntity.ok(adoptedPets);
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/my-requests")
    public ResponseEntity<List<Adoption>> getMyAdoptionRequests(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        return ResponseEntity.ok(adoptionRepository.findByUser(user));
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/pending-requests")
    public ResponseEntity<List<Adoption>> getPendingRequests(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        return ResponseEntity.ok(adoptionRepository.findByUserAndStatus(user, AdoptionStatus.PENDING));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/unadopt/{petId}")
    public ResponseEntity<String> unadoptPet(@PathVariable Long petId, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PET_NOT_FOUND));

        Optional<Adoption> adoptionOptional = adoptionRepository.findByUserAndPetAndStatus(user, pet, AdoptionStatus.APPROVED);

        if (adoptionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ADOPTION_NOT_FOUND);
        }

        if (pet.getStatus() != PetStatus.ADOPTED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PET_NOT_ADOPTED);
        }

        pet.setStatus(PetStatus.AVAILABLE);
        petRepository.save(pet);
        adoptionRepository.delete(adoptionOptional.get());

        return ResponseEntity.ok(UNADOPT_SUCCESS);
    }
}
