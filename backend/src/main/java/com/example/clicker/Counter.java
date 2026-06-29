package com.example.clicker;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long count;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
