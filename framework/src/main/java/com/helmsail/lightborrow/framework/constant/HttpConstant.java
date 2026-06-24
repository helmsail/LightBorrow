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

    // ========== 默认超时 ==========
    public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    public static final int DEFAULT_READ_TIMEOUT = 10000;
}
