package ai.agentic.core.engine.objects.agentResultClasses;

import io.github.yannfavinleveque.agentic.agent.model.AgentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@ToString
public class ResultClass implements AgentResult {
    // getter and setter

    private String result;

}