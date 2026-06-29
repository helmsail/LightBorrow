package com.helmsail.lightborrow.framework.util;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import org.slf4j.MDC;

import java.util.UUID;

/** 供非 Web 上下文（MQ/定时任务）使用。 */
public final class TraceIdHelper {

    private TraceIdHelper() {
    }

    public static String getOrGenerateTraceId() {
        String traceId = MDC.get(HttpConstant.MDC_TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = generateTraceId();
            MDC.put(HttpConstant.MDC_TRACE_ID, traceId);
        }
        return traceId;
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
