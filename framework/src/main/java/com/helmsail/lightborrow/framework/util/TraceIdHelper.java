package com.helmsail.lightborrow.framework.util;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import org.slf4j.MDC;

/**
 * TraceId 辅助工具。供非 Web 上下文（MQ/定时任务）中读写 MDC traceId。
 */
public final class TraceIdHelper {

    private TraceIdHelper() {
    }
    public static String getCurrentTraceId() {
        return MDC.get(HttpConstant.MDC_TRACE_ID);
    }
    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            MDC.put(HttpConstant.MDC_TRACE_ID, traceId);
        }
    }
    public static void clearTraceId() {
        MDC.remove(HttpConstant.MDC_TRACE_ID);
    }
}
