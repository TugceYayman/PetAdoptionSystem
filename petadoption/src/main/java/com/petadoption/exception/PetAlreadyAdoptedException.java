package com.petadoption.exception;

public class PetAlreadyAdoptedException extends RuntimeException {
    public PetAlreadyAdoptedException(String message) {
        super(message);
    }
}
