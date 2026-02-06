package ai.agentic.core.config;

import io.github.yannfavinleveque.agentic.agent.service.AgentService;
import io.github.yannfavinleveque.agentic.agent.config.AgentServiceConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for AgentService from agentic-helper library v1.5.2
 *
 * Uses JSON-based instance configuration for maximum flexibility:
 * - Model-aware routing and load balancing across instances
 * - Support for mixed OpenAI + Azure OpenAI + Azure Anthropic deployments
 * - Automatic embedding generation with round-robin per model
 * - Rate limiting, retry logic, and autonomous agent mode
 *
 * Configuration via LLM_INSTANCES environment variable (JSON array)
 * See: https://github.com/Yann-Favin-Leveque/agentic-helper
 */
@Configuration
public class AgentServiceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AgentServiceConfiguration.class);

    @Value("${llm.instances:[]}")
    private String instancesJson;

    @Value("${llm.concurrent.stream.limit.per.instance:15}")
    private int concurrentStreamLimitPerInstance;

    @Value("${llm.max.retries:3}")
    private int maxRetries;

    @Value("${llm.default.response.timeout:120000}")
    private long defaultResponseTimeout;

    @Bean
    public AgentService agentService() {
        logger.info("Initializing AgentService from agentic-helper library v1.5.2");

        AgentServiceConfig config = AgentServiceConfig.builder()
                .instancesJson(instancesJson)
                .requestsPerSecond(concurrentStreamLimitPerInstance)
                .maxRetries(maxRetries)
                .defaultResponseTimeout(defaultResponseTimeout)
                .agentJsonFolderPath("src/main/resources/agents")
                .agentResultClassPackage("ai.agentic.core.engine.objects.agentResultClasses")
                .build();

        AgentService agentService = new AgentService(config);

        logger.info("AgentService initialized successfully");
        logger.info("   Instances: {}, Stream limit/instance: {}, Max retries: {}",
                agentService.getInstanceCount(), concurrentStreamLimitPerInstance, maxRetries);
        logger.info("   Available models: {}", agentService.getAvailableModels());

        return agentService;
    }
}
