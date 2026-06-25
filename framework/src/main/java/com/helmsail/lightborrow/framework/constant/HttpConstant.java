package com.helmsail.lightborrow.framework.constant;

/**
 * HTTP 相关常量
 */
public final class HttpConstant {

    private HttpConstant() {
    }

    // ========== Header ==========
    /** TraceId 透传用的请求头 */
    public static final String X_TRACE_ID = "X-Trace-Id";

    // ========== MDC Key ==========
    public static final String MDC_TRACE_ID = "traceId";

    // ========== 慢调用阈值 ==========
    /** 慢调用阈值（毫秒），超过此值日志输出 warn */
    public static final int SLOW_THRESHOLD_MILLIS = 5000;
}
