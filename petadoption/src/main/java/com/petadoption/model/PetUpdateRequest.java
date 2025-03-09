package com.petadoption.model;

import org.springframework.web.multipart.MultipartFile;

public class PetUpdateRequest {
    private String name;
    private String type;
    private String breed;
    private int age;
    private String status;
    private MultipartFile image;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }
}

