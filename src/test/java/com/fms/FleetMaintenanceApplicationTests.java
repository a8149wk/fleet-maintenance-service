package com.fms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class FleetMaintenanceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the full Spring application context bootstraps
        // successfully against the embedded H2 database (dev profile).
    }
}
