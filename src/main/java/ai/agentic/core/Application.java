package ai.agentic.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Application Entry Point
 *
 * This is a template application configured with:
 * - AgentService from agentic-helper library (multi-LLM orchestration)
 * - Async execution support
 * - Scheduled task support
 * - spring-dotenv for .env file auto-loading
 *
 * Customize the package scan and annotations based on your project needs.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "7");
        SpringApplication.run(Application.class, args);
    }
}
