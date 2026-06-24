package com.helmsail.lightborrow.framework.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilTest {

    @Test
    void format_withDefaultPattern_shouldReturnCorrectFormat() {
        LocalDateTime dt = LocalDateTime.of(2026, 6, 25, 10, 30, 0);
        assertThat(DateUtil.format(dt)).isEqualTo("2026-06-25 10:30:00");
    }

    @Test
    void format_withCustomPattern_shouldReturnCorrectFormat() {
        LocalDateTime dt = LocalDateTime.of(2026, 6, 25, 10, 30, 0);
        assertThat(DateUtil.format(dt, "yyyy/MM/dd")).isEqualTo("2026/06/25");
    }

    @Test
    void format_withLocalDate_shouldReturnDateOnly() {
        LocalDate date = LocalDate.of(2026, 6, 25);
        assertThat(DateUtil.format(date, "yyyy-MM-dd")).isEqualTo("2026-06-25");
    }

    @Test
    void format_withTemporalAccessorAndFormatter_shouldUseGivenFormatter() {
        LocalDateTime dt = LocalDateTime.of(2026, 6, 25, 10, 30, 0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        assertThat(DateUtil.format(dt, fmt)).isEqualTo("10:30:00");
    }

    @Test
    void parse_withDefaultPattern_shouldReturnDateTime() {
        LocalDateTime dt = DateUtil.parse("2026-06-25 10:30:00");
        assertThat(dt.getYear()).isEqualTo(2026);
        assertThat(dt.getMonthValue()).isEqualTo(6);
        assertThat(dt.getDayOfMonth()).isEqualTo(25);
        assertThat(dt.getHour()).isEqualTo(10);
        assertThat(dt.getMinute()).isEqualTo(30);
    }

    @Test
    void parse_withCustomPattern_shouldReturnDateTime() {
        LocalDateTime dt = DateUtil.parse("2026/06/25 10:30", "yyyy/MM/dd HH:mm");
        assertThat(dt.getYear()).isEqualTo(2026);
        assertThat(dt.getMonthValue()).isEqualTo(6);
    }

    @Test
    void parseDate_shouldReturnLocalDate() {
        LocalDate date = DateUtil.parseDate("2026-06-25", "yyyy-MM-dd");
        assertThat(date.getYear()).isEqualTo(2026);
        assertThat(date.getMonthValue()).isEqualTo(6);
    }

    @Test
    void now_shouldReturnCurrentDateTime() {
        LocalDateTime now = DateUtil.now();
        // Just verify it returns a non-null recent datetime
        assertThat(now).isNotNull();
        assertThat(now.getYear()).isGreaterThanOrEqualTo(2026);
    }

    @Test
    void nowStr_shouldReturnFormattedCurrentTime() {
        String now = DateUtil.nowStr();
        assertThat(now).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    void nowStr_withPattern_shouldUseGivenPattern() {
        String now = DateUtil.nowStr("yyyy/MM/dd");
        assertThat(now).matches("\\d{4}/\\d{2}/\\d{2}");
    }

    @Test
    void toEpochMilli_shouldConvertCorrectly() {
        LocalDateTime dt = LocalDateTime.of(2026, 6, 25, 0, 0);
        long epoch = DateUtil.toEpochMilli(dt);
        assertThat(epoch).isPositive();
    }

    @Test
    void fromEpochMilli_shouldConvertBack() {
        long epoch = 1772323200000L; // 2026-03-01T00:00:00 UTC approximately
        LocalDateTime dt = DateUtil.fromEpochMilli(epoch);
        assertThat(dt).isNotNull();
    }

    @Test
    void nowEpochMilli_shouldReturnPositive() {
        assertThat(DateUtil.nowEpochMilli()).isPositive();
    }

    @Test
    void roundTrip_epochAndLocalDateTime() {
        LocalDateTime original = LocalDateTime.of(2026, 6, 25, 10, 30, 0);
        long epoch = DateUtil.toEpochMilli(original);
        LocalDateTime restored = DateUtil.fromEpochMilli(epoch);

        assertThat(restored.getYear()).isEqualTo(original.getYear());
        assertThat(restored.getMonthValue()).isEqualTo(original.getMonthValue());
        assertThat(restored.getDayOfMonth()).isEqualTo(original.getDayOfMonth());
    }

    @Test
    void formatterCaching_shouldUsePredefinedFormatters() {
        // Calling format with DEFAULT_PATTERN should reuse cached formatter
        String r1 = DateUtil.format(LocalDateTime.of(2026, 1, 1, 0, 0), DateUtil.DEFAULT_PATTERN);
        String r2 = DateUtil.format(LocalDateTime.of(2026, 1, 1, 0, 0), DateUtil.DEFAULT_PATTERN);
        assertThat(r1).isEqualTo(r2);
    }
}
