package com.petadoption.controller;

import com.petadoption.model.*;
import com.petadoption.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/adoptions")
public class AdminAdoptionController {

    private final AdoptionRepository adoptionRepository;
    private final PetRepository petRepository;

    public AdminAdoptionController(AdoptionRepository adoptionRepository, PetRepository petRepository) {
        this.adoptionRepository = adoptionRepository;
        this.petRepository = petRepository;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<Adoption>> getPendingAdoptions() {
        List<Adoption> pendingRequests = adoptionRepository.findByStatus(AdoptionStatus.PENDING);
        if (pendingRequests.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(pendingRequests);
    }


    // ✅ Approve an adoption request
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/approve/{requestId}")
    public ResponseEntity<?> approveAdoption(@PathVariable Long requestId) {
        Optional<Adoption> requestOptional = adoptionRepository.findById(requestId);

        if (requestOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Adoption request not found.");
        }

        Adoption adoptionRequest = requestOptional.get();
        Pet pet = adoptionRequest.getPet();

        // ✅ Ensure the pet is still available before approving
        if (pet.getStatus() != PetStatus.AVAILABLE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pet is no longer available for adoption.");
        }

        // ✅ Mark pet as adopted and approve adoption
        pet.setStatus(PetStatus.ADOPTED);
        adoptionRequest.setStatus(AdoptionStatus.APPROVED);

        petRepository.save(pet);
        adoptionRepository.save(adoptionRequest);

        return ResponseEntity.ok("Adoption approved successfully.");
    }

    // ✅ Reject an adoption request
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectAdoption(@PathVariable Long requestId) {
        Optional<Adoption> requestOptional = adoptionRepository.findById(requestId);

        if (requestOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Adoption request not found.");
        }

        Adoption adoptionRequest = requestOptional.get();

        // ✅ Ensure we don’t reject already approved or rejected requests
        if (adoptionRequest.getStatus() != AdoptionStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This request is already processed.");
        }

        adoptionRequest.setStatus(AdoptionStatus.REJECTED);
        adoptionRepository.save(adoptionRequest);

        return ResponseEntity.ok("Adoption request rejected.");
    }
    
    
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/adoption-list")
    public ResponseEntity<List<Map<String, Object>>> getAllAdoptions() {
        List<Adoption> adoptions = adoptionRepository.findAllAdoptionsWithDetails();
        List<Map<String, Object>> adoptionList = new ArrayList<>();

        for (Adoption adoption : adoptions) {
            Map<String, Object> adoptionData = new HashMap<>();
            adoptionData.put("adopterName", adoption.getUser().getName());
            adoptionData.put("adopterEmail", adoption.getUser().getEmail());
            adoptionData.put("petName", adoption.getPet().getName());
            adoptionData.put("petType", adoption.getPet().getType());
            adoptionData.put("petBreed", adoption.getPet().getBreed());
            adoptionData.put("adoptionStatus", adoption.getStatus().toString());

            adoptionList.add(adoptionData);
        }

        return ResponseEntity.ok(adoptionList);
    }


}
