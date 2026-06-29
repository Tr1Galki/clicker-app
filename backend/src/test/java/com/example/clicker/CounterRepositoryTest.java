package com.example.clicker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CounterRepositoryTest {

    @Autowired
    private CounterRepository repository;

    @Test
    void savesAndReadsCounter() {
        Counter counter = new Counter();
        counter.setCount(5L);
        Counter saved = repository.save(counter);

        assertThat(repository.findById(saved.getId())).isPresent();
        assertThat(repository.findById(saved.getId()).get().getCount()).isEqualTo(5L);
    }
}
