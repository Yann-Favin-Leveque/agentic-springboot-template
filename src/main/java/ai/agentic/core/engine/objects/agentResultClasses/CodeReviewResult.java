package ai.agentic.core.engine.objects.agentResultClasses;

import io.github.yannfavinleveque.agentic.agent.model.AgentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * CodeReviewResult - Structured output for code review operations
 *
 * Used by agent_103_code_reviewer.json to return structured code review findings.
 *
 * This class enforces JSON Schema compliance via AgentService, ensuring
 * consistent, actionable code review feedback.
 *
 * Example usage:
 * <pre>
 * String code = "public void process() { String sql = \"SELECT * FROM users WHERE id=\" + userId; }";
 * CodeReviewResult result = (CodeReviewResult) agentService.requestAgent("103",
 *     "Review this code: " + code, null).join();
 * System.out.println("Quality Score: " + result.getOverallScore());
 * result.getIssues().forEach(issue ->
 *     System.out.println("- [" + issue.getSeverity() + "] " + issue.getDescription()));
 * </pre>
 */
@Getter
@Setter
@ToString
public class CodeReviewResult implements AgentResult {

    /**
     * Overall code quality score (0-100)
     * - 90-100: Excellent
     * - 75-89: Good
     * - 60-74: Acceptable
     * - 0-59: Needs improvement
     */
    private Integer overallScore;

    /**
     * List of identified issues in the code
     */
    private List<Issue> issues;

    /**
     * List of suggestions for improvement
     */
    private List<String> suggestions;

    /**
     * Positive aspects of the code (strengths)
     */
    private List<String> strengths;

    /**
     * Overall assessment and recommendations (2-4 sentences)
     */
    private String summary;

    /**
     * Individual code issue or problem
     */
    @Getter
    @Setter
    @ToString
    public static class Issue {
        /**
         * Severity level: "critical", "high", "medium", "low"
         */
        private String severity;

        /**
         * Type of issue: "bug", "security", "performance", "readability", "maintainability", "testing"
         */
        private String type;

        /**
         * Detailed description of the issue
         */
        private String description;

        /**
         * Line number or code location (optional)
         */
        private String location;

        /**
         * Suggested fix or remediation
         */
        private String suggestedFix;
    }

}
