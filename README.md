# Spring Boot AgentService Template

A production-ready Spring Boot starter template with [agentic](https://github.com/Yann-Favin-Leveque/agentic) for multi-LLM orchestration. Build AI-powered applications with OpenAI, Azure OpenAI, and Azure Anthropic (Claude).

## Features

- **Multi-Provider LLM Orchestration**: OpenAI, Azure OpenAI, Azure Anthropic (Claude) with automatic routing
- **Dynamic Agent Management**: Load AI agents from JSON, reload without restart, register programmatically
- **Autonomous Agent Mode**: Agents that call tools, reflect, and iterate autonomously
- **Conversation Management**: Multi-turn conversations with automatic history
- **Structured Outputs**: Type-safe JSON responses with custom result classes
- **Direct Model Usage**: Use any model without agent registration
- **Production-Ready**: Rate limiting, retry logic, async execution, health checks, Prometheus metrics
- **Spring Dotenv**: Automatic `.env` file loading
- **Frontend Included**: Vite + React + TypeScript starter with API proxy

## Tech Stack

- **Backend**: Spring Boot 3.4.2, Java 17, Maven
- **AI/LLM**: agentic-helper v1.5.2 (AgentService)
- **Frontend**: Vite 5, React 18, TypeScript
- **Monitoring**: Spring Actuator, Prometheus metrics
- **Config**: spring-dotenv 4.0.0

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- Node.js 18+ (for frontend, optional)
- OpenAI API key and/or Azure credentials

### 1. Clone the Template

```bash
git clone https://github.com/Yann-Favin-Leveque/agentic-springboot-template.git
cd agentic-springboot-template
```

### 2. Configure API Instances

Copy `.env.example` to `.env` and fill in your credentials:

```bash
cp .env.example .env
```

Minimal `.env` for OpenAI:

```env
LLM_INSTANCES=[{"id":"openai-main","url":"https://api.openai.com","key":"sk-proj-YOUR_KEY","models":"gpt-4o,gpt-4o-mini,text-embedding-3-small,dall-e-3","provider":"openai","enabled":true}]
```

For Azure OpenAI:

```env
LLM_INSTANCES=[{"id":"azure-main","url":"https://YOUR-RESOURCE.openai.azure.com","key":"YOUR_KEY","models":"gpt-4o","provider":"azure-openai","apiVersion":"2024-08-01-preview","enabled":true}]
```

For Azure Anthropic (Claude):

```env
LLM_INSTANCES=[{"id":"claude","url":"https://YOUR-RESOURCE.services.ai.azure.com","key":"YOUR_KEY","models":"claude-sonnet-4-5,claude-haiku-4-5","provider":"azure-anthropic","apiVersion":"2023-06-01","enabled":true}]
```

Multi-provider setup: combine instances in a single JSON array.

### 3. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

Backend runs on: `http://localhost:8080`

### 4. Run Frontend (Optional)

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: `http://localhost:5173` with API proxy to backend.

### 5. Verify

```bash
# Health check
curl http://localhost:8080/api/agents/health

# List loaded agents
curl http://localhost:8080/api/agents

# Test a simple request
curl "http://localhost:8080/api/examples/simple?question=Hello"
```

## Creating Your First Agent

### 1. Define Agent JSON

Create `src/main/resources/agents/agent_200_my_agent.json`:

```json
{
  "id": "200",
  "name": "My Custom Agent",
  "model": "gpt-4o-mini",
  "temperature": 0.7,
  "responseTimeout": 60000,
  "maxTokens": 4096,
  "instructions": "You are an AI assistant that helps with...",
  "description": "Custom agent for my use case"
}
```

### 2. Add Structured Output (Optional)

For typed JSON responses, add a `resultClass` to the agent JSON:

```json
{
  "id": "201",
  "name": "Structured Agent",
  "model": "gpt-4o",
  "resultClass": "MyCustomResult",
  "instructions": "Always respond with structured data..."
}
```

Create the result class in `ai.agentic.core.engine.objects.agentResultClasses`:

```java
@Getter @Setter @ToString
public class MyCustomResult implements AgentResult {
    private String answer;
    private List<String> suggestions;
    private Double confidence;
}
```

### 3. Reload & Use

```bash
# Reload agents without restart
curl -X POST http://localhost:8080/api/agents/reload
```

### 4. Use in Code

```java
@Autowired
private AgentService agentService;

// Simple agent request
AgentResult result = agentService.requestAgent("200", "What is AI?").join();
String answer = result.getContent();

// Structured output (agent with resultClass)
MyCustomResult typed = (MyCustomResult) agentService.requestAgent("201", question).join();

// Direct model usage (no agent registration needed)
AgentResult result = agentService.requestModel("gpt-4o", "What is 2+2?").join();

// Direct model with options (web search, structured output, images, etc.)
AgentResult result = agentService.requestModel("gpt-4o", "Search for latest AI news",
        ModelRequestOptions.withWebSearch()).join();

// Conversation with automatic history
String convId = agentService.createConversation();
agentService.requestAgent("200", "My name is Alice", convId).join();
agentService.requestAgent("200", "What's my name?", convId).join(); // remembers Alice
agentService.deleteConversation(convId);

// Embeddings
float[] embedding = agentService.requestEmbedding("Hello world").join();
float[] embedding = agentService.requestEmbedding("Hello world", "text-embedding-3-small").join();
List<float[]> batch = agentService.requestEmbeddings(List.of("text1", "text2")).join();

// Image generation (DALL-E)
String base64Png = agentService.requestImage("A cat in space").join();

// Chat completion (stateless, no agent needed)
List<ChatMessage> msgs = List.of(
        ChatMessage.SystemMessage.of("You are helpful"),
        ChatMessage.UserMessage.of("Hello"));
DefaultResult result = agentService.chatCompletion("gpt-4o", msgs, 0.7).join();

// Autonomous agent with tool calling
AgentResult result = agentService.requestAgent("207", "Research AI trends", toolExecutor).join();
```

## Agent JSON Configuration

### Valid Fields (v1.5.2)

| Field | Required | Default | Description |
|-------|----------|---------|-------------|
| `id` | Yes | - | Unique identifier |
| `name` | Yes | - | Display name |
| `model` | Yes | - | LLM model name (e.g., `gpt-4o`, `claude-sonnet-4-5`) |
| `instructions` | Yes | - | System prompt |
| `temperature` | No | - | 0.0-2.0 |
| `responseTimeout` | No | 120000 | Max wait time (ms) |
| `maxTokens` | No | 4096 | Max response tokens |
| `resultClass` | No | - | Result POJO class name for structured output |
| `description` | No | - | Agent description |
| `webSearch` | No | false | Enable web search tool |
| `autonomous` | No | false | Enable autonomous agent mode |
| `maxIterations` | No | 10 | Max tool call iterations (autonomous mode) |
| `maxToolTokenOutput` | No | - | Max tokens per tool output (autonomous mode) |
| `functions` | No | [] | Tool/function definitions (autonomous mode) |

### Autonomous Agent Example

```json
{
  "id": "207",
  "name": "Research Agent",
  "model": "gpt-4o",
  "autonomous": true,
  "maxIterations": 10,
  "resultClass": "DataAnalysisResult",
  "instructions": "You are a research agent. Use available tools to gather and analyze data.",
  "functions": [
    {
      "name": "search_data",
      "description": "Search for data matching a query",
      "parameters": {
        "type": "object",
        "properties": {
          "query": { "type": "string", "description": "Search query" }
        },
        "required": ["query"]
      }
    }
  ]
}
```

## API Endpoints

### Agent Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/agents` | List all loaded agents |
| `GET` | `/api/agents/{id}` | Get specific agent config |
| `POST` | `/api/agents/reload` | Reload all agents from JSON |
| `POST` | `/api/agents/{id}/reload` | Reload specific agent |
| `POST` | `/api/agents/register` | Register agent programmatically |
| `GET` | `/api/agents/health` | Agent service health check |

### Example Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/examples/simple` | Simple agent request |
| `GET` | `/api/examples/claude` | Claude agent request |
| `GET` | `/api/examples/direct-model` | Direct model usage |
| `GET` | `/api/examples/chat` | Chat completion |
| `GET` | `/api/examples/embedding` | Embedding generation |
| `GET` | `/api/examples/image` | Image generation (DALL-E) |
| `GET` | `/api/examples/resilient` | Resilient call with retry/fallback |
| `POST` | `/api/examples/structured` | Structured output |
| `POST` | `/api/examples/conversation` | Multi-turn conversation |
| `POST` | `/api/examples/autonomous` | Autonomous agent with tools |
| `POST` | `/api/examples/pipeline` | Sequential pipeline |
| `POST` | `/api/examples/parallel` | Parallel execution |
| `POST` | `/api/examples/conditional` | Conditional orchestration |
| `GET` | `/api/examples/chat-structured` | Structured chat completion |

### Monitoring

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/actuator/health` | Application health |
| `GET` | `/actuator/metrics` | Application metrics |
| `GET` | `/actuator/prometheus` | Prometheus metrics export |

## Configuration

### application.properties

```properties
spring.application.name=AgentServiceTemplate
server.port=8080

# LLM instances (JSON array from .env)
llm.instances=${LLM_INSTANCES:[]}
llm.concurrent.stream.limit.per.instance=${CONCURRENT_STREAM_LIMIT_PER_INSTANCE:15}
llm.max.retries=${LLM_MAX_RETRIES:3}
llm.default.response.timeout=${LLM_DEFAULT_RESPONSE_TIMEOUT:120000}

# Async thread pool
async.pool.size=${ASYNC_POOL_SIZE:30}

# Actuator
management.endpoints.web.exposure.include=health,prometheus,metrics
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `LLM_INSTANCES` | `[]` | JSON array of provider instances |
| `CONCURRENT_STREAM_LIMIT_PER_INSTANCE` | `15` | Concurrent stream limit per instance |
| `LLM_MAX_RETRIES` | `3` | Retry attempts on failure |
| `LLM_DEFAULT_RESPONSE_TIMEOUT` | `120000` | Default timeout (ms) |
| `ASYNC_POOL_SIZE` | `30` | Thread pool size |

## Project Structure

```
├── src/main/java/ai/agentic/core/
│   ├── Application.java                    # Main entry point
│   ├── config/
│   │   ├── AgentServiceConfiguration.java  # AgentService bean config
│   │   └── AsyncConfiguration.java         # Async thread pool
│   ├── startup/
│   │   └── ApplicationStartup.java         # Startup logging
│   ├── controller/
│   │   ├── AgentManagementController.java  # Agent CRUD API
│   │   └── ExamplesController.java         # Example endpoints
│   └── engine/
│       ├── service/
│       │   ├── BasicAgentExamplesService.java      # Basic usage examples
│       │   └── ComplexOrchestrationService.java    # Advanced patterns
│       └── objects/
│           └── agentResultClasses/         # Structured output classes
│               ├── ResultClass.java
│               ├── DataAnalysisResult.java
│               ├── ContentResult.java
│               ├── CodeReviewResult.java
│               └── RAGResult.java
├── src/main/resources/
│   ├── application.properties
│   └── agents/                             # Agent JSON definitions
│       ├── agent_100_example.json
│       ├── agent_101_data_analyzer.json
│       ├── agent_102_content_writer.json
│       ├── agent_103_code_reviewer.json
│       ├── agent_104_rag_assistant.json
│       ├── agent_105_claude_assistant.json
│       ├── agent_106_claude_analyzer.json
│       └── agent_107_autonomous_example.json
├── frontend/                               # React frontend (Vite)
├── .env.example                            # Environment template
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Docker

```bash
# Build and run with Docker Compose
docker compose up --build

# Or build manually
docker build -t agentservice-template .
docker run -p 8080:8080 --env-file .env agentservice-template
```

## Troubleshooting

### Agents Not Loading
- Check `src/main/resources/agents/` folder exists
- Verify JSON files follow naming: `agent_XXX_name.json`
- Check logs for parsing errors
- Run `POST /api/agents/reload` to reload

### API Connection Failed
- Verify API keys in `.env`
- Check network connectivity
- For Azure: verify URL format and API version
- Check `GET /api/agents/health` for degraded mode status

### Model Not Found
- Ensure the model is listed in at least one instance's `models` field
- Check `GET /api/agents/health` → `availableModels`

### Frontend Can't Connect
- Ensure backend is running on port 8080
- Check Vite proxy config in `frontend/vite.config.ts`

## Development

```bash
# Build
mvn clean compile

# Run tests
mvn test

# Run application
mvn spring-boot:run

# Package for production
mvn clean package
```

## Links

- [agentic Library](https://github.com/Yann-Favin-Leveque/agentic)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

## License

MIT License
