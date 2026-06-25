package com.helmsail.lightborrow.framework.util;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringContextHolderTest {

    @Test
    void shouldThrowWhenNotInitialized() {
        assertThatThrownBy(SpringContextHolder::getApplicationContext)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spring 尚未初始化完成");
    }

    @Test
    void shouldGetBeanByType() {
        ApplicationContext mockContext = mock(ApplicationContext.class);
        when(mockContext.getBean(String.class)).thenReturn("testBean");

        SpringContextHolder holder = new SpringContextHolder();
        holder.setApplicationContext(mockContext);

        assertThat(SpringContextHolder.getBean(String.class)).isEqualTo("testBean");
    }

    @Test
    void shouldGetBeanByName() {
        ApplicationContext mockContext = mock(ApplicationContext.class);
        when(mockContext.getBean("myBean", String.class)).thenReturn("namedBean");

        SpringContextHolder holder = new SpringContextHolder();
        holder.setApplicationContext(mockContext);

        assertThat(SpringContextHolder.getBean("myBean", String.class)).isEqualTo("namedBean");
    }

    @Test
    void shouldGetActiveProfile() {
        ApplicationContext mockContext = mock(ApplicationContext.class);
        when(mockContext.getEnvironment()).thenReturn(mock(org.springframework.core.env.Environment.class));
        when(mockContext.getEnvironment().getActiveProfiles()).thenReturn(new String[]{"dev"});

        SpringContextHolder holder = new SpringContextHolder();
        holder.setApplicationContext(mockContext);

        assertThat(SpringContextHolder.getActiveProfile()).isEqualTo("dev");
    }

    @Test
    void shouldGetProperty() {
        ApplicationContext mockContext = mock(ApplicationContext.class);
        when(mockContext.getEnvironment()).thenReturn(mock(org.springframework.core.env.Environment.class));
        when(mockContext.getEnvironment().getProperty("test.key")).thenReturn("testValue");

        SpringContextHolder holder = new SpringContextHolder();
        holder.setApplicationContext(mockContext);

        assertThat(SpringContextHolder.getProperty("test.key")).isEqualTo("testValue");
    }

    @Test
    void shouldGetPropertyWithDefault() {
        ApplicationContext mockContext = mock(ApplicationContext.class);
        when(mockContext.getEnvironment()).thenReturn(mock(org.springframework.core.env.Environment.class));
        when(mockContext.getEnvironment().getProperty("missing.key", "default")).thenReturn("default");

        SpringContextHolder holder = new SpringContextHolder();
        holder.setApplicationContext(mockContext);

        assertThat(SpringContextHolder.getProperty("missing.key", "default")).isEqualTo("default");
    }
}
