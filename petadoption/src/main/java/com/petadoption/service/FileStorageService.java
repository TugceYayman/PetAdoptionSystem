package com.petadoption.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    // ✅ Set absolute upload path to ensure files are stored correctly
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    public FileStorageService() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs(); // ✅ Ensure upload directory exists
        }
    }

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png");

    private boolean isValidFileExtension(String filename) {
        return ALLOWED_EXTENSIONS.stream().anyMatch(filename.toLowerCase()::endsWith);
    }

    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty!");
        }

        String originalFilename = file.getOriginalFilename();
        if (!isValidFileExtension(originalFilename)) {
            throw new IllegalArgumentException("Invalid file type! Only JPG, JPEG, PNG are allowed.");
        }

        try {
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = Paths.get(UPLOAD_DIR, uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            return "/uploads/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    
    
    
}
