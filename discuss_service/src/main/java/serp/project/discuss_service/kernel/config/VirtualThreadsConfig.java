/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.kernel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync(proxyTargetClass = true)
public class VirtualThreadsConfig {

    @Bean
    @Primary
    public AsyncTaskExecutor virtualThreadAsyncTaskExecutor() {
        return new VirtualThreadTaskExecutor();
    }

    /**
     * Virtual thread executor for attachment uploads.
     * Virtual threads are ideal for I/O-bound operations like file uploads.
     */
    @Bean("attachmentUploadExecutor")
    public ExecutorService attachmentUploadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Virtual thread executor for post-message async operations.
     * Used for Kafka events, cache updates after message send.
     */
    @Bean("messageAsyncExecutor")
    public ExecutorService messageAsyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
