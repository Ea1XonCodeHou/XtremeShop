# XtremeShop 开发路线图

> 当前已完成：分布式 Session 登录注册（用户端）
> 目标：在此基础上构建完整的分布式秒杀系统

---

## 数据库表总览（9 张表）

| 文件 | 表名 | 说明 |
|------|------|------|
| `xtreme_user.sql` | `user` | 用户表（已建） |
| `merchant.sql` | `merchant` | 商家表，独立登录体系 |
| `category.sql` | `category` | 商品分类表（含初始数据） |
| `product.sql` | `product` | 商品表，商家发布，OSS 图片 |
| `seckill.sql` | `seckill_activity` | 秒杀活动表 |
| `seckill.sql` | `seckill_product` | 秒杀商品表（活动×商品，设秒杀价/库存） |
| `coupon.sql` | `coupon` | 优惠券表，支持满减/折扣 |
| `coupon.sql` | `user_coupon` | 用户优惠券表（领取/使用记录） |
| `order.sql` | `order` | 订单表，含价格快照和优惠券核销 |

**建表顺序**（按外键依赖）：
```
user → merchant → category → product
     → seckill_activity → seckill_product（依赖 product）
     → coupon（依赖 merchant） → user_coupon（依赖 user + coupon）
     → order（依赖 user + merchant + product + seckill_product + user_coupon）
```

---

## 第一阶段：商家端基础（后端 + 前端）

**目标**：商家可以注册/登录，发布商品，主页支持「我是商家」入口

### 后端
- [ ] `MerchantController` — 注册/登录（复用 PasswordUtils + Redis Session，复用 `@AutoFill`）
- [ ] `MerchantInterceptor` — 商家鉴权拦截器，加入 `WebMvcConfig`
- [ ] `CategoryController` — 分类列表查询（只读，供前端下拉）
- [ ] `ProductController` — 商家发布/修改/下架商品（需鉴权）
- [ ] `ProductController` — 用户端商品列表（按分类查询、分页）
- [ ] **阿里云 OSS 集成** — `OssService`，供商品封面上传

### 前端
- [ ] 主页导航栏加「我是商家」入口（跳转商家登录页）
- [ ] 商家登录/注册页（复用用户登录样式）
- [ ] 商家后台页（发布商品表单，含图片上传）
- [ ] 主页商品列表对接后端（替换硬编码数据，支持分类筛选）

---

## 第二阶段：秒杀核心（后端）

**目标**：实现高并发安全的秒杀下单，不超卖

### 核心设计
```
用户点击「立即抢购」
  ↓
1. 拦截器鉴权（必须登录）
2. 接口限流（Redisson RateLimiter 或 Redis INCR，每秒每 IP 上限）
3. 检查用户是否已抢过（Redis Set: seckill:bought:{activityId}:{productId}）
4. Redis 预减库存（DECR seckill:stock:{seckillProductId}，< 0 则秒杀结束）
5. 发送消息到 RabbitMQ（异步落单）
  ↓ 异步
6. 消费消息：写 order 表 + 扣减 seckill_product.seckill_stock
7. 返回订单号（前端轮询订单状态）
```

### 后端
- [ ] `SeckillActivityController` — 活动列表/详情查询
- [ ] `SeckillController` — 秒杀下单接口（核心）
- [ ] Redis 预加载库存（应用启动时 / 活动开始时，把库存写入 Redis）
- [ ] **MQ 异步落单**（引入 RabbitMQ 或先用线程池简化版）
- [ ] `OrderController` — 查询我的订单状态

### Redis Key 设计
| Key | 类型 | 说明 |
|-----|------|------|
| `seckill:stock:{seckillProductId}` | String | 秒杀库存，原子 DECR |
| `seckill:bought:{activityId}:{userId}` | Set | 已抢用户集合，防重复 |
| `order:status:{orderNo}` | String | 订单状态缓存（轮询用） |
| `session:{token}` | String | 用户 session（已有） |
| `user:session:{userId}` | String | 用户 token 反查（已有） |

---

## 第三阶段：优惠券秒杀（后端 + 前端）

**目标**：用户可以秒杀抢券，下单时使用优惠券抵扣

### 后端
- [ ] `CouponController` — 优惠券列表、抢券接口（防重复领取）
- [ ] 抢券用 Redis 原子操作（同秒杀库存逻辑）
- [ ] 下单时核销优惠券（更新 `user_coupon.status = 1`）
- [ ] 计算实付金额（`actual_amount = seckill_price × qty - discount`）

### 前端
- [ ] 秒杀页展示活动商品（倒计时 + 秒杀价 + 剩余库存）
- [ ] 下单弹窗（选择优惠券、确认金额）
- [ ] 抢购结果页（成功/失败、订单号）
- [ ] 「我的订单」页（订单列表、状态展示）
- [ ] 「我的优惠券」页（未使用/已使用/已过期）

---

## 第四阶段：体验优化

- [ ] 秒杀页实时库存刷新（轮询或 WebSocket）
- [ ] 订单超时自动取消（Redis TTL + 定时任务恢复库存）
- [ ] 商家后台订单管理
- [ ] 接口限流（Redisson / Spring 限流注解）
- [ ] 压测验证不超卖（JMeter 模拟 500 并发）

---

## 下一步立即动手

**第一阶段第一步**：
1. 在 MySQL 执行所有建表 SQL（按顺序）
2. 后端新建 `MerchantController` + `MerchantService` + `MerchantMapper`
3. 前端主页导航栏加「我是商家」入口

> 注：第二阶段秒杀核心是本项目的亮点，建议第一阶段完成后立即推进，
> 保证演示时有完整的「商家发布秒杀商品 → 用户抢购 → 生成订单」闭环。
