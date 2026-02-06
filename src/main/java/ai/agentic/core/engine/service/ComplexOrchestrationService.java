package ai.agentic.core.engine.service;

import io.github.yannfavinleveque.agentic.agent.model.AgentResult;
import io.github.yannfavinleveque.agentic.agent.service.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * ComplexOrchestrationService - Advanced AgentService Orchestration Patterns
 *
 * This service demonstrates complex multi-agent workflows:
 * 1. Sequential agent calls with data manipulation
 * 2. Parallel agent execution (CompletableFuture.allOf)
 * 3. Agent-to-agent data passing workflows
 * 4. Multi-turn conversation with context
 * 5. Error handling and retry patterns
 * 6. Conditional orchestration (decision trees)
 */
@Service
public class ComplexOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(ComplexOrchestrationService.class);

    @Autowired
    private AgentService agentService;

    // ==================== 1. SEQUENTIAL PIPELINE ====================

    /**
     * Pattern 1: Sequential Pipeline - Data Analysis -> Content Generation
     *
     * Agent 1 analyzes data, result is transformed, Agent 2 generates content.
     */
    public CompletableFuture<String> sequentialPipeline(String rawData) {
        logger.info("Pattern 1: Sequential Pipeline");
        logger.info("   Input: {} chars", rawData.length());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Step 1: Data analysis with structured output
                logger.info("   Step 1: Analyzing data with agent 101 (Data Analyzer)");
                AgentResult analysisResult = agentService.requestAgent(
                        "101",
                        "Analyze this data and identify key metrics: " + rawData
                ).join();
                logger.info("   Analysis complete: {}", analysisResult);

                // Step 2: Data transformation
                logger.info("   Step 2: Transforming analysis results");
                String transformedData = "Key findings:\n" + analysisResult +
                        "\n\nPlease create a professional summary highlighting the most important insights.";

                // Step 3: Content generation
                logger.info("   Step 3: Generating content with agent 102 (Content Writer)");
                AgentResult contentResult = agentService.requestAgent(
                        "102",
                        "Based on this analysis, write a professional summary: " + transformedData
                ).join();
                String finalContent = contentResult.getContent();
                logger.info("   Content generated: {} chars", finalContent.length());

                return finalContent;

            } catch (Exception e) {
                logger.error("   Pipeline failed: {}", e.getMessage());
                throw new RuntimeException("Sequential pipeline failed", e);
            }
        });
    }

    // ==================== 2. PARALLEL EXECUTION ====================

    /**
     * Pattern 2: Parallel Execution - Concurrent Agent Tasks
     *
     * Execute multiple agents simultaneously using CompletableFuture.allOf.
     */
    public CompletableFuture<Map<String, String>> parallelExecution(Map<String, String> tasks) {
        logger.info("Pattern 2: Parallel Agent Execution");
        logger.info("   Tasks: {}", tasks.size());

        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, CompletableFuture<AgentResult>> futures = new HashMap<>();

                for (Map.Entry<String, String> task : tasks.entrySet()) {
                    logger.info("   Launching task '{}': {}", task.getKey(), task.getValue());
                    futures.put(task.getKey(), agentService.requestAgent("100", task.getValue()));
                }

                // Wait for ALL tasks to complete
                CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).join();
                logger.info("   All tasks completed!");

                // Collect results
                Map<String, String> results = new HashMap<>();
                for (Map.Entry<String, CompletableFuture<AgentResult>> entry : futures.entrySet()) {
                    String result = entry.getValue().join().getContent();
                    results.put(entry.getKey(), result);
                    logger.info("   Task '{}' result: {} chars", entry.getKey(), result.length());
                }

                return results;

            } catch (Exception e) {
                logger.error("   Parallel execution failed: {}", e.getMessage());
                throw new RuntimeException("Parallel execution failed", e);
            }
        });
    }

    // ==================== 3. MULTI-AGENT WORKFLOW ====================

    /**
     * Pattern 3: Multi-Agent Workflow - Specialized Agent Chain
     *
     * Analyzer -> Reviewer -> Writer pipeline.
     */
    public CompletableFuture<String> multiAgentWorkflow(String initialInput) {
        logger.info("Pattern 3: Multi-Agent Workflow (Specialized Chain)");
        logger.info("   Input: {} chars", initialInput.length());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Agent 1: Analyze
                AgentResult analysisResult = agentService.requestAgent(
                        "101", "Extract and structure key information from: " + initialInput
                ).join();

                // Agent 2: Review
                AgentResult reviewResult = agentService.requestAgent(
                        "103", "Review this analysis for accuracy and completeness: " + analysisResult
                ).join();

                // Agent 3: Write
                AgentResult finalResult = agentService.requestAgent(
                        "102",
                        "Based on this analysis and review, create a professional document:\n" +
                        "Analysis: " + analysisResult + "\n" +
                        "Review: " + reviewResult.getContent()
                ).join();

                return finalResult.getContent();

            } catch (Exception e) {
                logger.error("   Multi-agent workflow failed: {}", e.getMessage());
                throw new RuntimeException("Multi-agent workflow failed", e);
            }
        });
    }

    // ==================== 4. MULTI-TURN CONVERSATION ====================

    /**
     * Pattern 4: Multi-Turn Conversation with Context
     *
     * Uses ConversationManager for persistent context across multiple exchanges.
     */
    public CompletableFuture<List<String>> conversationalWorkflow(
            String agentId, List<String> questions) {

        logger.info("Pattern 4: Multi-Turn Conversational Workflow");
        logger.info("   Agent: {}, Questions: {}", agentId, questions.size());

        return CompletableFuture.supplyAsync(() -> {
            String conversationId = null;
            List<String> responses = new ArrayList<>();

            try {
                conversationId = agentService.createConversation();
                logger.info("   Created conversation: {}", conversationId);

                for (int i = 0; i < questions.size(); i++) {
                    String question = questions.get(i);
                    logger.info("   Question {}/{}: {}", i + 1, questions.size(), question);

                    AgentResult result = agentService.requestAgent(agentId, question, conversationId).join();
                    String response = result.getContent();
                    responses.add(response);
                    logger.info("   Response {}: {} chars", i + 1, response.length());
                }

                return responses;

            } catch (Exception e) {
                logger.error("   Conversational workflow failed: {}", e.getMessage());
                throw new RuntimeException("Conversational workflow failed", e);
            } finally {
                if (conversationId != null) {
                    agentService.deleteConversation(conversationId);
                }
            }
        });
    }

    // ==================== 5. RESILIENT AGENT CALL ====================

    /**
     * Pattern 5: Resilient Agent Call with Custom Retry and Fallback
     */
    public CompletableFuture<String> resilientAgentCall(
            String agentId, String fallbackAgentId, String message, int maxRetries) {

        logger.info("Pattern 5: Resilient Agent Call with Fallback");
        logger.info("   Primary: {}, Fallback: {}, Max retries: {}", agentId, fallbackAgentId, maxRetries);

        return CompletableFuture.supplyAsync(() -> {
            int attempt = 0;
            Exception lastException = null;

            while (attempt < maxRetries) {
                try {
                    attempt++;
                    logger.info("   Attempt {}/{} with primary agent {}", attempt, maxRetries, agentId);

                    AgentResult result = agentService.requestAgent(agentId, message).join();
                    logger.info("   Success on attempt {}", attempt);
                    return result.getContent();

                } catch (Exception e) {
                    lastException = e;
                    logger.warn("   Attempt {} failed: {}", attempt, e.getMessage());

                    if (attempt < maxRetries) {
                        long delay = (long) (1000 * Math.pow(2, attempt - 1));
                        logger.info("   Waiting {}ms before retry...", delay);
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Retry interrupted", ie);
                        }
                    }
                }
            }

            // Fallback
            logger.warn("   Primary agent {} failed after {} attempts, trying fallback {}", agentId, maxRetries, fallbackAgentId);
            try {
                AgentResult result = agentService.requestAgent(fallbackAgentId, message).join();
                return "Response from fallback agent:\n" + result.getContent();
            } catch (Exception e) {
                throw new RuntimeException("All retry attempts exhausted. Last error: " + lastException.getMessage(), lastException);
            }
        });
    }

    // ==================== 6. CONDITIONAL ORCHESTRATION ====================

    /**
     * Pattern 6: Intelligent Routing - Decision Tree Orchestration
     */
    public CompletableFuture<String> conditionalOrchestration(String userInput) {
        logger.info("Pattern 6: Conditional Orchestration (Decision Tree)");
        logger.info("   Input: {}", userInput);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Step 1: Classify input
                AgentResult classResult = agentService.requestAgent(
                        "100",
                        "Classify this input into ONE word (data_analysis, content_creation, code_review, or general): " + userInput
                ).join();
                String classification = classResult.getContent();
                logger.info("   Classification: {}", classification);

                // Step 2: Route to specialized agent
                String routedAgentId;
                String routedAgentName;

                if (classification.toLowerCase().contains("data_analysis")) {
                    routedAgentId = "101";
                    routedAgentName = "Data Analyzer";
                } else if (classification.toLowerCase().contains("content_creation")) {
                    routedAgentId = "102";
                    routedAgentName = "Content Writer";
                } else if (classification.toLowerCase().contains("code_review")) {
                    routedAgentId = "103";
                    routedAgentName = "Code Reviewer";
                } else {
                    routedAgentId = "100";
                    routedAgentName = "General Agent";
                }

                logger.info("   Routing to {} ({})", routedAgentName, routedAgentId);

                // Step 3: Process with specialized agent
                AgentResult result = agentService.requestAgent(routedAgentId, userInput).join();
                return String.format("Handled by %s:\n\n%s", routedAgentName, result.getContent());

            } catch (Exception e) {
                logger.error("   Conditional orchestration failed: {}", e.getMessage());
                throw new RuntimeException("Conditional orchestration failed", e);
            }
        });
    }

    // ==================== COMPREHENSIVE DEMO ====================

    public CompletableFuture<String> demonstrateAllPatterns() {
        logger.info("Demonstrating All Complex Orchestration Patterns");

        return CompletableFuture.supplyAsync(() -> {
            StringBuilder results = new StringBuilder();
            results.append("=== COMPLEX ORCHESTRATION PATTERNS ===\n\n");

            try {
                results.append("1. Sequential Pipeline:\n");
                String pipeline = sequentialPipeline("Sample data: 100, 200, 300").join();
                results.append("   Result: ").append(pipeline.substring(0, Math.min(100, pipeline.length()))).append("...\n\n");

                results.append("2. Parallel Execution:\n");
                Map<String, String> tasks = Map.of(
                        "task1", "Summarize: AI is transforming industries",
                        "task2", "Explain: Quantum computing basics",
                        "task3", "Describe: Future of technology"
                );
                Map<String, String> parallelResults = parallelExecution(tasks).join();
                results.append("   Tasks completed: ").append(parallelResults.size()).append("\n\n");

                results.append("3. Multi-Agent Workflow:\n");
                String workflow = multiAgentWorkflow("Complex business data requiring analysis").join();
                results.append("   Workflow output: ").append(workflow.substring(0, Math.min(100, workflow.length()))).append("...\n\n");

                results.append("5. Resilient Agent Call:\n");
                String resilient = resilientAgentCall("100", "101", "Test question", 2).join();
                results.append("   Resilient call: ").append(resilient.substring(0, Math.min(100, resilient.length()))).append("...\n\n");

                results.append("6. Conditional Orchestration:\n");
                String conditional = conditionalOrchestration("Analyze these sales numbers: 1000, 2000, 3000").join();
                results.append("   Routed result: ").append(conditional.substring(0, Math.min(100, conditional.length()))).append("...\n\n");

                results.append("=== ALL PATTERNS DEMONSTRATED ===");

            } catch (Exception e) {
                results.append("ERROR: ").append(e.getMessage());
                logger.error("Failed to demonstrate all patterns", e);
            }

            return results.toString();
        });
    }
}
