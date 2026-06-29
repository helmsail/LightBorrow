# ============================================================================
# Docker 多阶段构建
#
# 阶段1：构建前端（Vite + Vue）
# 阶段2：构建后端（Maven + Spring Boot）
# 阶段3：运行（最小 JRE 镜像）
# ============================================================================

# === 阶段1：前端构建 ===
FROM node:20-alpine AS frontend-builder
WORKDIR /build/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN mkdir -p ../bootstrap/src/main/resources/static && \
    npm run build

# === 阶段2：后端构建 ===
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /build
COPY --from=frontend-builder /build/bootstrap/src/main/resources/static bootstrap/src/main/resources/static
COPY .mvn/settings.xml /root/.m2/settings.xml
COPY . .
RUN mkdir -p bootstrap/src/main/resources/docs && \
    cp -r docs/* bootstrap/src/main/resources/docs/ && \
    mvn -pl bootstrap -am -DskipTests clean package -q

# === 阶段3：运行 ===
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-builder /build/bootstrap/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
