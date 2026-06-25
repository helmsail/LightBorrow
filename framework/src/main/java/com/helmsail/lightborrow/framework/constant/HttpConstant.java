package com.helmsail.lightborrow.framework.constant;

/**
 * HTTP 相关常量。
 */
public final class HttpConstant {

    private HttpConstant() {
    }

    /** TraceId 请求头 */
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    /** MDC 中 TraceId 的 key */
    public static final String MDC_TRACE_ID = "traceId";
}
