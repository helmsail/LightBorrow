package com.helmsail.lightborrow.framework.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Spring Context 持有者。由 JacksonConfig 通过 @Bean 注册。
 */
public class SpringContextHolder implements ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        checkInitialized();
        return applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        checkInitialized();
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        checkInitialized();
        return applicationContext.getBean(name, clazz);
    }

    public static String getActiveProfile() {
        checkInitialized();
        String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
        return profiles.length > 0 ? profiles[0] : null;
    }

    public static String getProperty(String key) {
        checkInitialized();
        return applicationContext.getEnvironment().getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        checkInitialized();
        return applicationContext.getEnvironment().getProperty(key, defaultValue);
    }

    private static void checkInitialized() {
        Assert.state(applicationContext != null, "Spring 尚未初始化完成，请确保在 ApplicationContext 初始化后调用");
    }
}
