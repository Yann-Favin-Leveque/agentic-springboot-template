package ai.agentic.core.engine.objects.agentResultClasses;

import io.github.yannfavinleveque.agentic.agent.model.AgentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ContentResult - Structured output for content generation operations
 *
 * Used by agent_102_content_writer.json to return structured written content.
 *
 * This class enforces JSON Schema compliance via AgentService, ensuring
 * consistent content structure from the LLM.
 *
 * Example usage:
 * <pre>
 * ContentResult result = (ContentResult) agentService.requestAgent("102",
 *     "Write a blog post about AI", null).join();
 * System.out.println("Title: " + result.getTitle());
 * System.out.println("Content:\n" + result.getContent());
 * </pre>
 */
@Getter
@Setter
@ToString
public class ContentResult implements AgentResult {

    /**
     * Title or heading for the content
     */
    private String title;

    /**
     * Main body of the generated content
     * May include markdown formatting, paragraphs, lists, etc.
     */
    private String content;

    /**
     * Brief summary or key takeaways (optional)
     * Useful for previews, excerpts, or SEO descriptions
     */
    private String summary;

    /**
     * Tone of the content
     * Examples: "professional", "casual", "technical", "friendly", "formal"
     */
    private String tone;

    /**
     * Approximate word count of the content
     */
    private Integer wordCount;

}
