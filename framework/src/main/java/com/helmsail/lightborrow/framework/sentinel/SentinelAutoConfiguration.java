package com.helmsail.lightborrow.framework.sentinel;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

import java.util.Collections;

/**
 * Sentinel 限流熔断规则初始化。
 *
 * <p>限流：按用户 ID 对 chat API 限流，每个用户 20 次/60s 窗口。
 * <br>熔断：LLM API 调用，10s 窗口内异常比例 50% 则熔断，10s 后恢复。
 *
 * <p>后续扩容时，只需引入 sentinel-spring-cloud-adapter 并配置
 * spring.cloud.sentinel.datasource.redis 即可启用集群模式，无需修改业务代码。
 */
@Slf4j
@AutoConfiguration
public class SentinelAutoConfiguration {

    /** 聊天 API 限流资源名 */
    public static final String RESOURCE_CHAT_API = "chat:api";

    /** LLM 调用熔断资源名 */
    public static final String RESOURCE_LLM_CHAT = "llm:chat";

    /** Embedding 调用熔断资源名 */
    public static final String RESOURCE_EMBEDDING = "ai:embedding";

    @PostConstruct
    public void init() {
        initFlowRules();
        initDegradeRules();
        initEmbeddingDegradeRules();
        log.info("[Sentinel] 规则初始化完成: flow={}, degrade={}",
                RESOURCE_CHAT_API, RESOURCE_LLM_CHAT);
    }

    private void initFlowRules() {
        // 按用户限流：每个用户 20 次/60s
        ParamFlowRule rule = new ParamFlowRule(RESOURCE_CHAT_API)
                .setParamIdx(0)                     // userId 是 SphU.entry 的第一个参数
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(20.0 / 60)                // QPS = 20次/60秒 ≈ 0.33
                .setDurationInSec(60)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
                .setMaxQueueingTimeMs(500);

        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    private void initDegradeRules() {
        // LLM 熔断：10s 窗口内异常比例 >= 50% 则熔断，10s 后半开
        DegradeRule rule = new DegradeRule(RESOURCE_LLM_CHAT)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(0.5)                      // 50% 异常比例
                .setTimeWindow(10)                  // 熔断持续时间（秒）
                .setMinRequestAmount(5)             // 触发熔断的最小请求数
                .setStatIntervalMs(10_000);         // 统计窗口 10s

        DegradeRuleManager.loadRules(Collections.singletonList(rule));
    }

    private void initEmbeddingDegradeRules() {
        // Embedding 熔断：10s 窗口内异常比例 >= 50% 则熔断，10s 后半开
        DegradeRule rule = new DegradeRule(RESOURCE_EMBEDDING)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(0.5)
                .setTimeWindow(10)
                .setMinRequestAmount(5)
                .setStatIntervalMs(10_000);

        DegradeRuleManager.loadRules(Collections.singletonList(rule));
    }
}
