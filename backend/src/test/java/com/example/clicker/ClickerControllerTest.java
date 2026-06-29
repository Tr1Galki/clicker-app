package com.example.clicker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClickerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CounterRepository counterRepository;

    @BeforeEach
    void reset() {
        Counter counter = counterRepository.findById(1L).orElseThrow();
        counter.setCount(0L);
        counterRepository.save(counter);
    }

    @Test
    void getCountReturnsZeroInitially() throws Exception {
        mockMvc.perform(get("/api/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void postClickIncrementsAndReturnsNewCount() throws Exception {
        mockMvc.perform(post("/api/click"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void multipleClicksAccumulate() throws Exception {
        mockMvc.perform(post("/api/click"));
        mockMvc.perform(post("/api/click"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }
}
