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
