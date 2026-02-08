# AgentServiceConfiguration (agentic-helper v1.5.2)

Guide to `AgentServiceConfiguration.java` - the Spring configuration that initializes AgentService.

**Location:** `src/main/java/ai/agentic/core/config/AgentServiceConfiguration.java`

---

## What It Does

1. Reads configuration from environment variables (via spring-dotenv)
2. Creates AgentService bean with `AgentServiceConfig` builder
3. Enables dependency injection throughout the application
4. Configures rate limiting, retry logic, and timeouts
5. Sets up agent/result class package paths

---

## Current Configuration

```java
@Configuration
public class AgentServiceConfiguration {

    @Value("${llm.instances:[]}")
    private String instancesJson;

    @Value("${llm.requests.per.second:2}")
    private int requestsPerSecond;

    @Value("${llm.max.concurrent.streams:20}")
    private int maxConcurrentStreams;

    @Value("${llm.max.retries:3}")
    private int maxRetries;

    @Value("${llm.default.response.timeout:120000}")
    private long defaultResponseTimeout;

    @Bean
    public AgentService agentService() {
        AgentServiceConfig config = AgentServiceConfig.builder()
                .instancesJson(instancesJson)
                .requestsPerSecond(requestsPerSecond)
                .maxConcurrentStreamsPerInstance(maxConcurrentStreams)
                .maxRetries(maxRetries)
                .defaultResponseTimeout(defaultResponseTimeout)
                .agentJsonFolderPath("src/main/resources/agents")
                .agentResultClassPackage("ai.agentic.core.engine.objects.agentResultClasses")
                .build();

        return new AgentService(config);
    }
}
```

---

## Configuration Parameters

| Parameter | Property | Default | Description |
|-----------|----------|---------|-------------|
| `instancesJson` | `llm.instances` | `[]` | JSON array of provider instances |
| `requestsPerSecond` | `llm.requests.per.second` | `2` | Max API calls per second per instance |
| `maxConcurrentStreamsPerInstance` | `llm.max.concurrent.streams` | `20` | Max concurrent HTTP/2 streams per instance |
| `maxRetries` | `llm.max.retries` | `3` | Retry attempts on transient failure |
| `defaultResponseTimeout` | `llm.default.response.timeout` | `120000` | Default timeout in ms |
| `agentJsonFolderPath` | hardcoded | `src/main/resources/agents` | Agent JSON definitions folder |
| `agentResultClassPackage` | hardcoded | `ai.agentic.core.engine.objects.agentResultClasses` | Package for result POJOs |

---

## Builder Fields Reference (AgentServiceConfig)

| Field | Type | Description |
|-------|------|-------------|
| `instancesJson` | String | JSON array of API instances |
| `requestsPerSecond` | int | Max API calls per second per instance |
| `maxConcurrentStreamsPerInstance` | int | Max concurrent HTTP/2 streams per instance |
| `maxRetries` | int | Max retry attempts |
| `defaultResponseTimeout` | long | Default timeout (ms) |
| `agentJsonFolderPath` | String | Path to agent JSON folder |
| `agentResultClassPackage` | String | Package for result classes |
| `functionParameterClassPackage` | String | Package for function parameter classes (autonomous mode) |
| `functionExecutorClassPackage` | String | Package for function executor classes |

---

## Customization Examples

### Higher Rate Limit + Custom Paths

```java
AgentServiceConfig config = AgentServiceConfig.builder()
        .instancesJson(instancesJson)
        .requestsPerSecond(50)
        .maxRetries(5)
        .defaultResponseTimeout(180000L)  // 3 minutes
        .agentJsonFolderPath("config/agents")
        .agentResultClassPackage("com.myapp.results")
        .build();
```

### Profile-Specific Configuration

```java
@Bean
@Profile("development")
public AgentService devAgentService() {
    return new AgentService(AgentServiceConfig.builder()
            .instancesJson(instancesJson)
            .requestsPerSecond(5)
            .build());
}

@Bean
@Profile("production")
public AgentService prodAgentService() {
    return new AgentService(AgentServiceConfig.builder()
            .instancesJson(instancesJson)
            .requestsPerSecond(50)
            .maxRetries(5)
            .build());
}
```

---

## Dependency Injection

```java
@Service
public class MyService {

    @Autowired
    private AgentService agentService;

    public String doSomething() {
        return agentService.requestAgent("100", "Question").join().getContent();
    }
}
```

---

## Troubleshooting

| Error | Cause | Solution |
|-------|-------|----------|
| `Failed to parse instancesJson` | Invalid JSON in `LLM_INSTANCES` | Validate JSON syntax |
| `No instances configured` | Empty `LLM_INSTANCES` | Set env variable in `.env` |
| `RateLimitError` | Too many requests | Increase `LLM_REQUESTS_PER_SECOND` or add instances |

---

**Last Updated**: February 2026
