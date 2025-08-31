package serp.project.account.kernel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Configuration
public class VirtualThreadsConfig {

    @Bean
    public TaskExecutor virtualThreadTaskExecutor() {
        return new TaskExecutor() {
            private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

            @Override
            public void execute(Runnable task) {
                executor.execute(task);
            }
        };
    }

    @Bean
    @Primary
    public ExecutorService virtualThreadPerTaskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public AsyncTaskExecutor virtualThreadAsyncTaskExecutor() {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        return new AsyncTaskExecutor() {
            @Override
            public void execute(Runnable task) {
                executor.execute(task);
            }

            @Override
            public Future<?> submit(Runnable task) {
                return executor.submit(task);
            }

            @Override
            public <T> Future<T> submit(Callable<T> task) {
                return executor.submit(task);
            }
        };
    }
}