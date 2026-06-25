package com.helmsail.lightborrow.framework.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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
    void shouldFormatLocalDate() {
        LocalDate date = LocalDate.of(2026, 6, 25);
        assertThat(DateUtil.format(date, "yyyy-MM-dd")).isEqualTo("2026-06-25");
    }

    @Test
    void shouldParseDefaultPattern() {
        LocalDateTime result = DateUtil.parse("2026-06-25 10:30:00");
        assertThat(result).isEqualTo(LocalDateTime.of(2026, 6, 25, 10, 30, 0));
    }

    @Test
    void shouldParseWithCustomPattern() {
        java.time.LocalDate result = DateUtil.parseDate("2026/06/25", "yyyy/MM/dd");
        assertThat(result.getYear()).isEqualTo(2026);
        assertThat(result.getMonthValue()).isEqualTo(6);
        assertThat(result.getDayOfMonth()).isEqualTo(25);
    }

    @Test
    void shouldParseDate() {
        LocalDate result = DateUtil.parseDate("2026-06-25", "yyyy-MM-dd");
        assertThat(result).isEqualTo(LocalDate.of(2026, 6, 25));
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
    void shouldGetNowStrWithPattern() {
        String nowStr = DateUtil.nowStr("yyyy-MM-dd");
        assertThat(nowStr).matches("\\d{4}-\\d{2}-\\d{2}");
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
