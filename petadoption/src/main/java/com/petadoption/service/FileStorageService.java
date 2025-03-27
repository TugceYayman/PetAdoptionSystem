package com.petadoption.service;

import com.petadoption.exception.FileStorageException;
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

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png");

    public FileStorageService() {
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
    }

    private boolean isValidFileExtension(String filename) {
        return ALLOWED_EXTENSIONS.stream().anyMatch(filename.toLowerCase()::endsWith);
    }

    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty!");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File must have a name!");
        }

        if (!isValidFileExtension(originalFilename)) {
            throw new IllegalArgumentException("Invalid file type! Only JPG, JPEG, PNG are allowed.");
        }

        try {
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = Paths.get(uploadDir, uniqueFilename);
            saveFile(file, filePath);
            return "/uploads/" + uniqueFilename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + e.getMessage(), e);
        }
    }

    protected void saveFile(MultipartFile file, Path filePath) throws IOException {
        Files.copy(file.getInputStream(), filePath);
    }
}