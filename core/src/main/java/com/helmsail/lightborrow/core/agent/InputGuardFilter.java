package com.helmsail.lightborrow.core.agent;

import lombok.extern.slf4j.Slf4j;

/**
 * 输入安全过滤器。用于检测 Prompt 注入、过长输入、敏感信息。
 */
@Slf4j
public class InputGuardFilter {

    /** Prompt 注入关键词 */
    private static final String[] INJECTION_KEYWORDS = {
            "忽略", "忽略指令", "忽略以上", "ignore", "ignore all",
            "system prompt", "system message", "你不是", "你其实是",
            "假装", "扮演", "role play", "forget", "忘记"
    };

    /** 敏感信息正则 */
    private static final java.util.regex.Pattern SENSITIVE_PATTERN =
            java.util.regex.Pattern.compile(
                    "(1[3-9]\\d{9})|" +       // 手机号
                    "(\\d{17}[0-9Xx])"         // 身份证
            );

    /** 最大输入长度 */
    private static final int MAX_INPUT_LENGTH = 4000;

    /**
     * 检查输入是否安全。
     *
     * @param input 用户输入
     * @return GuardResult 安全结果
     */
    public GuardResult check(String input) {
        if (input == null || input.isBlank()) {
            return GuardResult.passed(input);
        }

        // 检查注入关键词
        for (String keyword : INJECTION_KEYWORDS) {
            if (input.contains(keyword)) {
                log.warn("[Guard] 检测到 Prompt 注入关键词: '{}'", keyword);
                return GuardResult.rejected("输入包含不安全内容，请重新描述你的需求。");
            }
        }

        // 敏感信息检测并脱敏
        String sanitized = input;
        java.util.regex.Matcher matcher = SENSITIVE_PATTERN.matcher(input);
        if (matcher.find()) {
            log.warn("[Guard] 检测到敏感信息，已自动脱敏");
            sanitized = matcher.replaceAll("***");
        }

        // 过长截断
        if (sanitized.length() > MAX_INPUT_LENGTH) {
            log.warn("[Guard] 输入过长 ({}), 截断至 {}", sanitized.length(), MAX_INPUT_LENGTH);
            sanitized = sanitized.substring(0, MAX_INPUT_LENGTH);
        }

        return GuardResult.passed(sanitized);
    }

    /** 安全检查结果 */
    public record GuardResult(boolean passed, String sanitizedInput, String rejectReason) {

        public static GuardResult passed(String input) {
            return new GuardResult(true, input, null);
        }

        public static GuardResult rejected(String reason) {
            return new GuardResult(false, null, reason);
        }
    }
}
