package com.petadoption.service;

import java.util.List;

import com.petadoption.model.Adoption;

public interface AdoptionService {
	  Adoption requestAdoption(Long petId, String userEmail);  // Single adoption request
	    List<Adoption> getUserAdoptions(String userEmail);       
}
