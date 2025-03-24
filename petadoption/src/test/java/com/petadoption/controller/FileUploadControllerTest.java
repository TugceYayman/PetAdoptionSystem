package com.petadoption.controller;
import org.springframework.http.HttpStatus;

import com.petadoption.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private FileUploadController fileUploadController;

    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "file", 
                "test-image.jpg", 
                "image/jpeg", 
                new byte[]{1, 2, 3, 4}
        );
    }

    @Test
    void testUploadFile_Success() throws IOException {
        String fileUrl = "http://localhost/uploads/test-image.jpg";
        when(fileStorageService.uploadFile(mockFile)).thenReturn(fileUrl);

        ResponseEntity<Map<String, String>> response = fileUploadController.uploadFile(mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(fileUrl, response.getBody().get("url"));

        verify(fileStorageService, times(1)).uploadFile(mockFile);
    }


}
