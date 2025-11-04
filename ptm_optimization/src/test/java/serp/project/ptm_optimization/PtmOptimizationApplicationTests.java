package serp.project.ptm_optimization;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Skip Spring Boot context load test in unit runs without infrastructure")
class PtmOptimizationApplicationTests {

    @Test
    void contextLoads() {
        // intentionally disabled to avoid requiring DB/Redis during unit tests
    }

}
