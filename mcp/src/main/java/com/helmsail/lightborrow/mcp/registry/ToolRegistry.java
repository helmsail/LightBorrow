package com.helmsail.lightborrow.mcp.registry;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.mcp.annotation.McpParam;
import com.helmsail.lightborrow.mcp.annotation.McpTool;
import com.helmsail.lightborrow.mcp.exception.McpException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册中心。扫描 {@link McpTool} 注解的方法，提供注册、查找、调用能力。
 */
@Slf4j
public class ToolRegistry {

    private final Map<String, ToolDefinition> tools = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;
    private volatile boolean scanned = false;

    public ToolRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener
    public void onContextRefreshed(ContextRefreshedEvent event) {
        if (!scanned) {
            scanTools();
            scanned = true;
        }
    }

    private void scanTools() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
            for (Method method : targetClass.getDeclaredMethods()) {
                McpTool annotation = AnnotationUtils.findAnnotation(method, McpTool.class);
                if (annotation == null) continue;
                registerMethod(annotation, bean, method);
            }
        }
        log.info("[MCP] 工具注册完成，共 {} 个工具: {}", tools.size(), tools.keySet());
    }

    private void registerMethod(McpTool annotation, Object bean, Method method) {
        String name = annotation.name();
        String description = annotation.description();
        Map<String, Object> parameters = buildParametersSchema(method, annotation.params());

        ToolDefinition.ToolExecutor executor = args -> {
            try {
                method.setAccessible(true);
                if (method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == String.class) {
                    Object result = method.invoke(bean, args != null && !args.isEmpty()
                            ? args.values().iterator().next() : "");
                    return result != null ? result.toString() : "";
                }
                Object result = method.invoke(bean, args);
                return result != null ? result.toString() : "";
            } catch (Exception e) {
                log.error("[MCP] 工具执行失败 name={}", name, e);
                throw new McpException(ErrorCode.MCP_TOOL_EXECUTION_FAILED, e, name);
            }
        };

        ToolDefinition def = new ToolDefinition(name, description, parameters, executor);
        tools.put(name, def);
        log.debug("[MCP] 注册工具: {}", name);
    }

    private Map<String, Object> buildParametersSchema(Method method, McpParam[] params) {
        Map<String, Object> schema = new LinkedHashMap<>(4);
        schema.put("type", "object");

        if (params != null && params.length > 0) {
            Map<String, Object> properties = new LinkedHashMap<>(4);
            List<String> required = new ArrayList<>(4);
            for (McpParam param : params) {
                Map<String, Object> prop = new LinkedHashMap<>(4);
                prop.put("type", "string");
                if (!param.desc().isEmpty()) {
                    prop.put("description", param.desc());
                }
                properties.put(param.name(), prop);
                if (param.required()) {
                    required.add(param.name());
                }
            }
            schema.put("properties", properties);
            if (!required.isEmpty()) schema.put("required", required);
            return schema;
        }

        Map<String, Object> properties = new LinkedHashMap<>(4);
        List<String> required = new ArrayList<>(4);

        if (method.getParameterCount() == 1
                && method.getParameterTypes()[0] == String.class) {
            Map<String, Object> prop = new LinkedHashMap<>(2);
            prop.put("type", "string");
            prop.put("description", "输入参数");
            properties.put("input", prop);
            required.add("input");
        } else if (method.getParameterCount() == 1
                && method.getParameterTypes()[0] == Map.class) {
            Map<String, Object> prop = new LinkedHashMap<>(2);
            prop.put("type", "object");
            prop.put("description", "工具参数");
            properties.put("args", prop);
            required.add("args");
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) schema.put("required", required);
        return schema;
    }

    public ToolDefinition getTool(String name) {
        ToolDefinition def = tools.get(name);
        if (def == null) {
            throw new McpException(ErrorCode.MCP_TOOL_NOT_FOUND, name);
        }
        return def;
    }

    public List<ToolDefinition> getToolDefinitions() {
        return List.copyOf(tools.values());
    }

    public String invoke(String name, Map<String, Object> args) {
        ToolDefinition def = getTool(name);
        return def.execute(args);
    }

    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }
}
