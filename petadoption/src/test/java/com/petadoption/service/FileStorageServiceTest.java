package com.petadoption.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;


    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
    }

    @Test
    void testUploadFile_Success() {
        // Mock a valid image file
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", 
                "image.jpg", 
                "image/jpeg", 
                "sample image content".getBytes()
        );

        String storedFilePath = fileStorageService.uploadFile(mockFile);

        assertNotNull(storedFilePath);
        assertTrue(storedFilePath.contains("/uploads/"));
    }

    @Test
    void testUploadFile_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", 
                "empty.jpg", 
                "image/jpeg", 
                new byte[0]
        );

        // Expect an IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> fileStorageService.uploadFile(emptyFile));
        assertEquals("File is empty!", exception.getMessage());
    }

    @Test
    void testUploadFile_InvalidFileType() {
        // Mock an invalid file type
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", 
                "document.pdf", 
                "application/pdf", 
                "pdf content".getBytes()
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> fileStorageService.uploadFile(invalidFile));
        assertEquals("Invalid file type! Only JPG, JPEG, PNG are allowed.", exception.getMessage());
    }

    @Test
    void testUploadFile_IOExceptionHandling() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", 
                "image.jpg", 
                "image/jpeg", 
                "sample image content".getBytes()
        );

        FileStorageService spyService = Mockito.spy(fileStorageService);
        Mockito.doThrow(new IOException("Simulated IOException"))
               .when(spyService)
               .saveFile(Mockito.<MultipartFile>any(), Mockito.<Path>any());

        Exception exception = assertThrows(RuntimeException.class, () -> spyService.uploadFile(mockFile));
        assertTrue(exception.getMessage().contains("Failed to store file"));
    }

}
