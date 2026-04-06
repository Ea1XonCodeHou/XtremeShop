# Xtreme-Shop

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?logo=redis&logoColor=white)

分布式软件设计个人作业项目，目标是完成分布式课程的秒杀商城的基础架构搭建与登录态能力建设。
项目选型了 Spring Boot 3.3.5 作为后端框架，Vue 3.5 作为前端框架，MySQL 8.0 作为关系型数据库，Redis 7.x 作为分布式缓存解决方案。目前处于开发初期阶段，已完成仓库初始化、前后端工程搭建，并实现了基于 Redis 的分布式 Session 登录/注册功能。后续将继续完善秒杀业务逻辑、订单处理等核心功能模块。

## 第一次作业（仓库初始化，前后端规模搭建）

- 完成仓库初始化与目录规范整理，建立前后端分层结构。
- 后端完成多模块 Maven 工程基础搭建，形成 common、pojo、server 的模块划分。
- 前端完成 Vue 工程基础初始化，建立 views、router、api、store、assets 等目录。
- 整理基础配置与开发运行环境，形成可持续迭代的工程骨架。

## 第二次作业（基于 Redis 的分布式 Session 登录/注册）

- 完成登录、注册前后端联通，登录态由后端统一生成并校验。
- 登录状态存储于 Redis，支持分布式场景下多实例共享会话信息。
- 实现单点登录策略，同一用户重复登录时旧会话失效，避免多会话并存。
- 增加会话续期与后端登出清理逻辑，提升会话管理完整性。
- 采用分布式sesion方案，提升系统的可扩展性和高可用性，为后续秒杀业务的用户认证打下基础。
- token过期时间设置为30分钟，登录成功后会将token存储在Redis中，并设置过期时间。当用户再次登录时，如果发现Redis中已经存在该用户的token，则会将旧的token删除，确保同一用户只能有一个有效的登录会话。token就是sessionId，后续秒杀业务中会通过token来校验用户的登录状态和权限。

## 第三次作业（秒杀系统后端设计）

- 完成商家端后端 MVC 体系搭建，包含登录/注册/商家鉴权拦截器，实现与用户端独立的会话管理（`merchant:session:` 前缀隔离）。
- 完成秒杀核心业务建模：设计 `seckill_activity`（活动）与 `seckill_product`（活动商品）双表结构，支持多活动并发。
- 实现秒杀下单接口，采用 Redis 原子自减库存 + 唯一订单 ID 生成策略（时间戳 + Redis 自增序列），防止超卖。
- 引入分布式锁（`SETNX`）保证同一用户在同一活动中不重复下单，结合 Lua 脚本保证校验与扣减的原子性。
- 完成商家端商品 CRUD、秒杀活动管理、订单查看等后台接口；完成用户端商品浏览、优惠券领取与使用、秒杀下单、订单流转等接口。
- 导入全量数据模型（9 张核心表），建立完整的业务数据层与服务层骨架，为后续缓存优化与高并发处理打好基础。

## 第四次作业（缓存预热与缓存问题统一解决）

- 在应用启动阶段引入 `SeckillWarmUpTask`，通过 `ApplicationRunner` 机制自动将进行中的秒杀活动库存预热至 Redis，彻底规避冷启动下缓存击穿问题。
- 统一解决三类经典缓存问题：缓存穿透（空值缓存 + 请求参数合法性校验）、缓存击穿（Redisson 分布式锁单飞重建）、缓存雪崩（随机 TTL 抖动避免同批数据集中过期）。
- 封装 `CacheClient` 通用缓存工具类，提供带逻辑过期的缓存读写方法，支持泛型，与业务层解耦。
- 对秒杀库存扣减全链路替换为 Lua 脚本原子操作，彻底消除 Redis 多命令场景下的竞态条件。
- 完善商家端统计接口（按活动维度的秒杀销量聚合）及用户端个人信息读写接口，补全前后端联通链路。

## 第五次作业（Docker 容器化与完整容器编排）

- 完成前后端与全部中间件的容器化，形成四服务完整编排（`mysql` / `redis` / `backend` / `nginx`），一条命令 `docker compose up -d --build` 即可拉起整套系统。
- 后端采用多阶段 Docker 构建（`maven:3.9-eclipse-temurin-17` 构建 → `eclipse-temurin:17-jre-alpine` 运行），优化 pom 先 COPY 策略充分利用 Docker layer 缓存，减少重复构建时间；最终镜像仅含 JRE，体积比 JDK 镜像缩减约 60%。
- 前端采用多阶段 Docker 构建（`node:20-alpine` 编译 Vue dist → `nginx:alpine` serve），nginx 同时承担静态资源服务与 API 反向代理（`/api/*` → `backend:8080`），统一浏览器访问入口为 `http://localhost`，彻底消除跨域问题。
- 设计 `healthcheck` 机制：MySQL 和 Redis 容器就绪后才启动 backend（`depends_on: condition: service_healthy`），避免 Spring Boot 在数据库/缓存未就绪时启动失败的竞态问题。
- 后端配置实现双模式兼容：`application-dev.yml` 中 `${DB_HOST:127.0.0.1}`、`${REDIS_HOST:127.0.0.1}` 使用带默认值的环境变量占位符，在 IDEA 直接运行时自动使用本地地址，容器化时由 Compose 注入 `DB_HOST=mysql`、`REDIS_HOST=redis` 指向容器服务名，零代码修改切换两种运行方式。
- 容器内激活 `prod` Spring profile（`SPRING_PROFILES_ACTIVE=prod`），关闭 MyBatis SQL 全量日志，与本地开发环境差异化配置。
- 宿主机端口采用隔离策略（mysql 映射 3307、redis 映射 6380）避免与其他项目容器冲突，容器间通信全程走 `xtreme-net` 内部桥接网络，不暴露不必要端口。
