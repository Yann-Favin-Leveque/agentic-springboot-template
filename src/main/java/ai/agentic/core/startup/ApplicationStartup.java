package ai.agentic.core.startup;

import io.github.yannfavinleveque.agentic.agent.service.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * ApplicationStartup - Handles initialization tasks when application is ready
 *
 * Executed after all Spring beans are initialized and application context is ready.
 * Agents are automatically loaded from JSON files during AgentService construction.
 */
@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartup.class);

    private final AgentService agentService;

    public ApplicationStartup(AgentService agentService) {
        this.agentService = agentService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("========================================");
        logger.info("Application is ready");
        logger.info("========================================");

        logger.info("Agent definitions loaded from: src/main/resources/agents");
        logger.info("   {} agent(s) loaded", agentService.getAllAgents().size());
        logger.info("   {} instance(s) configured", agentService.getInstanceCount());
        logger.info("   Available models: {}", agentService.getAvailableModels());
        logger.info("   Degraded mode: {}", agentService.isDegradedMode());

        agentService.getAllAgents().forEach((id, agent) ->
                logger.info("   - Agent [{}] '{}' (model: {})", id, agent.getName(), agent.getModel()));

        logger.info("========================================");
    }
}
