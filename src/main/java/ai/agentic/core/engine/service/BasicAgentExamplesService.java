package ai.agentic.core.engine.service;

import io.github.yannfavinleveque.agentic.agent.model.AgentResult;
import io.github.yannfavinleveque.agentic.agent.model.ToolExecutor;
import io.github.yannfavinleveque.agentic.agent.service.AgentService;
import io.github.yannfavinleveque.agentic.domain.chat.ChatMessage;
import io.github.yannfavinleveque.agentic.domain.image.ImageRequest;
import io.github.yannfavinleveque.agentic.domain.image.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * BasicAgentExamplesService - Demonstrates basic usage of AgentService v1.5.2
 *
 * This service showcases fundamental AgentService capabilities:
 * 1. Simple agent requests (basic question/answer)
 * 2. Structured outputs (typed responses with custom classes)
 * 3. Conversation management (multi-turn with automatic history)
 * 4. Direct model usage (without agent registration)
 * 5. Embedding generation (text vectorization)
 * 6. Chat completions (stateless conversations)
 * 7. Image generation (DALL-E integration)
 * 8. Autonomous agent mode (tool execution loop)
 *
 * Each method is self-contained and can be called independently.
 */
@Service
public class BasicAgentExamplesService {

    private static final Logger logger = LoggerFactory.getLogger(BasicAgentExamplesService.class);

    @Autowired
    private AgentService agentService;

    // ==================== 1. SIMPLE AGENT REQUEST ====================

    /**
     * Example 1: Simple Agent Request
     *
     * Demonstrates the most basic usage of AgentService:
     * - Send a message to a registered agent
     * - Get a text response
     * - Stateless (one-shot request, no conversation history)
     *
     * @param agentId Agent ID (e.g., "100")
     * @param question User question
     * @return Agent's text response
     */
    public CompletableFuture<String> simpleAgentRequest(String agentId, String question) {
        logger.info("Example 1: Simple Agent Request");
        logger.info("   Agent: {}, Question: {}", agentId, question);

        return agentService.requestAgent(agentId, question)
                .thenApply(result -> {
                    String response = result.getContent();
                    logger.info("   Response received (length: {} chars)", response.length());
                    return response;
                })
                .exceptionally(ex -> {
                    logger.error("   Request failed: {}", ex.getMessage());
                    throw new RuntimeException("Simple agent request failed", ex);
                });
    }

    // ==================== 2. STRUCTURED OUTPUT ====================

    /**
     * Example 2: Structured Output with Typed Response
     *
     * The agent is configured with a resultClass (e.g., "DataAnalysisResult").
     * AgentService automatically applies JSON Schema to enforce response structure.
     *
     * @param agentId Agent ID configured with a result class (e.g., "101")
     * @param dataToAnalyze Data to analyze
     * @return Typed response object implementing AgentResult
     */
    public CompletableFuture<AgentResult> structuredOutputRequest(String agentId, String dataToAnalyze) {
        logger.info("Example 2: Structured Output Request");
        logger.info("   Agent: {}, Input length: {} chars", agentId, dataToAnalyze.length());

        return agentService.requestAgent(agentId, dataToAnalyze)
                .thenApply(typedResponse -> {
                    logger.info("   Structured response received: {}", typedResponse.getClass().getSimpleName());
                    logger.info("   Response: {}", typedResponse);
                    return typedResponse;
                })
                .exceptionally(ex -> {
                    logger.error("   Structured request failed: {}", ex.getMessage());
                    throw new RuntimeException("Structured output request failed", ex);
                });
    }

    // ==================== 3. CONVERSATION MANAGEMENT ====================

    /**
     * Example 3: Multi-Turn Conversation with Automatic History
     *
     * Uses the ConversationManager for automatic message history:
     * - createConversation() creates an in-memory conversation store
     * - requestAgent(agentId, message, conversationId) auto-manages history
     * - deleteConversation() cleans up when done
     *
     * @param agentId Agent ID
     * @param messages List of user messages to send in sequence
     * @return List of responses from the conversation
     */
    public CompletableFuture<List<String>> conversationWithMemory(String agentId, List<String> messages) {
        logger.info("Example 3: Conversational Agent with Automatic History");
        logger.info("   Agent: {}, Messages: {}", agentId, messages.size());

        return CompletableFuture.supplyAsync(() -> {
            String conversationId = null;
            List<String> responses = new java.util.ArrayList<>();

            try {
                // Step 1: Create conversation (in-memory history store)
                conversationId = agentService.createConversation();
                logger.info("   Created conversation: {}", conversationId);

                // Step 2: Send messages sequentially - each sees previous context
                for (int i = 0; i < messages.size(); i++) {
                    String message = messages.get(i);
                    logger.info("   Message {}: {}", i + 1, message);

                    AgentResult result = agentService.requestAgent(agentId, message, conversationId).join();
                    String response = result.getContent();
                    responses.add(response);

                    logger.info("   Response {}: {} chars", i + 1, response.length());
                }

                logger.info("   Conversation completed: {} messages exchanged", messages.size());
                return responses;

            } catch (Exception e) {
                logger.error("   Conversation failed: {}", e.getMessage());
                throw new RuntimeException("Conversation failed", e);
            } finally {
                // Step 3: Clean up conversation
                if (conversationId != null) {
                    boolean deleted = agentService.deleteConversation(conversationId);
                    logger.info("   Conversation cleaned up: {}", deleted);
                }
            }
        });
    }

    // ==================== 4. DIRECT MODEL USAGE ====================

    /**
     * Example 4: Direct Model Usage (No Agent Registration)
     *
     * Use any model directly without defining an agent in JSON.
     * Model suffixes enable special features:
     * - "gpt-4o-websearch" → web search enabled
     * - "gpt-4o-codeinterpreter" → code interpreter enabled
     *
     * @param model Model name (e.g., "gpt-4o", "claude-sonnet-4-5")
     * @param message User message
     * @return Model's text response
     */
    public CompletableFuture<String> directModelRequest(String model, String message) {
        logger.info("Example 4: Direct Model Usage");
        logger.info("   Model: {}, Message: {}", model, message);

        return agentService.requestAgent(model, message)
                .thenApply(result -> {
                    String response = result.getContent();
                    logger.info("   Response received (length: {} chars)", response.length());
                    return response;
                })
                .exceptionally(ex -> {
                    logger.error("   Direct model request failed: {}", ex.getMessage());
                    throw new RuntimeException("Direct model request failed", ex);
                });
    }

    // ==================== 5. EMBEDDING GENERATION ====================

    /**
     * Example 5: Embedding Generation - Text Vectorization
     *
     * @param text Text to vectorize
     * @param model Embedding model (default: "text-embedding-3-small")
     * @return Float array of embeddings
     */
    public CompletableFuture<float[]> generateEmbedding(String text, String model) {
        logger.info("Example 5: Embedding Generation");
        logger.info("   Model: {}, Text length: {} chars", model, text.length());

        return agentService.generateEmbedding(text, model)
                .thenApply(embedding -> {
                    logger.info("   Embedding generated: {} dimensions", embedding.length);
                    return embedding;
                })
                .exceptionally(ex -> {
                    logger.error("   Embedding generation failed: {}", ex.getMessage());
                    throw new RuntimeException("Embedding generation failed", ex);
                });
    }

    public CompletableFuture<float[]> generateEmbeddingDefault(String text) {
        return generateEmbedding(text, "text-embedding-3-small");
    }

    // ==================== 6. CHAT COMPLETION (STATELESS) ====================

    /**
     * Example 6: Chat Completion - Stateless Conversation
     *
     * Direct Chat Completions API (instant response, no history management).
     *
     * @param model Model name (e.g., "gpt-4o")
     * @param systemPrompt System message
     * @param userMessage User message
     * @return Response text
     */
    public CompletableFuture<String> chatCompletion(String model, String systemPrompt, String userMessage) {
        logger.info("Example 6: Chat Completion (Stateless)");
        logger.info("   Model: {}, User message: {}", model, userMessage);

        List<ChatMessage> messages = List.of(
                ChatMessage.SystemMessage.of(systemPrompt),
                ChatMessage.UserMessage.of(userMessage)
        );

        return agentService.chatCompletion(model, messages, 0.7)
                .thenApply(result -> {
                    String response = result.getResult();
                    logger.info("   Chat completion received (length: {} chars)", response.length());
                    return response;
                })
                .exceptionally(ex -> {
                    logger.error("   Chat completion failed: {}", ex.getMessage());
                    throw new RuntimeException("Chat completion failed", ex);
                });
    }

    // ==================== 7. IMAGE GENERATION (DALL-E) ====================

    /**
     * Example 7: Image Generation with DALL-E
     *
     * @param prompt Image description
     * @param model DALL-E model ("dall-e-3")
     * @param size Image size
     * @param quality Image quality (STANDARD or HD)
     * @return Base64-encoded PNG image data
     */
    public CompletableFuture<String> generateImage(
            String prompt, String model, Size size, ImageRequest.Quality quality) {

        logger.info("Example 7: Image Generation with DALL-E");
        logger.info("   Model: {}, Size: {}, Quality: {}", model, size, quality);

        return agentService.generateImage(prompt, model, size, quality)
                .thenApply(base64Image -> {
                    logger.info("   Image generated (base64 length: {} chars)", base64Image.length());
                    return base64Image;
                })
                .exceptionally(ex -> {
                    logger.error("   Image generation failed: {}", ex.getMessage());
                    throw new RuntimeException("Image generation failed", ex);
                });
    }

    public CompletableFuture<String> generateImageDefault(String prompt) {
        return generateImage(prompt, "dall-e-3", Size.X1024, ImageRequest.Quality.STANDARD);
    }

    // ==================== 8. AUTONOMOUS AGENT MODE ====================

    /**
     * Example 8: Autonomous Agent with Tool Execution
     *
     * The agent autonomously calls tools, reflects between calls, and terminates
     * via the auto-injected task_over function. The library manages the full loop.
     *
     * @param agentId Agent ID (must have autonomous=true in JSON config)
     * @param task Task description for the agent
     * @param toolExecutor Implementation that handles tool calls
     * @return Final structured result from the agent
     */
    public CompletableFuture<AgentResult> autonomousAgentRequest(
            String agentId, String task, ToolExecutor toolExecutor) {

        logger.info("Example 8: Autonomous Agent Mode");
        logger.info("   Agent: {}, Task: {}", agentId, task);

        return agentService.requestAgent(agentId, task, toolExecutor)
                .thenApply(result -> {
                    logger.info("   Autonomous task completed: {}", result.getClass().getSimpleName());
                    logger.info("   Result: {}", result);
                    return result;
                })
                .exceptionally(ex -> {
                    logger.error("   Autonomous agent failed: {}", ex.getMessage());
                    throw new RuntimeException("Autonomous agent failed", ex);
                });
    }

    // ==================== 9. STRUCTURED CHAT COMPLETION ====================

    /**
     * Example 9: Structured Chat Completion with Typed Response
     */
    public <T extends AgentResult> CompletableFuture<T> chatCompletionStructured(
            String model, String systemPrompt, String userMessage, Class<T> resultClass) {

        logger.info("Example 9: Structured Chat Completion");
        logger.info("   Model: {}, Result class: {}", model, resultClass.getSimpleName());

        List<ChatMessage> messages = List.of(
                ChatMessage.SystemMessage.of(systemPrompt),
                ChatMessage.UserMessage.of(userMessage)
        );

        return agentService.chatCompletion(model, messages, 0.7, resultClass)
                .thenApply(result -> {
                    logger.info("   Structured chat completion received: {}", result.getClass().getSimpleName());
                    return result;
                })
                .exceptionally(ex -> {
                    logger.error("   Structured chat completion failed: {}", ex.getMessage());
                    throw new RuntimeException("Structured chat completion failed", ex);
                });
    }

    public CompletableFuture<AgentResult> chatCompletionStructuredByName(
            String model, String systemPrompt, String userMessage, String resultClassName) {

        logger.info("Example 9b: Structured Chat Completion (by class name)");

        List<ChatMessage> messages = List.of(
                ChatMessage.SystemMessage.of(systemPrompt),
                ChatMessage.UserMessage.of(userMessage)
        );

        return agentService.chatCompletion(model, messages, 0.7, resultClassName)
                .thenApply(result -> {
                    logger.info("   Structured chat completion received: {}", result.getClass().getSimpleName());
                    return result;
                })
                .exceptionally(ex -> {
                    logger.error("   Structured chat completion failed: {}", ex.getMessage());
                    throw new RuntimeException("Structured chat completion failed", ex);
                });
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Helper: Demonstrates all basic examples in sequence
     */
    public CompletableFuture<String> runAllBasicExamples() {
        logger.info("Running all basic examples...");

        return CompletableFuture.supplyAsync(() -> {
            StringBuilder results = new StringBuilder();
            results.append("=== BASIC AGENTSERVICE EXAMPLES ===\n\n");

            try {
                // Example 1: Simple request
                results.append("1. Simple Agent Request:\n");
                String simple = simpleAgentRequest("100", "What is the capital of France?").join();
                results.append("   Response: ").append(simple.substring(0, Math.min(100, simple.length()))).append("...\n\n");

                // Example 2: Structured output
                results.append("2. Structured Output:\n");
                AgentResult structured = structuredOutputRequest("101", "Analyze these numbers: 10, 20, 30, 40, 50").join();
                results.append("   Type: ").append(structured.getClass().getSimpleName()).append("\n\n");

                // Example 3: Conversation
                results.append("3. Conversational Memory:\n");
                List<String> conversation = conversationWithMemory("100",
                        List.of("Hello, my name is Alice!", "What is my name?")).join();
                results.append("   Messages exchanged: ").append(conversation.size()).append("\n\n");

                // Example 4: Direct model
                results.append("4. Direct Model Usage:\n");
                String direct = directModelRequest("gpt-4o", "What is 2+2?").join();
                results.append("   Response: ").append(direct.substring(0, Math.min(100, direct.length()))).append("...\n\n");

                // Example 5: Embedding
                results.append("5. Embedding Generation:\n");
                float[] embedding = generateEmbeddingDefault("Semantic search example").join();
                results.append("   Dimensions: ").append(embedding.length).append("\n\n");

                // Example 6: Chat completion
                results.append("6. Chat Completion:\n");
                String chat = chatCompletion("gpt-4o",
                        "You are a helpful assistant",
                        "Explain quantum computing in one sentence").join();
                results.append("   Response: ").append(chat.substring(0, Math.min(100, chat.length()))).append("...\n\n");

                results.append("=== ALL BASIC EXAMPLES COMPLETED ===");

            } catch (Exception e) {
                results.append("ERROR: ").append(e.getMessage());
                logger.error("Failed to run all basic examples", e);
            }

            return results.toString();
        });
    }
}
