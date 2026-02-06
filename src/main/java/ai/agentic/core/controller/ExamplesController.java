package ai.agentic.core.controller;

import ai.agentic.core.engine.service.BasicAgentExamplesService;
import ai.agentic.core.engine.service.ComplexOrchestrationService;
import io.github.yannfavinleveque.agentic.agent.model.AgentResult;
import io.github.yannfavinleveque.agentic.agent.model.ToolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for testing AgentService example services
 *
 * Provides endpoints to test both basic and complex orchestration examples.
 */
@RestController
@RequestMapping("/api/examples")
public class ExamplesController {

    private static final Logger logger = LoggerFactory.getLogger(ExamplesController.class);

    @Autowired
    private BasicAgentExamplesService basicExamples;

    @Autowired
    private ComplexOrchestrationService orchestration;

    // ==================== BASIC EXAMPLES ====================

    /**
     * Test 1: Simple Agent Request
     * GET /api/examples/simple?question=What is AI?
     */
    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> testSimpleRequest(
            @RequestParam(defaultValue = "What is the capital of France?") String question,
            @RequestParam(defaultValue = "100") String agentId) {

        logger.info("Testing simple request with agent {} and question: {}", agentId, question);

        try {
            String response = basicExamples.simpleAgentRequest(agentId, question).join();
            return ResponseEntity.ok(Map.of("success", true, "question", question, "response", response));
        } catch (Exception e) {
            logger.error("Simple request failed", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test Claude Agent
     * GET /api/examples/claude?question=Hello Claude!
     */
    @GetMapping("/claude")
    public ResponseEntity<Map<String, Object>> testClaudeAgent(
            @RequestParam(defaultValue = "Hello Claude! What is 2+2?") String question,
            @RequestParam(defaultValue = "105") String agentId) {

        logger.info("Testing Claude agent {} with question: {}", agentId, question);

        try {
            String response = basicExamples.simpleAgentRequest(agentId, question).join();
            return ResponseEntity.ok(Map.of("success", true, "agentId", agentId, "question", question, "response", response));
        } catch (Exception e) {
            logger.error("Claude request failed", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test 2: Structured Output
     * POST /api/examples/structured
     * Body: {"data": "Analyze: 10, 20, 30, 40, 50"}
     */
    @PostMapping("/structured")
    public ResponseEntity<Map<String, Object>> testStructuredOutput(
            @RequestBody Map<String, String> request) {

        String data = request.getOrDefault("data", "Analyze: 10, 20, 30, 40, 50");
        logger.info("Testing structured output with data: {}", data);

        try {
            AgentResult result = basicExamples.structuredOutputRequest("101", data).join();
            return ResponseEntity.ok(Map.of(
                    "success", true, "input", data,
                    "result", result, "type", result.getClass().getSimpleName()));
        } catch (Exception e) {
            logger.error("Structured output failed", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test 3: Conversation with Memory
     * POST /api/examples/conversation
     * Body: {"messages": ["Hello, my name is Alice!", "What's my name?"]}
     */
    @PostMapping("/conversation")
    public ResponseEntity<Map<String, Object>> testConversation(
            @RequestBody Map<String, List<String>> request) {

        List<String> messages = request.getOrDefault("messages",
                List.of("Hello, my name is John", "What's my name?"));
        logger.info("Testing conversation with {} messages", messages.size());

        try {
            List<String> responses = basicExamples.conversationWithMemory("100", messages).join();
            return ResponseEntity.ok(Map.of("success", true, "messages", messages, "responses", responses));
        } catch (Exception e) {
            logger.error("Conversation failed", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test 4: Direct Model Usage
     * GET /api/examples/direct-model?model=gpt-4o&message=Hello
     */
    @GetMapping("/direct-model")
    public ResponseEntity<Map<String, Object>> testDirectModel(
            @RequestParam(defaultValue = "gpt-4o") String model,
            @RequestParam(defaultValue = "What is 2+2?") String message) {

        logger.info("Testing direct model {} with message: {}", model, message);

        try {
            String response = basicExamples.directModelRequest(model, message).join();
            return ResponseEntity.ok(Map.of("success", true, "model", model, "message", message, "response", response));
        } catch (Exception e) {
            logger.error("Direct model request failed", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test 5: Embedding Generation
     * GET /api/examples/embedding?text=Machine learning
     */
    @GetMapping("/embedding")
    public ResponseEntity<Map<String, Object>> testEmbedding(
            @RequestParam(defaultValue = "Machine learning and AI") String text) {

        logger.info("Testing embedding generation for text: {}", text);

        try {
            float[] embedding = basicExamples.generateEmbeddingDefault(text).join();
            float[] firstFive = new float[]{embedding[0], embedding[1], embedding[2], embedding[3], embedding[4]};
            return ResponseEntity.ok(Map.of(
                    "success", true, "text", text,
                    "dimensions", embedding.length, "firstValues", firstFive));
        } catch (Exception e) {
            logger.error("Embedding generation failed", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test 6: Chat Completion
     * GET /api/examples/chat?question=Explain recursion
     */
    @GetMapping("/chat")
    public ResponseEntity<Map<String, Object>> testChatCompletion(
            @RequestParam(defaultValue = "Explain recursion in one sentence") String question) {

        logger.info("Testing chat completion with question: {}", question);

        try {
            String response = basicExamples.chatCompletion("gpt-4o", "You are a helpful assistant", question).join();
            return ResponseEntity.ok(Map.of("success", true, "question", question, "response", response));
        } catch (Exception e) {
            logger.error("Chat completion failed", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test 7: Image Generation with DALL-E
     * GET /api/examples/image?prompt=A cat wearing sunglasses
     */
    @GetMapping("/image")
    public ResponseEntity<Map<String, Object>> testImageGeneration(
            @RequestParam(defaultValue = "A futuristic city with flying cars") String prompt) {

        logger.info("Testing image generation with prompt: {}", prompt);

        try {
            String base64Image = basicExamples.generateImageDefault(prompt).join();
            return ResponseEntity.ok(Map.of(
                    "success", true, "prompt", prompt,
                    "imageBase64Length", base64Image.length(),
                    "imageBase64", base64Image.substring(0, Math.min(100, base64Image.length())) + "..."));
        } catch (Exception e) {
            logger.error("Image generation failed", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test 8: Autonomous Agent Mode
     * POST /api/examples/autonomous
     * Body: {"task": "Search for information about quantum computing and analyze the results"}
     */
    @PostMapping("/autonomous")
    public ResponseEntity<Map<String, Object>> testAutonomousAgent(
            @RequestBody Map<String, String> request) {

        String task = request.getOrDefault("task", "Search for data about AI trends and calculate key statistics");
        logger.info("Testing autonomous agent with task: {}", task);

        try {
            // Example ToolExecutor that handles the demo tools defined in agent_107
            ToolExecutor toolExecutor = call -> {
                switch (call.getName()) {
                    case "search_data":
                        String query = call.getArgumentsAsMap().get("query").toString();
                        return "Search results for '" + query + "': [" +
                                "{\"title\": \"AI Market Report 2026\", \"value\": 450.5}," +
                                "{\"title\": \"ML Adoption Rate\", \"value\": 78.2}," +
                                "{\"title\": \"Compute Costs Trend\", \"value\": -23.1}" +
                                "]";
                    case "calculate":
                        String expression = call.getArgumentsAsMap().get("expression").toString();
                        return "Calculation result for '" + expression + "': 168.53";
                    default:
                        return "Unknown function: " + call.getName();
                }
            };

            AgentResult result = basicExamples.autonomousAgentRequest("107", task, toolExecutor).join();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("task", task);
            response.put("resultType", result.getClass().getSimpleName());
            response.put("result", result.toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Autonomous agent failed", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test 9: Structured Chat Completion
     * GET /api/examples/chat-structured?model=gpt-4o&message=Analyze this data&resultClass=DataAnalysisResult
     */
    @GetMapping("/chat-structured")
    public ResponseEntity<Map<String, Object>> testChatCompletionStructured(
            @RequestParam String model,
            @RequestParam String message,
            @RequestParam String resultClass) {

        logger.info("Testing structured chat completion with model: {}, resultClass: {}", model, resultClass);

        try {
            AgentResult response = basicExamples.chatCompletionStructuredByName(
                    model,
                    "You are a helpful assistant. Always respond in the requested JSON format.",
                    message,
                    resultClass
            ).join();

            return ResponseEntity.ok(Map.of(
                    "success", true, "model", model,
                    "resultClass", resultClass, "response", response.toString()));
        } catch (Exception e) {
            logger.error("Structured chat completion failed", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== COMPLEX ORCHESTRATION EXAMPLES ====================

    /**
     * Test: Sequential Pipeline
     * POST /api/examples/pipeline
     */
    @PostMapping("/pipeline")
    public ResponseEntity<Map<String, Object>> testSequentialPipeline(
            @RequestBody Map<String, String> request) {

        String data = request.getOrDefault("data", "Sample data: 100, 200, 300");
        try {
            String result = orchestration.sequentialPipeline(data).join();
            return ResponseEntity.ok(Map.of("success", true, "input", data, "output", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test: Parallel Execution
     * POST /api/examples/parallel
     */
    @PostMapping("/parallel")
    public ResponseEntity<Map<String, Object>> testParallelExecution(
            @RequestBody Map<String, Map<String, String>> request) {

        Map<String, String> tasks = request.getOrDefault("tasks", Map.of(
                "task1", "Summarize: AI is transforming industries",
                "task2", "Explain: Quantum computing basics"));

        try {
            Map<String, String> results = orchestration.parallelExecution(tasks).join();
            return ResponseEntity.ok(Map.of("success", true, "tasksCount", tasks.size(), "results", results));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test: Resilient Call
     * GET /api/examples/resilient?message=Test resilience
     */
    @GetMapping("/resilient")
    public ResponseEntity<Map<String, Object>> testResilientCall(
            @RequestParam(defaultValue = "Test message") String message) {

        try {
            String response = orchestration.resilientAgentCall("100", "101", message, 2).join();
            return ResponseEntity.ok(Map.of("success", true, "message", message, "response", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Test: Conditional Orchestration
     * POST /api/examples/conditional
     */
    @PostMapping("/conditional")
    public ResponseEntity<Map<String, Object>> testConditionalOrchestration(
            @RequestBody Map<String, String> request) {

        String input = request.getOrDefault("input", "Analyze sales data");
        try {
            String result = orchestration.conditionalOrchestration(input).join();
            return ResponseEntity.ok(Map.of("success", true, "input", input, "output", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== COMPREHENSIVE TESTS ====================

    @GetMapping("/run-all-basic")
    public ResponseEntity<Map<String, Object>> runAllBasicExamples() {
        try {
            String summary = basicExamples.runAllBasicExamples().join();
            return ResponseEntity.ok(Map.of("success", true, "summary", summary));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/run-all-patterns")
    public ResponseEntity<Map<String, Object>> runAllPatterns() {
        try {
            String summary = orchestration.demonstrateAllPatterns().join();
            return ResponseEntity.ok(Map.of("success", true, "summary", summary));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "basicExamplesService", basicExamples != null ? "available" : "unavailable",
                "orchestrationService", orchestration != null ? "available" : "unavailable"));
    }
}
