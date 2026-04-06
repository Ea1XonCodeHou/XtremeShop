# XtremeShop 开发路线图（更新版）

> **当前状态**：用户端分布式 Session 登录注册已完成
> **两套设计系统**：
> - 用户端 → **Amber Velocity**（暖橙 #ff6600，活力电商风）
> - 商家端 → **Precision Architect**（专业蓝 #005daa，企业后台风）

---

## 数据库表（9 张，已设计完毕）

| 表名 | 文件 | 状态 |
|------|------|------|
| `user` | xtreme_user.sql | ✅ 已建 |
| `merchant` | merchant.sql | ✅ 待执行 |
| `category` | category.sql | ✅ 待执行 |
| `product` | product.sql | ✅ 待执行 |
| `seckill_activity` / `seckill_product` | seckill.sql | ✅ 待执行 |
| `coupon` / `user_coupon` | coupon.sql | ✅ 待执行 |
| `order` | order.sql | ✅ 待执行 |

> **立即执行**：按顺序导入全部 SQL 文件到 xtreme_shop 数据库

---

## STEP 1：后端工具层（当前任务）

### 1-A  `AliOssUtil`
- 位置：`xtreme_common/utils/AliOssUtil.java`
- 功能：`uploadFile(fileName, inputStream)` → 返回文件公网 URL
- 功能：`deleteFile(fileUrl)` → 按 URL 删除 OSS 文件
- 配置：KEY/SECRET/BUCKET/ENDPOINT 写入 `application-dev.yml`，通过 `@ConfigurationProperties` 注入

### 1-B  `RedisIdWorker`
- 位置：`xtreme_common/utils/RedisIdWorker.java`
- 设计：参考雪花算法思路，**不需要机器码**
  ```
  64 bit ID = 符号位(1) + 时间戳秒(31) + Redis自增序列(32)
  ```
- 用途：全局唯一订单号、秒杀券 ID
- 使用：`redisIdWorker.nextId("order")` → 每种业务独立计数

---

## STEP 2：商家端后端 MVC

### 2-A  核心代码
- `Merchant` 实体类（xtreme_pojo）
- `MerchantMapper`（Java 注解风格，对齐 UserMapper）
- `MerchantService` 接口 + `MerchantServiceImpl`
  - `register(phone, password, name)` → 生成商家 ID（RedisIdWorker），加盐加密注册
  - `login(phone, password)` → 验证密码，生成 token 存 Redis（key 前缀区分 `merchant:session:`）
  - `logout(token)` → 清除 Redis session
- `MerchantController`：`/merchant/register`、`/merchant/login`、`/merchant/logout`

### 2-B  商家鉴权
- `MerchantInterceptor`：从请求头读取 `X-Merchant-Token`，Redis 验证，存入 `BaseContext`
- `WebMvcConfig` 注册商家拦截器（路径 `/merchant/**`，放行登录/注册）

---

## STEP 3：前端页面重构（双端分离）

### 3-A  用户端合并登录注册 → 单页切换
- 文件：`src/views/auth/UserAuth.vue`（合并原 Login + Register）
- 交互：底部「没有账号？立即注册 / 已有账号？去登录」切换表单
- 风格：**Amber Velocity**（现有橙色系，保持不变）
- 路由：`/login` 和 `/register` 均重定向至 `/auth/user`

### 3-B  商家端登录注册 → 单页切换（新建）
- 文件：`src/views/auth/MerchantAuth.vue`
- 风格：**Precision Architect**（专业蓝 #005daa）
  - 背景：深蓝渐变（`#0a1628` → `#0f2040`）左侧品牌区 + 白色卡片右侧表单
  - 字体：Plus Jakarta Sans（与商家后台统一）
  - 按钮：蓝色渐变，6px 圆角
- 入口：首页导航栏右侧「我是商家」按钮 → 跳转 `/auth/merchant`
- 路由：`/auth/merchant`

### 3-C  路由配置更新
```
/ → /home
/auth/user      UserAuth.vue    (用户登录注册)
/auth/merchant  MerchantAuth.vue (商家登录注册)
/merchant/dashboard  MerchantDashboard.vue  (需鉴权)
/merchant/marketing  MerchantMarketing.vue  (需鉴权)
/merchant/settings   MerchantSettings.vue   (需鉴权)
```

---

## STEP 4：商家后台前端（参考 reference 原型）

> 原型文件已就绪：`xtreme_frontend/refrence/stitch_xtreme_shop_design_requirements_prd/`

### 4-A  Dashboard（仪表盘）→ 参考 `dashboard/screen.png`
- 店铺数据概览（总销售额、订单数、商品数）
- 近期订单快览

### 4-B  商品管理 → 商家发布/编辑商品
- 商品列表（分类筛选、上下架操作）
- 发布商品表单（名称/价格/库存/分类/封面图上传 → AliOssUtil）

### 4-C  营销中心 → 参考 `marketing_center/screen.png`（核心！）
- 创建秒杀活动，选择商品，设置秒杀价和秒杀库存
- 发布优惠券（满减/折扣），设置发行量和有效期

### 4-D  店铺设置 → 参考 `store_settings/screen.png`
- 修改店铺名称、Logo 上传（AliOssUtil）

---

## STEP 5：秒杀核心后端（重点）

> 与你共同构思后再动手，以下为初步方案

### 流程设计
```
商家在营销中心发布秒杀活动
  ↓ 活动开始时，后端将库存预加载至 Redis

用户端点击「立即抢购」
  ↓ 1. 拦截器鉴权（必须登录）
  ↓ 2. Redis SISMEMBER 查重（seckill:bought:{activityId}:{userId}）
  ↓ 3. Redis DECR 预减库存（seckill:stock:{seckillProductId}）
  ↓ 4. 库存不足 → 直接返回「已抢完」
  ↓ 5. 库存充足 → 异步消息落单（RabbitMQ 或线程池简化版）
  ↓ 前端轮询订单状态（order:status:{orderNo}）
```

### Redis Key 规范
| Key | 类型 | 说明 |
|-----|------|------|
| `seckill:stock:{seckillProductId}` | String | 秒杀库存，DECR 原子操作 |
| `seckill:bought:{activityId}:{userId}` | Set | 已购用户集合，SADD 防重 |
| `coupon:stock:{couponId}` | String | 券库存，DECR 原子操作 |
| `coupon:got:{couponId}:{userId}` | String | 防重复领券 |
| `order:status:{orderNo}` | String | 订单状态缓存（轮询） |
| `session:{token}` | String | 用户 session（已有） |
| `merchant:session:{token}` | String | 商家 session |

---

## STEP 6：用户端秒杀页前端

- 秒杀商品列表（倒计时 + 秒杀价 + 库存进度条）
- 抢购弹窗（选择优惠券 → 确认金额 → 下单）
- 抢购结果页（成功/失败 → 订单号）
- 「我的订单」页（订单状态轮询）
- 「我的优惠券」页

---

## 当前最近任务清单

```
[ STEP 1 - 现在开始 ]
□ 1. 编写 AliOssUtil（含 yml 配置）
□ 2. 编写 RedisIdWorker

[ STEP 2 - 紧接 ]
□ 3. Merchant 实体 + Mapper + Service + Controller
□ 4. MerchantInterceptor + WebMvcConfig 更新

[ STEP 3 - 然后 ]
□ 5. UserAuth.vue（合并用户登录注册）
□ 6. MerchantAuth.vue（商家登录注册，蓝色系）
□ 7. 首页加「我是商家」入口 + 路由更新

[ STEP 4 - 之后共同构思 ]
□ 8. 商家后台 Dashboard + 商品管理 + 营销中心
□ 9. 秒杀后端核心（共同设计确认后再动手）
```


① 后端 Entity + Mapper（SeckillActivity, SeckillProduct）
② 后端 Service + Controller 接口1-5（商家端）
③ 后端接口6（公开接口，首页用）
④ 前端 MarketingCenter.vue 秒杀 Tab 完整实现
⑤ 前端 Home/index.vue 替换真实数据
⑥ 联调，按 T1-T10 逐项验证


Phase 1 — 后端基础层
  ① Order 实体 + Mapper
  ② SeckillOrderDTO / SeckillOrderVO / OrderVO
  ③ ThreadPoolConfig
  ④ seckill.lua

Phase 2 — 后端核心业务层
  ⑤ OrderService 接口
  ⑥ OrderServiceImpl（Lua 执行 + 异步落库 + 库存预热）
  ⑦ SeckillOrderController（3 个接口）
  ⑧ WebMvcConfig 拦截器注册

Phase 3 — 前端
  ⑨ Home/index.vue 按钮鉴权改造
  ⑩ SeckillOrder.vue 下单页
  ⑪ router/index.js 路由

3月31日：
Phase A — 前端页面创建
步骤	文件	说明
A1	src/views/Seckill/OrderConfirm.vue	秒杀下单确认页：展示商品信息（图片/名称/秒杀价）、填写收货人/手机/地址、显示应付金额、"确认下单"按钮
A2	src/views/Seckill/PayMock.vue	Mock 支付页：显示订单号+金额，"模拟支付"按钮，点击后调后端接口更新订单状态为已支付，跳转支付结果页
A3	src/views/Seckill/PayResult.vue	支付结果页：成功/失败展示，提供"查看订单"和"返回首页"入口
A4	src/views/Seckill/MyOrders.vue	我的订单列表页：调 GET /seckill/orders，分页展示订单卡片（商品图、名称、价格、状态）
Phase B — 路由注册
步骤	文件	说明
B1	src/router/index.js	新增 4 条路由：/seckill/confirm、/seckill/pay、/seckill/result、/orders，前三条需用户登录守卫
Phase C — Home/index.vue 改造
步骤	文件	说明
C1	src/views/Home/index.vue	"立即抢购"按钮 → 不再直接下单，改为 router.push({ path: '/seckill/confirm', query: { spId } })，跳转到下单确认页
C2	src/views/Home/index.vue	删除现有 placeSeckillOrder() 函数和 Toast 组件（移到 OrderConfirm 页面）
C3	src/views/Home/index.vue	顶栏增加"我的订单"入口（登录用户可见）
Phase D — 后端补全
步骤	文件	说明
D1	SeckillOrderController.java	新增 POST /api/seckill/order/{orderNo}/pay Mock 支付接口：校验订单归属+状态=0，更新 status=1 + payTime=now
D2	OrderService.java + OrderServiceImpl.java	新增 mockPay(String orderNo) 方法
D3	SeckillPublicController.java 或新接口	新增 GET /api/seckill/product/{spId} 单个秒杀商品详情（下单确认页需要单独拉取商品数据）
Phase E — 联调验证
步骤	说明
E1	完整链路：首页点击抢购 → 下单确认页 → 确认下单（Lua扣库存） → Mock支付 → 支付成功 → 查看订单
E2	异常链路：未登录→跳登录、库存不足→提示、重复购买→提示

⚠️ 后端缺失功能（15%）
1. 支付接口（Mock 支付）

❌ POST /api/seckill/order/{orderNo}/pay：Mock 支付接口
功能：校验订单归属 + 状态=0（待支付）→ 更新 status=1（已支付）+ payTime=now
需要在 OrderService 和 OrderServiceImpl 中新增 mockPay(String orderNo) 方法- 需要在 SeckillOrderController 中新增接口
2. 单个秒杀商品详情接口（可选）

❌ GET /api/seckill/product/{spId}：查询单个秒杀商品详情
功能：下单确认页需要单独拉取商品数据（商品名称、图片、秒杀价、库存）
当前只有列表接口（/api/seckill/active），没有单个商品详情接口
可以通过前端缓存列表数据来规避，不是必须的

❌ 前端缺失功能（60%）
1. 用户端秒杀下单确认页（0%）

❌ views/Seckill/OrderConfirm.vue：秒杀下单确认页
功能：
展示商品信息（图片、名称、秒杀价）
填写收货信息（收货人、手机号、收货地址）
显示应付金额
"确认下单"按钮 → 调用 /api/seckill/order 接口
下单成功 → 跳转到支付页（/seckill/pay?orderNo=xxx）
路由：/seckill/confirm?spId=xxx
当前首页直接下单，没有确认页
2. 用户端 Mock 支付页（0%）

❌ views/Seckill/PayMock.vue：Mock 支付页
功能：
显示订单号 + 应付金额
"模拟支付"按钮 → 调用 /api/seckill/order/{orderNo}/pay 接口
支付成功 → 跳转到支付结果页（/seckill/result?success=true&orderNo=xxx）
路由：/seckill/pay?orderNo=xxx
3. 用户端支付结果页（0%）

❌ views/Seckill/PayResult.vue：支付结果页
功能：
成功/失败展示（根据 URL 参数 success=true/false）
"查看订单"按钮 → 跳转到订单列表页（/orders）
"返回首页"按钮 → 跳转到首页（/home）
路由：/seckill/result?success=true&orderNo=xxx
4. 用户端我的订单列表页（0%）

❌ views/Seckill/MyOrders.vue：我的订单列表页
功能：
调用 /api/seckill/orders 接口（分页查询）
展示订单卡片（商品图、名称、秒杀价、数量、订单状态、订单号、下单时间）
订单状态标签（待支付/已支付/已取消）
点击订单卡片 → 查看订单详情（可选）
路由：/orders
当前首页顶栏没有"我的订单"入口
5. 首页改造（20%）

⚠️ Home/index.vue 需要改造：
❌ "立即抢购"按钮 → 改为跳转到下单确认页（router.push({ path: '/seckill/confirm', query: { spId } })）
❌ 删除现有的 placeSeckillOrder() 函数和 Toast 组件（移到 OrderConfirm 页面）
❌ 顶栏增加"我的订单"入口（登录用户可见）
6. 路由配置（0%）

❌ router/index.js 需要新增 4 条路由：
/seckill/confirm：下单确认页（需用户登录守卫）
/seckill/pay：Mock 支付页（需用户登录守卫）
/seckill/result：支付结果页（需用户登录守卫）
/orders：我的订单列表页（需用户登录守卫）
❌ 需要添加用户登录路由守卫（检查 sessionStorage.sessionId）




Phase 1 — 中间件容器化（基础先跑通）
Step 1：安装并启动 Docker Desktop（若未安装）

下载 Docker Desktop for Windows，开启 WSL2 后端
验证：docker version + docker compose version
Step 2：创建 docker-compose.yml（根目录）

定义 mysql、redis 两个 service
挂载数据卷（持久化）
统一 xtreme-net bridge 网络
Step 3：修改 application-dev.yml

spring.datasource.url 中 host 改为 localhost（因为后端此阶段仍在宿主机跑，映射端口访问容器）
spring.redis.host 同理改为 localhost
Step 4：导入 SQL 初始化脚本

在 docker-compose 中挂载 sql 到 MySQL 容器初始化目录，自动建表
Step 5：启动并验证

docker compose up -d
用 DBeaver/MysqlWorkbench 连接 localhost:3306 验证表结构
redis-cli -h localhost ping 验证 Redis 通
Phase 2 — Nginx 容器化（前端统一入口）
Step 6：创建 xtreme_frontend/nginx.conf

location / → serve /usr/share/nginx/html（Vue dist）
location /api/ → proxy_pass http://host.docker.internal:8080/（后端宿主机地址）
配置 try_files $uri /index.html（Vue Router history 模式）
Step 7：创建 xtreme_frontend/Dockerfile

多阶段构建：node:20-alpine build → nginx:alpine serve
COPY nginx.conf + COPY dist 产物
Step 8：在 docker-compose 中加入 nginx service

映射 80:80，挂载/COPY 前端 dist + nginx 配置
depends_on: [mysql, redis]（逻辑依赖）
Phase 3 — 后端容器化（完整容器编排）
Step 9：创建 xtreme_backend/Dockerfile

多阶段：maven:3.9-eclipse-temurin-17 build → eclipse-temurin:17-jre-alpine run
或者直接 COPY 本地 mvn package 产物（更快，适合本地调试阶段）
Step 10：修改 application-dev.yml / application-prod.yml

MySQL host 改为 mysql（compose service 名），Redis host 改为 redis
通过环境变量 ${DB_HOST:localhost} 兼容本地 IDEA 运行和容器运行
Step 11：将 backend 加入 docker-compose

environment 传入 DB_HOST、Redis HOST
depends_on: [mysql, redis]
Nginx proxy_pass 从 host.docker.internal:8080 改为 http://backend:8080/
Phase 4 — 扩展中间件（按需引入）
Step 12：Kafka（消息队列，用于秒杀异步下单削峰）

加入 zookeeper + kafka service（或用 KRaft 模式单节点）
后端加 spring-kafka 依赖
Step 13：Elasticsearch（商品搜索）

加入 elasticsearch:8.x service，挂载数据卷
后端加 spring-data-elasticsearch 依赖