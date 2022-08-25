package com.hln.challenge.controller;

import com.hln.challenge.exception.BadRequestException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {

    @GetMapping("/trigger-exception")
    public void triggerException() {
        throw new BadRequestException("Testing for BadRequestException");
    }
}
