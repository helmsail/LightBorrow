package com.helmsail.lightborrow.framework.util;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.FrameworkException;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdGeneratorTest {

    @Test
    void shouldGenerateId() {
        IdGenerator generator = new IdGenerator(1);
        long id = generator.nextId();
        assertThat(id).isPositive();
    }

    @Test
    void shouldGenerateMonotonicallyIncreasingIds() {
        IdGenerator generator = new IdGenerator(1);
        long prev = generator.nextId();
        for (int i = 0; i < 1000; i++) {
            long current = generator.nextId();
            assertThat(current).isGreaterThan(prev);
            prev = current;
        }
    }

    @Test
    void shouldGenerateUniqueIds() {
        IdGenerator generator = new IdGenerator(1);
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            ids.add(generator.nextId());
        }
        assertThat(ids).hasSize(10000);
    }

    @Test
    void shouldThrowOnInvalidWorkerId() {
        assertThatThrownBy(() -> new IdGenerator(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new IdGenerator(1024))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldParseId() {
        IdGenerator generator = new IdGenerator(42);
        long id = generator.nextId();
        IdGenerator.IdComponents components = IdGenerator.parseId(id);
        assertThat(components.workerId()).isEqualTo(42);
        assertThat(components.sequence()).isBetween(0L, 4095L);
    }

    @Test
    void differentWorkerIdsShouldProduceDifferentIds() {
        IdGenerator gen1 = new IdGenerator(1);
        IdGenerator gen2 = new IdGenerator(2);
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(gen1.nextId());
            ids.add(gen2.nextId());
        }
        assertThat(ids).hasSize(2000);
    }

    @Test
    void parseIdShouldReturnCorrectTimestamp() {
        IdGenerator generator = new IdGenerator(1);
        long before = System.currentTimeMillis();
        long id = generator.nextId();
        long after = System.currentTimeMillis();
        IdGenerator.IdComponents components = IdGenerator.parseId(id);
        assertThat(components.timestamp()).isBetween(before, after);
    }
}
