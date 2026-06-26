package com.helmsail.lightborrow.framework.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日期工具类。基于 Java 8 Time API，线程安全。
 */
public final class DateUtil {

    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

    /** 缓存用户自定义 pattern 的 DateTimeFormatter，避免频繁创建 */
    private static final Map<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>(8);

    private DateUtil() {
    }

    // ========== 格式化 ==========

    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime.format(getCachedFormatter(pattern));
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_FORMATTER);
    }

    // ========== 解析 ==========

    public static LocalDateTime parse(String dateStr, String pattern) {
        return LocalDateTime.parse(dateStr, getCachedFormatter(pattern));
    }

    public static LocalDateTime parse(String dateStr) {
        return LocalDateTime.parse(dateStr, DEFAULT_FORMATTER);
    }

    // ========== 当前时间 ==========

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static String nowStr() {
        return format(now());
    }

    // ========== 时间戳转换 ==========

    public static long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    // ========== 内部方法 ==========

    private static DateTimeFormatter getCachedFormatter(String pattern) {
        if (DEFAULT_PATTERN.equals(pattern)) {
            return DEFAULT_FORMATTER;
        }
        if (DATE_PATTERN.equals(pattern)) {
            return DATE_FORMATTER;
        }
        if (TIME_PATTERN.equals(pattern)) {
            return TIME_FORMATTER;
        }
        return FORMATTER_CACHE.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
    }
}
