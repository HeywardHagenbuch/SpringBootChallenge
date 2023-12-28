package com.hln.challenge.controller;

import com.hln.challenge.persistence.models.Wood;
import com.hln.challenge.service.WoodService;
import com.hln.challenge.service.dto.Bundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class WoodControllerTest {

    @Mock
    private WoodService woodService;

    @InjectMocks
    private WoodController woodController;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }
    @Test
    void testUploadFile() {
        String type = "OAK";
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "content".getBytes());

        List<Wood> mockResponse = new ArrayList<>();
        when(woodService.processFileUpload(type, file)).thenReturn(mockResponse);

        ResponseEntity<?> response = woodController.uploadFile(type, file);

        assertEquals(ResponseEntity.ok(mockResponse), response);
        verify(woodService).processFileUpload(type, file);
    }

    @Test
    void testGetBundles() {
        String type = "OAK";
        Double minPrice = 10.0;
        Double maxPrice = 50.0;

        List<Bundle> mockResponse = new ArrayList<>();
        when(woodService.getBundles(type, minPrice, maxPrice)).thenReturn(mockResponse);

        ResponseEntity<?> response = woodController.getBundles(type, minPrice, maxPrice);

        assertEquals(ResponseEntity.ok(mockResponse), response);
        verify(woodService).getBundles(type, minPrice, maxPrice);
    }
}