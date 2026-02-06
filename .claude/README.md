# Spring Boot AgentService Template

## Project Overview
Starter template for Spring Boot applications with agentic-helper integration for multi-LLM orchestration.

## Tech Stack
- **Backend**: Spring Boot 3.4.2, Java 17
- **AI/LLM**: agentic-helper v1.5.2 (AgentService)
- **Frontend**: Vite 5 + React 18 + TypeScript
- **Config**: spring-dotenv 4.0.0
- **Build**: Maven

## Project Structure
```
├── src/main/java/ai/agentic/core/
│   ├── Application.java                    # Main entry point (@EnableAsync, @EnableScheduling)
│   ├── config/
│   │   ├── AgentServiceConfiguration.java  # AgentService bean (v1.5.2 builder)
│   │   └── AsyncConfiguration.java         # Thread pool for async agent requests
│   ├── startup/
│   │   └── ApplicationStartup.java         # Startup logging (agent count, models, instances)
│   ├── controller/
│   │   ├── AgentManagementController.java  # Agent CRUD REST API
│   │   └── ExamplesController.java         # Example endpoints for all features
│   └── engine/
│       ├── service/
│       │   ├── BasicAgentExamplesService.java      # 9 basic usage examples
│       │   └── ComplexOrchestrationService.java    # 6 orchestration patterns
│       └── objects/
│           └── agentResultClasses/         # Structured output POJOs
│               ├── ResultClass.java        # Generic result
│               ├── DataAnalysisResult.java # Data analysis output
│               ├── ContentResult.java      # Content generation output
│               ├── CodeReviewResult.java   # Code review output
│               └── RAGResult.java          # Research/RAG output
├── src/main/resources/
│   ├── application.properties
│   └── agents/                             # 8 agent JSON definitions
├── frontend/                               # React frontend
├── .env.example
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Key APIs (agentic-helper v1.5.2)
- `agentService.requestAgent(agentId, message)` - simple request
- `agentService.requestAgent(agentId, message, conversationId)` - with conversation
- `agentService.requestAgent(agentId, task, toolExecutor)` - autonomous mode
- `agentService.createConversation()` / `deleteConversation(id)` - conversation management
- `agentService.chatCompletion(model, messages, temp)` - stateless chat
- `agentService.generateEmbedding(text, model)` - embeddings
- `agentService.generateImage(prompt, model, size, quality)` - image generation
- `agentService.reloadAgents()` / `reloadAgent(id)` - hot reload
- `agentService.registerAgent(agent)` - programmatic registration

## Quick Start
See main README.md for setup instructions.
