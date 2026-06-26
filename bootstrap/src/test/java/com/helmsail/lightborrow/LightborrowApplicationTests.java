package com.helmsail.lightborrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootTest(classes = LightborrowApplicationTests.TestConfig.class)
class LightborrowApplicationTests {

	@Test
	void contextLoads() {
	}

	/**
	 * 最小化测试配置：仅扫描 framework + ai-infra，排除需要外部基础设施的自动配置。
	 */
	@SpringBootConfiguration
	@EnableAutoConfiguration(excludeName = {
			"com.helmsail.lightborrow.mcp.config.McpAutoConfiguration",
			"com.helmsail.lightborrow.memory.config.MemoryAutoConfiguration",
			"com.helmsail.lightborrow.rag.config.RagAutoConfiguration",
			"com.helmsail.lightborrow.core.config.CoreAutoConfiguration",
			"com.helmsail.lightborrow.gateway.config.GatewayAutoConfiguration",
			"com.helmsail.lightborrow.framework.config.RedisConfig",
			"org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
			"org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration",
			"org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
	})
	@ComponentScan(basePackages = {
			"com.helmsail.lightborrow.framework",
			"com.helmsail.lightborrow.aiinfra"
	}, excludeFilters = {
			@ComponentScan.Filter(type = FilterType.REGEX,
					pattern = "com\\.helmsail\\.lightborrow\\.framework\\.config\\.Redis.*"),
			@ComponentScan.Filter(type = FilterType.REGEX,
					pattern = "com\\.helmsail\\.lightborrow\\.framework\\.redis\\..*")
	})
	static class TestConfig {
	}
}
