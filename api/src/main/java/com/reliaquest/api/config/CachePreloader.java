package com.reliaquest.api.config;

import com.reliaquest.api.service.EmployeeServiceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CachePreloader implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(CachePreloader.class);
    private final EmployeeServiceApi employeeServiceApi;

    public CachePreloader(EmployeeServiceApi employeeServiceApi) {
        this.employeeServiceApi = employeeServiceApi;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Preloading cache on startup...");
        try {
            employeeServiceApi.getAllEmployees();
            logger.info("Cache preloaded successfully");
        } catch (Exception e) {
            logger.warn("Failed to preload cache on startup: {}", e.getMessage());
        }
    }
}
