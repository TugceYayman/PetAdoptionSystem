package com.petadoption.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.petadoption.model.Adoption;
import com.petadoption.model.Pet;
import com.petadoption.model.PetStatus;
import com.petadoption.model.User;
import com.petadoption.repository.AdoptionRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;

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
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Pet pet = petRepository.findById(petId).orElseThrow(() -> new RuntimeException("Pet not found"));

        if (pet.getStatus() == PetStatus.ADOPTED) {
            throw new RuntimeException("Pet already adopted");
        }

        Adoption adoption = new Adoption();
        adoption.setUser(user);
        adoption.setPet(pet);
        adoption.setStatus("PENDING");

        return adoptionRepository.save(adoption);
    }

    @Override
    public List<Adoption> getUserAdoptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        return adoptionRepository.findByUser(user);
    }
}
