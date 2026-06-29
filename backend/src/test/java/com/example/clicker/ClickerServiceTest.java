package com.example.clicker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClickerServiceTest {

    @Autowired
    private ClickerService clickerService;

    @Autowired
    private CounterRepository counterRepository;

    @BeforeEach
    void reset() {
        Counter counter = counterRepository.findAll().stream().findFirst().orElseThrow();
        counter.setCount(0L);
        counterRepository.save(counter);
    }

    @Test
    void initialCountIsZero() {
        assertThat(clickerService.getCount()).isEqualTo(0L);
    }

    @Test
    void incrementIncreasesCountByOne() {
        long first = clickerService.increment();
        assertThat(first).isEqualTo(1L);
        assertThat(clickerService.getCount()).isEqualTo(1L);
    }

    @Test
    void multipleIncrementsAccumulate() {
        clickerService.increment();
        clickerService.increment();
        long third = clickerService.increment();
        assertThat(third).isEqualTo(3L);
    }
}
