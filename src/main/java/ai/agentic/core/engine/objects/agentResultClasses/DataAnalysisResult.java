package ai.agentic.core.engine.objects.agentResultClasses;

import io.github.yannfavinleveque.agentic.agent.model.AgentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * DataAnalysisResult - Structured output for data analysis operations
 *
 * Used by agent_101_data_analyzer.json to return structured analysis results.
 *
 * This class enforces JSON Schema compliance via AgentService, ensuring
 * the LLM returns predictable, type-safe responses.
 *
 * Example usage:
 * <pre>
 * DataAnalysisResult result = (DataAnalysisResult) agentService.requestAgent("101", "Analyze: 10, 20, 30", null).join();
 * System.out.println("Average: " + result.getKeyMetrics().get("average"));
 * </pre>
 */
@Getter
@Setter
@ToString
public class DataAnalysisResult implements AgentResult {

    /**
     * Brief overview of the analysis (2-3 sentences)
     */
    private String summary;

    /**
     * Key metrics extracted from the data
     * Common keys: "mean", "median", "min", "max", "count", "stddev", etc.
     */
    private Map<String, Double> keyMetrics;

    /**
     * Identified trends in the data
     * Examples: "Increasing trend", "Seasonal pattern detected", "Volatile data"
     */
    private List<String> trends;

    /**
     * Actionable insights derived from the analysis
     * Examples: "Revenue is declining 15% month-over-month", "Peak usage at 2PM"
     */
    private List<String> insights;

    /**
     * Confidence level in the analysis (0.0 to 1.0)
     * - 0.9-1.0: High confidence
     * - 0.7-0.9: Medium confidence
     * - 0.0-0.7: Low confidence (more data needed)
     */
    private Double confidence;

}
