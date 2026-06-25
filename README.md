# LightBorrow

基于 Spring Boot 3 的多模块应用框架，遵循国内主流企业级实践。

## 模块结构

| 模块 | 类型 | 说明 |
|------|------|------|
| `bootstrap` | 可部署 JAR | 启动入口、配置、生产就绪（优雅关闭/Actuator/K8s探针） |
| `framework` | 库 JAR | 基础设施自动配置（Web/Redis/JSON/分布式锁/MQ/异步线程池/异常/工具） |
| `ai-infra` | 库 JAR | AI 基础设施（LLM / Embedding / pgvector 向量存储） |

## 快速开始

```bash
# 构建
mvn clean install -DskipTests

# 启动
mvn spring-boot:run -pl bootstrap
```

## 环境要求
- JDK 21+
- Redis（分布式锁 / Stream / 缓存需要）