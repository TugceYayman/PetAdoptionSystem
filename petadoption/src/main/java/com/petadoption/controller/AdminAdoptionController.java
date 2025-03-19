package com.petadoption.controller;

import com.petadoption.model.*;
import com.petadoption.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/adoptions")
public class AdminAdoptionController {

    private final AdoptionRepository adoptionRepository;
    private final PetRepository petRepository;

    private static final String REQUEST_NOT_FOUND = "Adoption request not found.";
    private static final String PET_NOT_AVAILABLE = "Pet is no longer available for adoption.";
    private static final String REQUEST_ALREADY_PROCESSED = "This request is already processed.";
    private static final String APPROVAL_SUCCESS = "Adoption approved successfully.";
    private static final String REJECTION_SUCCESS = "Adoption request rejected.";

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

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/approve/{requestId}")
    public ResponseEntity<String> approveAdoption(@PathVariable Long requestId) {
        return adoptionRepository.findById(requestId)
                .map(adoptionRequest -> {
                    Pet pet = adoptionRequest.getPet();

                    if (pet.getStatus() != PetStatus.AVAILABLE) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PET_NOT_AVAILABLE);
                    }

                    pet.setStatus(PetStatus.ADOPTED);
                    adoptionRequest.setStatus(AdoptionStatus.APPROVED);
                    petRepository.save(pet);
                    adoptionRepository.save(adoptionRequest);

                    return ResponseEntity.ok(APPROVAL_SUCCESS);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(REQUEST_NOT_FOUND));
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/reject/{requestId}")
    public ResponseEntity<String> rejectAdoption(@PathVariable Long requestId) {
        return adoptionRepository.findById(requestId)
                .map(adoptionRequest -> {
                    if (adoptionRequest.getStatus() != AdoptionStatus.PENDING) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(REQUEST_ALREADY_PROCESSED);
                    }

                    adoptionRequest.setStatus(AdoptionStatus.REJECTED);
                    adoptionRepository.save(adoptionRequest);

                    return ResponseEntity.ok(REJECTION_SUCCESS);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(REQUEST_NOT_FOUND));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/adoption-list")
    public ResponseEntity<List<Map<String, Object>>> getAllAdoptions() {
        List<Adoption> adoptions = adoptionRepository.findLatestAdoptionRecords();
        List<Map<String, Object>> adoptionList = new ArrayList<>();

        for (Adoption adoption : adoptions) {
            Map<String, Object> adoptionData = new HashMap<>();
            adoptionData.put("adopterName", adoption.getUser().getName());
            adoptionData.put("adopterEmail", adoption.getUser().getEmail());
            adoptionData.put("petName", adoption.getPet().getName());
            adoptionData.put("petType", adoption.getPet().getType());
            adoptionData.put("petBreed", Optional.ofNullable(adoption.getPet().getBreed()).orElse("Unknown"));
            adoptionData.put("adoptionStatus", adoption.getStatus().toString());

            adoptionList.add(adoptionData);
        }

        return ResponseEntity.ok(adoptionList);
    }
}
