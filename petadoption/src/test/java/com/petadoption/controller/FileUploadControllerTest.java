package com.petadoption.controller;

import com.linecorp.armeria.common.HttpStatus;
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

    // ✅ Test: Successful File Upload
    @Test
    void testUploadFile_Success() throws IOException {
        String fileUrl = "http://localhost/uploads/test-image.jpg";
        when(fileStorageService.uploadFile(mockFile)).thenReturn(fileUrl);

        ResponseEntity<Map<String, String>> response = fileUploadController.uploadFile(mockFile);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(fileUrl, response.getBody().get("url"));

        verify(fileStorageService, times(1)).uploadFile(mockFile);
    }

    // ❌ Test: File Upload Failure due to IOException
    @Test
    void testUploadFile_IOException() throws IOException {
        when(fileStorageService.uploadFile(mockFile)).thenThrow(new IOException("File upload failed"));

        ResponseEntity<Map<String, String>> response = fileUploadController.uploadFile(mockFile);

        // ✅ Check that the response returns INTERNAL_SERVER_ERROR (500)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("File upload failed", response.getBody().get("error"));

        verify(fileStorageService, times(1)).uploadFile(mockFile);
    }

}
