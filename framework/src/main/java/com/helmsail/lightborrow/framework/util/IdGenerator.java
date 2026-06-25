package com.helmsail.lightborrow.framework.util;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.FrameworkException;
import lombok.extern.slf4j.Slf4j;

/**
 * 雪花算法 ID 生成器。
 * 64bit: 符号位1 + 时间戳41 + workerId10 + 序列号12。
 * 时钟回拨容忍 5ms，超过则抛异常。
 */
@Slf4j
public class IdGenerator {

    // ========== 位分配 ==========
    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_BITS = 10L;

    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    // ========== 位移 ==========
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    // ========== 起始时间戳 (2024-01-01) ==========
    private static final long EPOCH = 1704067200000L;

    // ========== 时钟回拨最大容忍 (5ms) ==========
    private static final long MAX_CLOCK_BACKWARD_MILLIS = 5L;

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * @param workerId 机器 ID (0 - 1023)
     */
    public IdGenerator(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                    String.format("Worker ID must be between 0 and %d, got: %d", MAX_WORKER_ID, workerId));
        }
        this.workerId = workerId;
        log.info("[ID生成器] 初始化完成 workerId={}", workerId);
    }

    public synchronized long nextId() {
        long currentTimestamp = timestamp();

        // 时钟回拨处理
        if (currentTimestamp < lastTimestamp) {
            long offset = lastTimestamp - currentTimestamp;
            if (offset <= MAX_CLOCK_BACKWARD_MILLIS) {
                currentTimestamp = waitUntil(lastTimestamp);
            } else {
                throw new FrameworkException(ErrorCode.ID_GENERATION_FAILED,
                        "Clock moved backwards by " + offset + "ms, refusing to generate ID");
            }
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitUntil(lastTimestamp + 1);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /** 解析 ID 各组成部分（调试用） */
    public static IdComponents parseId(long id) {
        long diff = id >>> TIMESTAMP_SHIFT;
        long timestamp = diff + EPOCH;
        long wid = (id >>> WORKER_ID_SHIFT) & ~(-1L << WORKER_ID_BITS);
        long seq = id & ~(-1L << SEQUENCE_BITS);
        return new IdComponents(timestamp, wid, seq);
    }

    private long timestamp() {
        return System.currentTimeMillis();
    }

    private long waitUntil(long target) {
        long current;
        while ((current = timestamp()) < target) {
            Thread.yield();
        }
        return current;
    }

    /**
     * ID 组成部分（用于调试）
     */
    public record IdComponents(long timestamp, long workerId, long sequence) {
        @Override
        public String toString() {
            return "IdComponents{" +
                    "timestamp=" + timestamp +
                    " (" + DateUtil.format(DateUtil.fromEpochMilli(timestamp)) + ")" +
                    ", workerId=" + workerId +
                    ", sequence=" + sequence +
                    '}';
        }
    }
}
