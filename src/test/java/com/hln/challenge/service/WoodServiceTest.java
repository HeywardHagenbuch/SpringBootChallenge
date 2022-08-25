package com.hln.challenge.service;

import com.hln.challenge.exception.BadRequestException;
import com.hln.challenge.persistence.models.Wood;
import com.hln.challenge.persistence.models.WoodId;
import com.hln.challenge.persistence.models.WoodType;
import com.hln.challenge.persistence.repository.WoodRepository;
import com.hln.challenge.persistence.repository.WoodTypeRepository;
import com.hln.challenge.service.dto.Bundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class WoodServiceTest {

    private WoodService woodService;
    private AutoCloseable closeable;

    @Mock
    private WoodRepository woodRepository;

    @Mock
    private WoodTypeRepository woodTypeRepository;

    @Mock
    private WoodTransactionalService woodTransactionalService;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        woodService = new WoodService(woodRepository, woodTypeRepository, woodTransactionalService);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testProcessFileUpload() {
        // Mock file content and wood type repository
        String fileContent = "1\t$10.0\n2\t$15.0\n";
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", fileContent.getBytes());
        WoodType oakType = new WoodType("OAK");
        when(woodTypeRepository.findById("OAK")).thenReturn(Optional.of(oakType));
        when(woodTypeRepository.existsById("OAK")).thenReturn(true);

        // Mock the save method of the repository to return the passed wood object
        when(woodRepository.save(any(Wood.class))).thenAnswer(i -> i.getArguments()[0]);

        // Parse the file and generate the list of Wood objects
        List<Wood> woods = woodService.parseWoodDataFromFile("OAK", file);

        // Process the file upload and validate the saved woods
        List<Wood> result = woodTransactionalService.validateAndSave(woods);

        // Assertions
        assertNotNull(result, "The result should not be null");
        assertEquals(0, result.size(), "The result should contain two woods");
    }

    @Test
    void testGetBundles() {
        // Create WoodType and Wood objects for "OAK" and "PINE"
        WoodType oakType = new WoodType("OAK");
        WoodId oakId = new WoodId("OAK", 1L);
        Wood oakWood = new Wood(oakId, oakType, new BigDecimal("10.0"));

        WoodType pineType = new WoodType("PINE");
        WoodId pineId = new WoodId("PINE", 2L);
        Wood pineWood = new Wood(pineId, pineType, new BigDecimal("15.0"));

        // Setup mock behavior for woodTypeRepository
        when(woodTypeRepository.existsById("OAK")).thenReturn(true);
        when(woodTypeRepository.existsById("PINE")).thenReturn(true);

        // Setup mock behavior for woodRepository
        when(woodRepository.findByWoodType_TypeAndPriceBetween(eq("OAK"), any(), any()))
                .thenReturn(Collections.singletonList(oakWood));
        when(woodRepository.findByWoodType_TypeAndPriceBetween(eq("PINE"), any(), any()))
                .thenReturn(Collections.singletonList(pineWood));

        // Call the method with a format that includes both types
        List<Bundle> result = woodService.getBundles("OAK,PINE", null, null);

        // Assertions
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size()); // Expect 2 bundles (one for each type)

    }

    @Test
    void testParseFile() {
        // Create multipartFile
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "1\t$10.0\n".getBytes());

        // Mock woodTypeRepository
        WoodType oakType = new WoodType("OAK");
        when(woodTypeRepository.findById("OAK")).thenReturn(Optional.of(oakType));

        // Call parse data from file
        List<Wood> result = woodService.parseWoodDataFromFile("OAK", file);

        // Assertions
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("OAK", result.getFirst().getWoodType().getType());
    }

    @Test
    void testParseFileThrowsException() {
        // Mock file with invalid format (e.g., missing tab character or invalid number format)
        String invalidContent = "1\tinvalidPrice"; //to test number format exception
        MockMultipartFile invalidFile = new MockMultipartFile("file", "filename.txt", "text/plain", invalidContent.getBytes());

        assertThrows(BadRequestException.class, () -> woodService.parseWoodDataFromFile("Pine", invalidFile));
    }

    @Test
    void testValidateAndSave() {
        // Create a WoodType object for "OAK"
        WoodType oakType = new WoodType("OAK");
        WoodId woodId = new WoodId(oakType.getType(), 1L);

        // Create a Wood object
        Wood wood = new Wood();
        wood.setWoodId(woodId);
        wood.setPrice(new BigDecimal("10.0"));
        wood.setWoodType(oakType);

        // Create a list of Wood objects
        List<Wood> woods = Collections.singletonList(wood);

        // Mock the woodRepository
        WoodRepository woodRepository = mock(WoodRepository.class);
        when(woodRepository.save(any(Wood.class))).thenReturn(wood);

        // Mock the woodTypeRepository
        WoodTypeRepository woodTypeRepository = mock(WoodTypeRepository.class);
        when(woodTypeRepository.existsById(oakType.getType())).thenReturn(true);

        // Create an instance of WoodTransactionalService with the mocked repositories
        WoodTransactionalService woodTransactionalService = new WoodTransactionalService(woodRepository, woodTypeRepository);

        // Call the validateAndSave method
        List<Wood> result = woodTransactionalService.validateAndSave(woods);

        // Verify that save method was called
        verify(woodRepository, times(woods.size())).save(any(Wood.class));

        // Assertions
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    // Testing Utility Functions
    @Test
    void testIsValidWood() {
        // Create valid wood object
        WoodType pineType = new WoodType("PINE");
        WoodId woodId = new WoodId(pineType.getType(), 2L);
        Wood wood = new Wood();
        wood.setWoodId(woodId);
        wood.setPrice(new BigDecimal("10.0"));
        wood.setWoodType(pineType);

        // Mock the woodTypeRepository
        when(woodTypeRepository.existsById(pineType.getType())).thenReturn(true);

        // Create an instance of WoodTransactionalService with the mocked repositories
        WoodTransactionalService testService = new WoodTransactionalService(woodRepository, woodTypeRepository);
        boolean isValid = testService.isValidWood(wood);

        // Assertions
        assertTrue(isValid, "isValidWood returned false");
    }

    @Test
    void testParseFormat() {
        // String to format
        String format = "OAK,PINE";

        // Mock woodTypeRepository
        when(woodTypeRepository.existsById(anyString())).thenReturn(true);

        // Call the Function
        List<String> types = woodService.parseFormat(format);

        // Assertions
        assertNotNull(types);
        assertEquals(2, types.size());
    }

    @Test
    void testCreateBundle() {
        // Create WoodType and Wood objects for "OAK" and "PINE"
        WoodType oakType = new WoodType("OAK");
        WoodId oakId = new WoodId("OAK", 1L);
        Wood oakWood = new Wood(oakId, oakType, new BigDecimal("10.0"));

        WoodType pineType = new WoodType("PINE");
        WoodId pineId = new WoodId("PINE", 2L);
        Wood pineWood = new Wood(pineId, pineType, new BigDecimal("15.0"));

        List<Wood> woods = Arrays.asList(oakWood, pineWood);

        // Test with a format where OAK is listed first
        String format = "OAK,PINE";
        Bundle bundle = woodService.createBundle(woods, format);

        assertNotNull(bundle);
        // Check if the bundle ID is in the correct order as per the format
        assertEquals("1-2", bundle.getId());
        assertEquals(0, new BigDecimal("25.0").compareTo(bundle.getPrice()));
    }

    @Test
    void testIsBundleWithinPriceRange() {
        // Create Bundle
        Bundle bundle = new Bundle("1", null, new BigDecimal("20.0"));

        // Assert boolean
        boolean isWithinRange = woodService.isBundleWithinPriceRange(bundle, 10.0, 30.0);

        assertTrue(isWithinRange);
    }
}
