package com.example.clicker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CounterRepository extends JpaRepository<Counter, Long> {

    @Modifying
    @Query("UPDATE Counter c SET c.count = c.count + 1")
    void increment();
}
