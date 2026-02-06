# ApplicationStartup (agentic-helper v1.5.2)

Guide to `ApplicationStartup.java` - the initialization handler.

**Location:** `src/main/java/ai/agentic/core/startup/ApplicationStartup.java`

---

## What It Does

Executes after Spring Boot is fully ready. Logs the state of the agent service:
- Number of loaded agents
- Number of configured instances
- Available models across all instances
- Degraded mode status
- Individual agent details (id, name, model)

**Note:** Since v1.5.2, agents are loaded automatically during `AgentService` construction. There is no `createAllAgents()` call needed.

---

## Current Implementation

```java
@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    private final AgentService agentService;

    public ApplicationStartup(AgentService agentService) {
        this.agentService = agentService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("Application is ready");

        logger.info("Agent definitions loaded from: src/main/resources/agents");
        logger.info("   {} agent(s) loaded", agentService.getAllAgents().size());
        logger.info("   {} instance(s) configured", agentService.getInstanceCount());
        logger.info("   Available models: {}", agentService.getAvailableModels());
        logger.info("   Degraded mode: {}", agentService.isDegradedMode());

        agentService.getAllAgents().forEach((id, agent) ->
                logger.info("   - Agent [{}] '{}' (model: {})", id, agent.getName(), agent.getModel()));
    }
}
```

---

## Startup Sequence

```
Application Start
    |
Load .env (spring-dotenv)
    |
Create Spring Beans
    |
Initialize AgentService
    ├─ Parse LLM_INSTANCES JSON
    ├─ Create provider clients (OpenAI, Azure, Claude)
    └─ Load agent JSON files from agents/ folder
    |
All Beans Ready
    |
ApplicationReadyEvent
    |
ApplicationStartup.onApplicationEvent()
    ├─ Log agent count
    ├─ Log instance count
    ├─ Log available models
    └─ Log each agent
    |
Application Ready for Requests
```

---

## Customization

### Add Health Validation

```java
@Override
public void onApplicationEvent(ApplicationReadyEvent event) {
    // ... existing logging ...

    if (agentService.isDegradedMode()) {
        logger.warn("AgentService is in degraded mode - some providers may be unavailable");
    }

    if (agentService.getInstanceCount() == 0) {
        logger.error("No instances configured! Set LLM_INSTANCES in .env");
    }
}
```

### Async Warm-up Request

```java
@Override
public void onApplicationEvent(ApplicationReadyEvent event) {
    // ... existing logging ...

    // Optional: warm-up request to verify connectivity
    CompletableFuture.runAsync(() -> {
        try {
            agentService.requestAgent("100", "ping").join();
            logger.info("Warm-up request succeeded");
        } catch (Exception e) {
            logger.warn("Warm-up request failed: {}", e.getMessage());
        }
    });
}
```

---

## Troubleshooting

| Symptom | Cause | Solution |
|---------|-------|----------|
| 0 agents loaded | Missing or invalid JSON files | Check `src/main/resources/agents/` folder |
| 0 instances configured | Missing `LLM_INSTANCES` | Set env variable in `.env` |
| Degraded mode: true | Some provider connections failed | Check API keys and network |

---

**Last Updated**: February 2026
