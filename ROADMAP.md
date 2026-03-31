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