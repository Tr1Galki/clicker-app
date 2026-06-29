package com.example.clicker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ClickerController {

    private final ClickerService clickerService;

    public ClickerController(ClickerService clickerService) {
        this.clickerService = clickerService;
    }

    @PostMapping("/click")
    public Map<String, Long> click() {
        return Map.of("count", clickerService.increment());
    }

    @GetMapping("/count")
    public Map<String, Long> count() {
        return Map.of("count", clickerService.getCount());
    }
}
