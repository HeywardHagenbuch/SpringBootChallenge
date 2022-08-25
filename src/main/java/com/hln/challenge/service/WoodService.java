package com.hln.challenge.service;

import com.hln.challenge.exception.BadRequestException;
import com.hln.challenge.persistence.models.Wood;
import com.hln.challenge.persistence.models.WoodId;
import com.hln.challenge.persistence.models.WoodType;
import com.hln.challenge.persistence.repository.WoodRepository;
import com.hln.challenge.persistence.repository.WoodTypeRepository;
import com.hln.challenge.service.dto.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class WoodService {

    private final WoodRepository woodRepository;
    private final WoodTypeRepository woodTypeRepository;
    private final WoodTransactionalService woodTransactionalService;

    private static final Logger logger = LoggerFactory.getLogger(WoodService.class);

    public WoodService(WoodRepository woodRepository, WoodTypeRepository woodTypeRepository, WoodTransactionalService woodTransactionalService) {
        this.woodRepository = woodRepository;
        this.woodTypeRepository = woodTypeRepository;
        this.woodTransactionalService = woodTransactionalService;
    }

    public List<Wood> processFileUpload(String type, MultipartFile file) {
        logger.info("Processing file upload for type: {}", type);
        // Check if the WoodType exists in the database, if not, create it
        WoodType woodType = woodTypeRepository.findById(type)
                .orElseGet(() -> {
                    logger.info("Creating new wood type: {}", type);
                    WoodType newType = new WoodType(type);
                    return woodTypeRepository.save(newType);
                });

        List<Wood> woods = parseWoodDataFromFile(type, file);

        // Set the WoodType on each Wood object
        woods.forEach(wood -> wood.setWoodType(woodType));

        return woodTransactionalService.validateAndSave(woods);
    }

    public List<Bundle> getBundles(String format, Double minPrice, Double maxPrice) {
        List<String> typesInFormat = parseFormat(format);
        if (typesInFormat.isEmpty()) {
            throw new BadRequestException("Invalid format: No valid wood types found");
        }
        return createBundles(typesInFormat, minPrice, maxPrice);
    }

    public List<Bundle> createBundles(List<String> typesInFormat, Double minPrice, Double maxPrice) {
        List<Bundle> bundles = new ArrayList<>();
        for (String type : typesInFormat) {
            BigDecimal min = BigDecimal.valueOf(Optional.ofNullable(minPrice).orElse(0.0));
            BigDecimal max = BigDecimal.valueOf(Optional.ofNullable(maxPrice).orElse(Double.MAX_VALUE));
            List<Wood> woods = woodRepository.findByWoodType_TypeAndPriceBetween(type, min, max);
            logger.info("Woods found for type '{}' between prices {} and {}: {}", type, min, max, woods);

            if (!woods.isEmpty()) {
                String bundleId = woods.stream()
                        .map(wood -> wood.getWoodId().getId().toString())
                        .collect(Collectors.joining("-"));
                BigDecimal totalPrice = woods.stream()
                        .map(Wood::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                bundles.add(new Bundle(bundleId, woods, totalPrice));
            }
        }

        return bundles.stream()
                .sorted(Comparator.comparing(Bundle::getPrice).reversed()
                        .thenComparing(Bundle::getId))
                .collect(Collectors.toList());
    }

    Bundle createBundle(List<Wood> woods, String format) {
        String bundleId = createBundleId(woods, format);
        BigDecimal totalPrice = woods.stream()
                .map(Wood::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Bundle bundle = new Bundle();
        bundle.setId(bundleId);
        bundle.setBundle(woods);
        bundle.setPrice(totalPrice);
        return bundle;
    }

    private String createBundleId(List<Wood> woods, String format) {
        // Split the format string to get the order of wood types
        List<String> order = Arrays.asList(format.split(","));

        // Create a map of wood type to IDs
        Map<String, List<Long>> typeToIds = woods.stream()
                .collect(Collectors.groupingBy(
                        wood -> wood.getWoodType().getType(),
                        Collectors.mapping(wood -> wood.getWoodId().getId(), Collectors.toList())
                ));

        // Build the bundle ID based on the order and IDs
        return order.stream()
                .filter(typeToIds::containsKey)
                .flatMap(type -> typeToIds.get(type).stream().sorted())
                .map(Object::toString)
                .collect(Collectors.joining("-"));
    }
    public List<Wood> parseWoodDataFromFile(String type, MultipartFile file) {
        List<Wood> woods = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Check if line is a header
                if (line.trim().toLowerCase().matches("id\\s+price")) {
                    continue; // Skip header
                }
                String[] data = line.split("\\s+"); // Adjusted split regex for spaces/tabs
                if (data.length >= 2) {
                    Long id = Long.parseLong(data[0]);
                    BigDecimal price = new BigDecimal(data[1].replace("$", ""));
                    WoodType woodType = woodTypeRepository.findById(type)
                            .orElseThrow(() -> new BadRequestException("Invalid wood type: " + type));
                    WoodId woodId = new WoodId(type, id);
                    Wood wood = new Wood(woodId, woodType, price);
                    woods.add(wood);
                }
            }
        } catch (Exception e) {
            throw new BadRequestException("Error parsing file: " + e.getMessage());
        }
        return woods;
    }

    List<String> parseFormat(String format) {
        Set<String> types = new HashSet<>(Arrays.asList(format.split(",")));
        List<String> validTypes = types.stream()
                .filter(type -> {
                    boolean exists = woodTypeRepository.existsById(type);
                    logger.info("Checking type '{}': exists = {}", type, exists);
                    return exists;
                })
                .collect(Collectors.toList());
        logger.info("Valid types after parsing format: {}", validTypes);
        return validTypes;
    }

    boolean isBundleWithinPriceRange(Bundle bundle, Double minPrice, Double maxPrice) {
        BigDecimal price = bundle.getPrice();
        return (minPrice == null || price.compareTo(BigDecimal.valueOf(minPrice)) >= 0) &&
                (maxPrice == null || price.compareTo(BigDecimal.valueOf(maxPrice)) <= 0);
    }
}
