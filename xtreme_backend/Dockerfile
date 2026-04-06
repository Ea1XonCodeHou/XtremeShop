# ============================================================
# XtremeShop Backend — 多阶段 Docker 构建
# Stage 1: maven:3.9-eclipse-temurin-17  → mvn package（构建 jar）
# Stage 2: eclipse-temurin:17-jre-alpine → 运行 jar（精简镜像）
# 构建命令：docker compose build backend
# ============================================================

# ── Stage 1: Build ──────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先单独复制 pom 文件，利用 Docker layer 缓存：
# 只要 pom 不变，依赖下载层就会命中缓存，无需重复下载
COPY pom.xml .
COPY xtreme_common/pom.xml xtreme_common/
COPY xtreme_pojo/pom.xml   xtreme_pojo/
COPY xtreme_server/pom.xml xtreme_server/

# 离线下载全部依赖（pom 未改变时此层命中缓存，构建很快）
RUN mvn dependency:go-offline -B --no-transfer-progress

# 复制源代码
COPY xtreme_common/src xtreme_common/src
COPY xtreme_pojo/src   xtreme_pojo/src
COPY xtreme_server/src xtreme_server/src

# 只打包 xtreme_server（-am 自动编译其依赖模块 common/pojo）
RUN mvn clean package -DskipTests -pl xtreme_server -am --no-transfer-progress

# ── Stage 2: Run ────────────────────────────────────────────
# eclipse-temurin:17-jre-alpine 比 JDK 镜像小 ~60%，仅含运行时
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 复制构建产物
COPY --from=builder /build/xtreme_server/target/xtreme_server-0.0.1-SNAPSHOT.jar app.jar

# 暴露 Spring Boot 默认端口
EXPOSE 8080

# 使用 exec 形式启动，使 Java 进程成为 PID 1，可正确接收 SIGTERM
ENTRYPOINT ["java", "-jar", "app.jar"]
