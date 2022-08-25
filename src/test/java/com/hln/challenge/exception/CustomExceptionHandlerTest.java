package com.hln.challenge.exception;

import com.hln.challenge.controller.DummyController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DummyController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomExceptionHandler.class))
public class CustomExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHandleBadRequest() throws Exception {
        mockMvc.perform(get("/trigger-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.anything()));
    }
}
