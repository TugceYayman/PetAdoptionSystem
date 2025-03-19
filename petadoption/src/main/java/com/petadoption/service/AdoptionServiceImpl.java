package com.petadoption.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.petadoption.model.*;
import com.petadoption.repository.*;

@Service
public class AdoptionServiceImpl implements AdoptionService {

    private final AdoptionRepository adoptionRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public AdoptionServiceImpl(AdoptionRepository adoptionRepository, PetRepository petRepository, UserRepository userRepository) {
        this.adoptionRepository = adoptionRepository;
        this.petRepository = petRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Adoption requestAdoption(Long petId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new RuntimeException("Pet not found"));

        if (pet.getStatus() == PetStatus.ADOPTED) {
            throw new RuntimeException("Pet is already adopted");
        }

        if (adoptionRepository.existsByUserAndPet(user, pet)) {
            throw new RuntimeException("You have already requested adoption for this pet.");
        }

        Adoption adoption = new Adoption(user, pet, AdoptionStatus.PENDING);
        return adoptionRepository.save(adoption);
    }

    @Override
    public List<Adoption> getUserAdoptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return adoptionRepository.findByUser(user);
    }
}
