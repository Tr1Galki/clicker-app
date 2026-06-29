package com.example.clicker;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClickerService {

    private final CounterRepository counterRepository;

    public ClickerService(CounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    @PostConstruct
    public void init() {
        if (counterRepository.count() == 0) {
            Counter counter = new Counter();
            counter.setCount(0L);
            counterRepository.save(counter);
        }
    }

    @Transactional(readOnly = true)
    public long getCount() {
        return counterRepository.findAll().stream()
                .findFirst()
                .map(Counter::getCount)
                .orElse(0L);
    }

    @Transactional
    public long increment() {
        counterRepository.increment();
        return getCount();
    }
}
