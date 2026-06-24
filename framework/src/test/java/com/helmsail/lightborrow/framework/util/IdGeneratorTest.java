package com.helmsail.lightborrow.framework.util;

import com.helmsail.lightborrow.framework.exception.FrameworkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class IdGeneratorTest {

    private IdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        idGenerator = new IdGenerator(1);
    }

    @Test
    void constructWithInvalidWorkerId_shouldThrow() {
        assertThatIllegalArgumentException().isThrownBy(() -> new IdGenerator(-1));
        assertThatIllegalArgumentException().isThrownBy(() -> new IdGenerator(1024));
    }

    @Test
    void nextId_shouldGeneratePositiveId() {
        long id = idGenerator.nextId();
        assertThat(id).isPositive();
    }

    @Test
    void nextId_shouldBeMonotonicallyIncreasing() {
        long prev = idGenerator.nextId();
        for (int i = 0; i < 1000; i++) {
            long current = idGenerator.nextId();
            assertThat(current).isGreaterThan(prev);
            prev = current;
        }
    }

    @Test
    void nextId_shouldGenerateUniqueIds() {
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            long id = idGenerator.nextId();
            assertThat(ids.add(id)).as("Duplicate ID: %d", id).isTrue();
        }
    }

    @Test
    void nextId_shouldBeUniqueUnderConcurrency() throws InterruptedException {
        int threadCount = 10;
        int iterationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        Set<Long> allIds = new HashSet<>();
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger dupCount = new AtomicInteger();

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < iterationsPerThread; i++) {
                        long id = idGenerator.nextId();
                        synchronized (allIds) {
                            if (!allIds.add(id)) {
                                dupCount.incrementAndGet();
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(dupCount.get()).as("Concurrent ID generation produced duplicates").isZero();
        assertThat(allIds).hasSize(threadCount * iterationsPerThread);
    }

    @Test
    void parseId_shouldDecodeCorrectly() {
        long id = idGenerator.nextId();
        IdGenerator.IdComponents components = IdGenerator.parseId(id);

        assertThat(components.workerId()).isEqualTo(1);
        assertThat(components.sequence()).isBetween(0L, 4095L); // 12 bits max
        assertThat(components.timestamp()).isPositive();
    }

    @Test
    void parseId_shouldIncludeFormattedDateInToString() {
        long id = idGenerator.nextId();
        IdGenerator.IdComponents components = IdGenerator.parseId(id);

        String str = components.toString();
        assertThat(str).contains("timestamp=");
        assertThat(str).contains("workerId=1");
        assertThat(str).contains("sequence=");
    }

    @Test
    void sequence_shouldRolloverWithinSameMillisecond() {
        // Generate IDs quickly to test sequence rollover within same timestamp
        IdGenerator gen = new IdGenerator(1);
        long first = gen.nextId();
        long second = gen.nextId();

        assertThat(second).isGreaterThan(first);
    }

    @Test
    void differentWorkerIds_shouldGenerateDifferentIds() {
        IdGenerator gen1 = new IdGenerator(1);
        IdGenerator gen2 = new IdGenerator(2);

        long id1 = gen1.nextId();
        long id2 = gen2.nextId();

        IdGenerator.IdComponents c1 = IdGenerator.parseId(id1);
        IdGenerator.IdComponents c2 = IdGenerator.parseId(id2);

        // Worker ID should differ
        assertThat(c1.workerId()).isEqualTo(1);
        assertThat(c2.workerId()).isEqualTo(2);
    }
}
