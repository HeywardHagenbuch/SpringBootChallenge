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
        List<Wood> woods = parseWoodDataFromFile(type, file);

        // Fetch the WoodType entity from the database
        Optional<WoodType> woodTypeOptional = woodTypeRepository.findById(type);
        if (woodTypeOptional.isEmpty()) {
            throw new BadRequestException("Invalid wood type: " + type);
        }
        WoodType woodType = woodTypeOptional.get();

        // Set the WoodType on each Wood object
        woods.forEach(wood -> wood.setWoodType(woodType));

        return woodTransactionalService.validateAndSave(woods);
    }


    public List<Bundle> getBundles(String format, Double minPrice, Double maxPrice) {
        List<String> typesInFormat = parseFormat(format);

        // Fetch woods of all types in format
        List<Wood> allWoods = typesInFormat.stream()
                .flatMap(type -> woodRepository.findByWoodType_Type(type).stream())
                .collect(Collectors.toList());

        // Create a bundle with all the woods
        Bundle bundle = createBundle(allWoods, format);
        List<Bundle> bundles = new ArrayList<>();
        if (isBundleWithinPriceRange(bundle, minPrice, maxPrice)) {
            bundles.add(bundle);
        }

        // Sort the bundles as required
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
        logger.info("Parsing file: {}", file.getOriginalFilename());
        List<Wood> woods = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\t"); // Splitting by tab
                if (data.length >= 2) {
                    try {
                        Long id = Long.parseLong(data[0]);
                        BigDecimal price = new BigDecimal(data[1].replace("$", ""));
                        WoodType woodType = woodTypeRepository.findById(type)
                                .orElseThrow(() -> new BadRequestException("Invalid wood type: " + type));

                        WoodId woodId = new WoodId(type, id); // Create the composite key
                        Wood wood = new Wood(woodId, woodType, price);

                        woods.add(wood);
                    } catch (NumberFormatException e) {
                        throw new BadRequestException("Invalid number format on line: " + line);
                    }
                }
            }
        } catch (Exception e) {
            throw new BadRequestException("Error parsing file: " + e.getMessage());
        }
        logger.info("Parsed woods from file: {}", woods);
        return woods;
    }

    List<String> parseFormat(String format) {
        // Split the format string and validate each type
        Set<String> types = new HashSet<>(Arrays.asList(format.split(",")));
        // Verify each type exists in the database and there are no duplicates
        // Return a list of valid types
        return types.stream()
                .filter(woodTypeRepository::existsById)
                .collect(Collectors.toList());
    }

    boolean isBundleWithinPriceRange(Bundle bundle, Double minPrice, Double maxPrice) {
        BigDecimal price = bundle.getPrice();
        return (minPrice == null || price.compareTo(BigDecimal.valueOf(minPrice)) >= 0) &&
                (maxPrice == null || price.compareTo(BigDecimal.valueOf(maxPrice)) <= 0);
    }
}
