package serp.project.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Slf4j
public class LogisticsApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(LogisticsApplication.class, args);
        Environment env = context.getEnvironment();
        String name = env.getProperty("spring.application.app.name");
        String version = env.getProperty("spring.application.app.version");
        log.info("{} v{} is running...", name, version);
	}

}
