package ai.agentic.core.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async configuration for Spring's @Async annotation
 *
 * Provides a custom thread pool executor for async agent requests.
 * Configure pool size via application.properties.
 */
@Configuration
public class AsyncConfiguration {

    @Value("${async.pool.size:20}")
    private int poolSize;

    @Bean(name = "agentRequestsExecutor")
    public Executor agentRequestsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(poolSize * 100);
        executor.setThreadNamePrefix("Async-Agent-");
        executor.initialize();
        return executor;
    }
}
