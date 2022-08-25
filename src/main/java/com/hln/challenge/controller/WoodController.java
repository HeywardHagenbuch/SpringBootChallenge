package com.hln.challenge.controller;

import com.hln.challenge.persistence.models.Wood;
import com.hln.challenge.service.WoodService;
import com.hln.challenge.service.dto.Bundle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WoodController {

    private final WoodService woodService;

    public WoodController(WoodService woodService) {
        this.woodService = woodService;
    }

    @PostMapping("/upload/{type}")
    public ResponseEntity<?> uploadFile(@PathVariable String type, @RequestParam("file") MultipartFile file) {
        List<Wood> woods = woodService.processFileUpload(type, file);
        return ResponseEntity.ok(woods); // Or handle the response as needed
    }

    @GetMapping("/bundle")
    public ResponseEntity<?> getBundles(@RequestParam("format") String format,
                                        @RequestParam(required = false) Double minPrice,
                                        @RequestParam(required = false) Double maxPrice) {
        List<Bundle> bundles = woodService.getBundles(format, minPrice, maxPrice);
        return ResponseEntity.ok(bundles);
    }
}