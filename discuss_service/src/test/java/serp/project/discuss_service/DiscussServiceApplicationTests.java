package serp.project.discuss_service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test for application context loading.
 * Disabled by default as it requires database and infrastructure.
 * Run with: ./run-dev.sh && ./mvnw test -Dtest=DiscussServiceApplicationTests
 */
@SpringBootTest
@Disabled("Requires database and infrastructure - run manually with run-dev.sh")
class DiscussServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
