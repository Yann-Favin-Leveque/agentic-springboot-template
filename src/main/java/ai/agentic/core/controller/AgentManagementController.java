package ai.agentic.core.controller;

import io.github.yannfavinleveque.agentic.agent.core.Agent;
import io.github.yannfavinleveque.agentic.agent.service.AgentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AgentManagementController - REST API for managing AI agents
 *
 * Endpoints:
 * - GET    /api/agents          - List all loaded agents
 * - GET    /api/agents/{id}     - Get specific agent config
 * - POST   /api/agents/reload   - Reload all agent definitions from JSON
 * - POST   /api/agents/{id}/reload - Reload single agent from JSON
 * - POST   /api/agents/register - Register a new agent programmatically
 * - GET    /api/agents/health   - Health check
 */
@RestController
@RequestMapping("/api/agents")
public class AgentManagementController {

    private static final Logger logger = LoggerFactory.getLogger(AgentManagementController.class);

    @Autowired
    private AgentService agentService;

    /**
     * List all loaded agents
     * GET /api/agents
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listAgents() {
        logger.info("Listing all loaded agents");

        try {
            Map<String, Agent> agents = agentService.getAllAgents();

            Map<String, Object> response = new HashMap<>();
            response.put("agents", agents);
            response.put("count", agents.size());

            logger.info("Found {} agent(s)", agents.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to list agents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to list agents", "message", e.getMessage()));
        }
    }

    /**
     * Get a specific agent's configuration
     * GET /api/agents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getAgent(@PathVariable String id) {
        logger.info("Getting agent {}", id);

        try {
            Agent agent = agentService.getAgent(id);

            if (agent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Agent not found", "agentId", id));
            }

            return ResponseEntity.ok(agent);

        } catch (Exception e) {
            logger.error("Failed to get agent {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get agent", "agentId", id, "message", e.getMessage()));
        }
    }

    /**
     * Reload all agent definitions from JSON files
     * POST /api/agents/reload
     */
    @PostMapping("/reload")
    public ResponseEntity<Map<String, Object>> reloadAgents() {
        logger.info("Reloading agent definitions from JSON files");

        try {
            int previousCount = agentService.getAllAgents().size();
            long startTime = System.currentTimeMillis();

            agentService.reloadAgents().join();

            int newCount = agentService.getAllAgents().size();
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Agents reloaded successfully");
            response.put("previousCount", previousCount);
            response.put("newCount", newCount);
            response.put("durationMs", duration);

            logger.info("Agents reloaded in {}ms (before: {}, after: {})", duration, previousCount, newCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to reload agents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reload agents", "message", e.getMessage()));
        }
    }

    /**
     * Reload a specific agent from JSON file
     * POST /api/agents/{id}/reload
     */
    @PostMapping("/{id}/reload")
    public ResponseEntity<Map<String, Object>> reloadAgent(@PathVariable String id) {
        logger.info("Reloading agent {}", id);

        try {
            long startTime = System.currentTimeMillis();
            agentService.reloadAgent(id).join();
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Agent " + id + " reloaded successfully");
            response.put("agentId", id);
            response.put("durationMs", duration);

            logger.info("Agent {} reloaded in {}ms", id, duration);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to reload agent {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reload agent", "agentId", id, "message", e.getMessage()));
        }
    }

    /**
     * Register a new agent programmatically
     * POST /api/agents/register
     * Body: {"id": "200", "name": "My Agent", "model": "gpt-4o", "instructions": "..."}
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerAgent(@RequestBody Map<String, Object> agentConfig) {
        String id = (String) agentConfig.get("id");
        logger.info("Registering agent {}", id);

        try {
            Agent agent = Agent.builder()
                    .id(id)
                    .name((String) agentConfig.get("name"))
                    .model((String) agentConfig.get("model"))
                    .instructions((String) agentConfig.get("instructions"))
                    .temperature(agentConfig.containsKey("temperature") ?
                            ((Number) agentConfig.get("temperature")).doubleValue() : null)
                    .maxTokens(agentConfig.containsKey("maxTokens") ?
                            ((Number) agentConfig.get("maxTokens")).intValue() : null)
                    .build();

            agentService.registerAgent(agent);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Agent " + id + " registered successfully");
            response.put("agentId", id);

            logger.info("Agent {} registered successfully", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to register agent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to register agent", "message", e.getMessage()));
        }
    }

    /**
     * Health check for agent management
     * GET /api/agents/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("agentsLoaded", agentService.getAllAgents().size());
        response.put("instanceCount", agentService.getInstanceCount());
        response.put("availableModels", agentService.getAvailableModels());
        response.put("degradedMode", agentService.isDegradedMode());

        return ResponseEntity.ok(response);
    }
}
