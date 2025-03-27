package com.petadoption.service;

import com.petadoption.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
    }

    @Test
    void shouldUploadValidImageFileSuccessfully() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        String filePath = fileStorageService.uploadFile(file);
        assertNotNull(filePath);
        assertTrue(filePath.contains("/uploads/"));
    }

    @Test
    void shouldThrowException_WhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.uploadFile(file);
        });

        assertEquals("File is empty!", exception.getMessage());
    }


    @Test
    void shouldThrowException_WhenFileExtensionIsNotAllowed() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "not-an-image.txt",
                "text/plain",
                "hello".getBytes()
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.uploadFile(file);
        });

        assertEquals("Invalid file type! Only JPG, JPEG, PNG are allowed.", exception.getMessage());
    }

    @Test
    void shouldThrowFileStorageException_WhenIOExceptionOccurs() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                "image/jpeg",
                "data".getBytes()
        );

        FileStorageService spyService = spy(fileStorageService);

        doThrow(new IOException("Simulated failure"))
                .when(spyService).saveFile(any(MultipartFile.class), any(Path.class));

        FileStorageException exception = assertThrows(FileStorageException.class, () -> {
            spyService.uploadFile(file);
        });

        assertTrue(exception.getMessage().contains("Failed to store file"));
        assertNotNull(exception.getCause());
        assertEquals("Simulated failure", exception.getCause().getMessage());
    }
}
