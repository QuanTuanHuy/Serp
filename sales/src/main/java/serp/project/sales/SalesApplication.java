package serp.project.sales;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Slf4j
public class SalesApplication {

	public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SalesApplication.class, args);
        Environment env = context.getEnvironment();
        String name = env.getProperty("spring.application.app.name");
        String version = env.getProperty("spring.application.app.version");
        log.info("{} v{} is running...", name, version);
	}

}
