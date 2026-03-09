package serp.project.pmcore.kernel.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ExecutorServiceConfig {
    
    @Bean
    @Primary
    public ExecutorService virtualThreadExecutorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
