package com.helmsail.lightborrow.framework.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdHelperTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldGenerateTraceIdWhenNotInMdc() {
        String traceId = TraceIdHelper.getOrGenerateTraceId();
        assertThat(traceId).isNotBlank();
        assertThat(traceId).hasSize(32);
        assertThat(MDC.get("traceId")).isEqualTo(traceId);
    }

    @Test
    void shouldReturnExistingTraceIdFromMdc() {
        MDC.put("traceId", "existing-id-12345");
        assertThat(TraceIdHelper.getOrGenerateTraceId()).isEqualTo("existing-id-12345");
    }

    @Test
    void shouldGenerate32CharUuid() {
        assertThat(TraceIdHelper.generateTraceId()).hasSize(32);
    }

    @Test
    void shouldGenerateUniqueIds() {
        assertThat(TraceIdHelper.generateTraceId()).isNotEqualTo(TraceIdHelper.generateTraceId());
    }
}
