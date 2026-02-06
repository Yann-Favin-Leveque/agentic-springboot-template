package ai.agentic.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "llm.instances=[]"
})
class ApplicationTests {

    @Test
    void contextLoads() {
    }
}
