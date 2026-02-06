# AgentService Usage Guide (agentic-helper v1.5.2)

Quick reference for using AgentService with the unified API.

---

## Core Concepts

### Unified `requestAgent()` API

```java
// Simple request (stateless)
CompletableFuture<AgentResult> requestAgent(String agentId, String message)

// With conversation (multi-turn with history)
CompletableFuture<AgentResult> requestAgent(String agentId, String message, String conversationId)

// Autonomous mode (tool execution loop)
CompletableFuture<AgentResult> requestAgent(String agentId, String task, ToolExecutor toolExecutor)
```

**Return behavior:**
- If agent has `resultClass` configured -> Returns that specific type (e.g., `DataAnalysisResult`)
- If no `resultClass` -> Returns `DefaultResult` with `getContent()` for raw string

```java
// Agent with resultClass="DataAnalysisResult"
AgentResult result = agentService.requestAgent("101", "Analyze data").join();
DataAnalysisResult data = (DataAnalysisResult) result;

// Agent without resultClass
AgentResult result = agentService.requestAgent("100", "Hello").join();
String text = result.getContent();  // Universal getter

// Direct model usage (no agent registration needed)
AgentResult result = agentService.requestAgent("gpt-4o", "Hello").join();
String text = result.getContent();
```

### Provider Auto-Detection

Provider is determined automatically from the model name - no `isOpenAI` field needed:
- `gpt-4o`, `gpt-4o-mini` -> routes to OpenAI or Azure OpenAI instance
- `claude-sonnet-4-5`, `claude-haiku-4-5` -> routes to Azure Anthropic instance
- `text-embedding-3-small` -> routes to instance with that model
- `dall-e-3` -> routes to instance with DALL-E model

---

## Basic Usage

### 1. Simple Request (One-shot)

```java
AgentResult result = agentService.requestAgent("100", "What is 2+2?").join();
String answer = result.getContent();
```

### 2. Structured Output

```java
// Agent 101 has resultClass="DataAnalysisResult"
AgentResult result = agentService.requestAgent("101", "Analyze: 10, 20, 30").join();
DataAnalysisResult analysis = (DataAnalysisResult) result;
System.out.println(analysis.getSummary());
System.out.println(analysis.getTrends());
```

### 3. Conversation with Memory

```java
// Create conversation (in-memory history store)
String conversationId = agentService.createConversation();

try {
    // Each message sees previous context
    AgentResult resp1 = agentService.requestAgent("100", "My name is John", conversationId).join();
    AgentResult resp2 = agentService.requestAgent("100", "What's my name?", conversationId).join();
    // resp2.getContent(): "Your name is John"
} finally {
    agentService.deleteConversation(conversationId);
}
```

### 4. Direct Model Usage

```java
// Use any model without agent registration
AgentResult result = agentService.requestAgent("gpt-4o", "Explain quantum computing").join();
String answer = result.getContent();

// Works with any configured model
AgentResult claude = agentService.requestAgent("claude-sonnet-4-5", "Hello Claude!").join();
```

### 5. Chat Completion (Stateless)

```java
List<ChatMessage> messages = List.of(
    ChatMessage.SystemMessage.of("You are a helpful assistant"),
    ChatMessage.UserMessage.of("Hello!")
);

ChatCompletionResult response = agentService.chatCompletion("gpt-4o", messages, 0.7).join();
String text = response.getResult();
```

### 6. Structured Chat Completion

```java
List<ChatMessage> messages = List.of(
    ChatMessage.SystemMessage.of("Respond in JSON"),
    ChatMessage.UserMessage.of("Analyze this data")
);

// By class
DataAnalysisResult result = agentService.chatCompletion("gpt-4o", messages, 0.7, DataAnalysisResult.class).join();

// By class name
AgentResult result = agentService.chatCompletion("gpt-4o", messages, 0.7, "DataAnalysisResult").join();
```

### 7. Embeddings

```java
float[] embedding = agentService.generateEmbedding("Text to vectorize", "text-embedding-3-small").join();
// Returns 1536-dimensional vector
```

### 8. Image Generation

```java
String base64Image = agentService.generateImage(
    "A futuristic city",
    "dall-e-3",
    Size.X1024,
    ImageRequest.Quality.STANDARD
).join();
```

### 9. Autonomous Agent Mode

```java
// Define tool executor
ToolExecutor toolExecutor = call -> {
    switch (call.getName()) {
        case "search_data":
            String query = call.getArgumentsAsMap().get("query").toString();
            return "Results for: " + query;
        case "calculate":
            String expr = call.getArgumentsAsMap().get("expression").toString();
            return "Result: 42";
        default:
            return "Unknown function: " + call.getName();
    }
};

// Agent 107 has autonomous=true with functions defined
AgentResult result = agentService.requestAgent("107", "Research AI trends", toolExecutor).join();
```

---

## Result Classes

### Creating a Result Class

```java
package ai.agentic.core.engine.objects.agentResultClasses;

import io.github.yannfavinleveque.agentic.agent.model.AgentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class MyResult implements AgentResult {
    private String summary;
    private List<String> items;
    private Double score;
}
```

**Requirements:**
1. Implement `io.github.yannfavinleveque.agentic.agent.model.AgentResult`
2. Use Lombok `@Getter`, `@Setter`, `@ToString`
3. Place in configured package (default: `ai.agentic.core.engine.objects.agentResultClasses`)

### DefaultResult

When no `resultClass` is configured, `requestAgent()` returns a `DefaultResult`:

```java
AgentResult result = agentService.requestAgent("100", "Hello").join();
String text = result.getContent();  // Raw LLM response
```

---

## Orchestration Patterns

### Sequential Pipeline

```java
AgentResult step1 = agentService.requestAgent("101", data).join();
AgentResult step2 = agentService.requestAgent("102", step1.getContent()).join();
```

### Parallel Execution

```java
CompletableFuture<AgentResult> task1 = agentService.requestAgent("100", "Q1");
CompletableFuture<AgentResult> task2 = agentService.requestAgent("100", "Q2");
CompletableFuture<AgentResult> task3 = agentService.requestAgent("100", "Q3");

CompletableFuture.allOf(task1, task2, task3).join();
```

### Resilient Call with Fallback

```java
try {
    return agentService.requestAgent(primaryAgentId, message).join().getContent();
} catch (Exception e) {
    return agentService.requestAgent(fallbackAgentId, message).join().getContent();
}
```

---

## Agent Management

```java
// List all agents
Map<String, Agent> agents = agentService.getAllAgents();

// Get specific agent
Agent agent = agentService.getAgent("100");

// Reload from JSON (hot reload)
agentService.reloadAgents().join();
agentService.reloadAgent("100").join();

// Register programmatically
Agent newAgent = Agent.builder()
    .id("200").name("My Agent").model("gpt-4o")
    .instructions("You are...").temperature(0.7)
    .build();
agentService.registerAgent(newAgent);

// Service info
int instanceCount = agentService.getInstanceCount();
List<String> models = agentService.getAvailableModels();
boolean degraded = agentService.isDegradedMode();
```

---

## Agent JSON Schema (v1.5.2)

```json
{
  "id": "101",
  "name": "Data Analyzer",
  "model": "gpt-4o",
  "temperature": 0.3,
  "responseTimeout": 90000,
  "maxTokens": 4096,
  "resultClass": "DataAnalysisResult",
  "instructions": "You are a data analyst...",
  "description": "Analyzes data and extracts insights",
  "webSearch": false,
  "autonomous": false,
  "maxIterations": 10,
  "functions": []
}
```

**Removed fields (no longer valid):** `assistantIds`, `isOpenAI`, `status`, `retrieval`, `createOnAppStart`, `threadType`

---

**Last Updated**: February 2026
