package ai.agentic.core.engine.objects.agentResultClasses;

import io.github.yannfavinleveque.agentic.agent.model.AgentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * RAGResult - Structured output for Retrieval-Augmented Generation operations
 *
 * Used by agent_104_rag_assistant.json to return structured research answers
 * with source attribution from vector store documents.
 *
 * This class enforces JSON Schema compliance via AgentService, ensuring
 * answers include proper citations and source tracking.
 *
 * Example usage:
 * <pre>
 * AgentResult result = agentService.requestAgent("104",
 *     "What are the key features?").join();
 * // result is a RAGResult with answer, sources, quotes, confidence
 * </pre>
 */
@Getter
@Setter
@ToString
public class RAGResult implements AgentResult {

    /**
     * Direct answer to the user's question
     * Should be concise but complete, synthesizing information from sources
     */
    private String answer;

    /**
     * List of sources that contributed to the answer
     */
    private List<Source> sources;

    /**
     * Relevant quotes or excerpts from the documents
     */
    private List<String> quotes;

    /**
     * Confidence in the answer based on source quality (0.0 to 1.0)
     * - 0.9-1.0: High confidence (multiple authoritative sources)
     * - 0.7-0.9: Medium confidence (some sources found)
     * - 0.0-0.7: Low confidence (limited or unclear sources)
     */
    private Double confidence;

    /**
     * Suggested related topics or follow-up questions
     */
    private List<String> relatedTopics;

    /**
     * Source document reference
     */
    @Getter
    @Setter
    @ToString
    public static class Source {
        /**
         * Document name or identifier
         */
        private String documentName;

        /**
         * Section, page, or location within the document
         */
        private String section;

        /**
         * Relevance score to the question (0.0 to 1.0)
         */
        private Double relevance;

        /**
         * Type of source: "documentation", "article", "reference", "example", etc.
         */
        private String type;
    }

}
