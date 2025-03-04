package com.petadoption.repository;

import com.petadoption.model.Adoption;
import com.petadoption.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdoptionRepository extends JpaRepository<Adoption, Long> {
	List<Adoption> findByUser(User user);

}
