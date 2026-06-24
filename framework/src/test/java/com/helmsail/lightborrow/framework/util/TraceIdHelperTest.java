package com.helmsail.lightborrow.framework.util;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdHelperTest {

    @AfterEach
    void cleanUp() {
        MDC.clear();
    }

    @Test
    void getCurrentTraceId_shouldReturnNullWhenNotSet() {
        assertThat(TraceIdHelper.getCurrentTraceId()).isNull();
    }

    @Test
    void setTraceId_shouldPutToMDC() {
        TraceIdHelper.setTraceId("abc123");
        assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isEqualTo("abc123");
    }

    @Test
    void setTraceId_shouldIgnoreNullOrBlank() {
        TraceIdHelper.setTraceId(null);
        assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isNull();

        TraceIdHelper.setTraceId("  ");
        assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isNull();
    }

    @Test
    void getCurrentTraceId_shouldReturnSetValue() {
        TraceIdHelper.setTraceId("test-trace-id");
        assertThat(TraceIdHelper.getCurrentTraceId()).isEqualTo("test-trace-id");
    }

    @Test
    void clearTraceId_shouldRemoveFromMDC() {
        TraceIdHelper.setTraceId("test");
        TraceIdHelper.clearTraceId();
        assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isNull();
    }

    @Test
    void clearTraceId_shouldNotAffectOtherMDCKeys() {
        MDC.put("other-key", "other-value");
        TraceIdHelper.setTraceId("test");
        TraceIdHelper.clearTraceId();

        assertThat(MDC.get("other-key")).isEqualTo("other-value");
        assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isNull();
    }
}
