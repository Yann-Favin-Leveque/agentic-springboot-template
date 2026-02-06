# Changelog

All notable changes to the Spring Boot AgentService Template.

---

## [0.2.0] - February 2026

### Major Upgrade: agentic-helper v1.1.5 -> v1.5.2

#### Dependencies
- Spring Boot 3.2.2 -> **3.4.2** (with parent BOM)
- agentic-helper 1.1.5 -> **1.5.2**
- Added **spring-dotenv 4.0.0** (auto `.env` loading)
- Removed unnecessary explicit dependencies (slf4j-api, logback-classic, jackson-databind, bucket4j-core, json)

#### Breaking Changes (from agentic-helper)
- **Assistants API removed**: No more `createAllAgents()`, `createAgent()`, `assistantIds`
- **Stateless Responses API**: All requests now use Responses API instead of Assistants API
- **Conversation management**: `createThread()`/`sendMessageToThread()`/`deleteThread()` replaced by `createConversation()`/`requestAgent(id, msg, conversationId)`/`deleteConversation()`
- **requestAgent() signature**: `requestAgent(id, msg, null)` -> `requestAgent(id, msg)` (no more null context)
- **Provider auto-detection**: `isOpenAI` field removed, provider determined from model name
- **Result extraction**: `result.toString()` -> `result.getContent()`

#### New Features
- **Autonomous Agent Mode**: `requestAgent(id, task, toolExecutor)` with tool execution loop
- **Direct Model Usage**: Use any model name as agent ID without registration
- **Web Search**: `webSearch: true` in agent JSON
- **Programmatic Registration**: `agentService.registerAgent(agent)` via builder
- **Service Info**: `getInstanceCount()`, `getAvailableModels()`, `isDegradedMode()`

#### Agent JSON Changes
- Removed deprecated fields: `assistantIds`, `isOpenAI`, `status`, `retrieval`, `createOnAppStart`, `threadType`
- New fields: `webSearch`, `autonomous`, `maxIterations`, `maxToolTokenOutput`, `functions`
- Added `agent_107_autonomous_example.json`

#### Code Changes
- Deleted `Agent.java` (duplicated library class)
- Deleted `AgentDefinition.java` (no longer needed)
- Rewrote `BasicAgentExamplesService.java` (9 examples including autonomous mode)
- Rewrote `ComplexOrchestrationService.java` (6 orchestration patterns)
- Updated `AgentManagementController.java` (removed create endpoints, added register)
- Updated `ExamplesController.java` (added autonomous, direct-model, conversation endpoints)
- Updated `AgentServiceConfiguration.java` (v1.5.2 builder with maxRetries, defaultResponseTimeout)
- Simplified `ApplicationStartup.java` (logging only, no createAllAgents)
- Renamed async bean from `assistantRequestsExecutor` to `agentRequestsExecutor`

#### Configuration Changes
- Removed `app.startup.init-agents` property
- Removed database configuration (not needed by template)
- Added `openai.max.retries`, `openai.default.response.timeout`
- Rewrote `.env.example` for v1.5.2

#### Infrastructure
- Added Dockerfile (multi-stage build)
- Added docker-compose.yml
- Updated README.md
- Updated all `.claude/` documentation

---

## [0.1.0] - November 2025

### Initial Template
- Spring Boot 3.2.2, Java 17
- agentic-helper v1.1.5
- Basic agent management (CRUD, create on OpenAI)
- Example services (simple, structured, conversation, RAG, image, embeddings)
- Vite + React frontend
- Prometheus metrics

---

**Last Updated**: February 2026
