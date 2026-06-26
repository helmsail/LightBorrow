package com.helmsail.lightborrow.framework.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilTest {

    @Test
    void shouldFormatDefaultPattern() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 25, 10, 30, 0);
        assertThat(DateUtil.format(now)).isEqualTo("2026-06-25 10:30:00");
    }

    @Test
    void shouldFormatWithCustomPattern() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 25, 10, 30, 0);
        assertThat(DateUtil.format(now, "yyyy/MM/dd")).isEqualTo("2026/06/25");
    }

    @Test
    void shouldParseDefaultPattern() {
        LocalDateTime result = DateUtil.parse("2026-06-25 10:30:00");
        assertThat(result).isEqualTo(LocalDateTime.of(2026, 6, 25, 10, 30, 0));
    }

    @Test
    void shouldParseWithCustomPattern() {
        LocalDateTime result = DateUtil.parse("2026/06/25 10:30:00", "yyyy/MM/dd HH:mm:ss");
        assertThat(result.getYear()).isEqualTo(2026);
        assertThat(result.getMonthValue()).isEqualTo(6);
        assertThat(result.getDayOfMonth()).isEqualTo(25);
    }

    @Test
    void shouldParseDate() {
        LocalDateTime result = DateUtil.parse("2026-06-25 00:00:00");
        assertThat(result.toLocalDate()).isEqualTo(java.time.LocalDate.of(2026, 6, 25));
    }

    @Test
    void shouldGetNow() {
        assertThat(DateUtil.now()).isNotNull();
    }
    
    @Test
    void shouldGetNowStr() {
        String nowStr = DateUtil.nowStr();
        assertThat(nowStr).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    void shouldConvertEpochMilli() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 25, 0, 0, 0);
        long epoch = DateUtil.toEpochMilli(dateTime);
        assertThat(epoch).isPositive();
    }

    @Test
    void shouldConvertFromEpochMilli() {
        long now = System.currentTimeMillis();
        LocalDateTime result = DateUtil.fromEpochMilli(now);
        long roundtrip = DateUtil.toEpochMilli(result);
        assertThat(Math.abs(roundtrip - now)).isLessThanOrEqualTo(100L);
    }
}
