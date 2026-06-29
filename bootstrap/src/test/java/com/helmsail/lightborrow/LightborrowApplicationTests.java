package com.helmsail.lightborrow;

import com.helmsail.lightborrow.core.agent.AgentLoop;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LightborrowApplicationTests {

	@MockBean
	private AgentLoop agentLoop;

	@Test
	void contextLoads() {
	}
}
