package com.hln.challenge.integration;

import com.hln.challenge.controller.WoodController;
import com.hln.challenge.exception.CustomExceptionHandler;
import com.hln.challenge.persistence.models.Wood;
import com.hln.challenge.persistence.models.WoodId;
import com.hln.challenge.persistence.models.WoodType;
import com.hln.challenge.persistence.repository.WoodRepository;
import com.hln.challenge.persistence.repository.WoodTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class WoodApplicationIntegrationTest {

    @Autowired
    private WoodController woodController;

    @Autowired
    private WoodRepository woodRepository;

    @Autowired
    private WoodTypeRepository woodTypeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(woodController)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();
        insertTestData();
    }

    void insertTestData() {
        // Insert wood types
        WoodType oakType = woodTypeRepository.findById("OAK")
                .orElseGet(() -> woodTypeRepository.save(new WoodType("OAK")));
        WoodType pineType = woodTypeRepository.findById("PINE")
                .orElseGet(() -> woodTypeRepository.save(new WoodType("PINE")));
        WoodType mapleType = woodTypeRepository.findById("MAPLE")
                .orElseGet(() -> woodTypeRepository.save(new WoodType("MAPLE")));

        // Insert woods
        woodRepository.save(new Wood(new WoodId("OAK", 1L), oakType, new BigDecimal("10.00")));
        woodRepository.save(new Wood(new WoodId("PINE", 2L), pineType, new BigDecimal("12.00")));
        woodRepository.save(new Wood(new WoodId("MAPLE", 3L), mapleType, new BigDecimal("15.00")));
    }

    @AfterEach
    void tearDown() {
        woodRepository.deleteAll();
        woodTypeRepository.deleteAll();
    }

    @Test
    void testDataLoading() {
        printWoodTypesFromDatabase();
        List<String> woodTypes = woodRepository.findAllWoodTypes();
        System.out.println(woodTypes);
        assertTrue(woodTypes.containsAll(Arrays.asList("OAK", "PINE", "MAPLE")));
    }

    @Test
    void testFileUploadEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "filename.txt",
                "text/plain",
                "1\t$10.0\tOAK\n2\t$15.0\tOAK\n".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/OAK").file(file))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void testInvalidFileUpload() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "invalid.txt",
                "text/plain",
                "1\t\tOAK\n2t\n".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/OAK").file(invalidFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidBundleRequest() throws Exception {
        mockMvc.perform(get("/api/bundle")
                        .param("format", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetBundlesEndpoint() throws Exception {
        mockMvc.perform(get("/api/bundle")
                        .param("format", "OAK")
                        .param("minPrice", "10.0")
                        .param("maxPrice", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").exists());
    }


    @Test
    void testDatabaseStateAfterFileUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "filename.txt",
                "text/plain",
                "1\t$10.0\tOAK\n2\t$15.0\tOAK\n".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/OAK").file(file))
                .andExpect(status().isOk())
                .andDo(print());

        List<Wood> woods = woodRepository.findByWoodType_Type("OAK");
        assertFalse(woods.isEmpty());
    }


    private void printWoodTypesFromDatabase() {
        List<String> woodTypes = jdbcTemplate.query("SELECT type FROM wood_type",
                (rs, rowNum) -> rs.getString("type"));
        System.out.println("Wood types from database: " + woodTypes);
    }
}
