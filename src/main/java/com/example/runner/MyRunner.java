package com.example.runner;

import com.example.service.GenerateService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyRunner implements CommandLineRunner {
    @Resource
    private GenerateService generateService;

    @Override
    public void run(String... args) throws Exception {
        generateService.generate(List.of("fumo_pckg_wdt_stat"));
    }
}
