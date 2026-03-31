--[[
  秒杀核心 Lua 脚本 —— 原子执行「防超卖 + 一人一单」
  =========================================================

  执行逻辑（三步原子）：
    1. 查库存  —— GET  KEYS[1]  若 ≤ 0 → 返回 -1（库存不足）
    2. 查重购  —— SISMEMBER KEYS[2] ARGV[1]  若已存在 → 返回 -2（已购买）
    3. 扣库存  —— DECR KEYS[1]
       标记购买 —— SADD KEYS[2] ARGV[1]
    4. 返回 1（秒杀资格获取成功，可进入异步落库）

  Redis Key 说明：
    KEYS[1] = seckill:stock:{spId}
              String 类型，存储剩余秒杀库存（活动开始前由后端 warmUp 写入）
    KEYS[2] = seckill:bought:{spId}
              Set 类型，存储已购买此秒杀商品的 userId 集合

  ARGV 说明：
    ARGV[1] = userId（String 类型，当前下单用户）

  返回值约定（Java 端用 Long 接收）：
     1 → 秒杀成功，可异步落库
    -1 → 库存不足（已抢完）
    -2 → 重复购买（一人一单）
--]]

-- 读取当前库存（String 类型，需 tonumber 转换）
local stock = tonumber(redis.call('get', KEYS[1]))

-- 1. 库存检查 —— nil 视为已售罄
if stock == nil or stock <= 0 then
    return -1
end

-- 2. 重复购买检查 —— Set 中已存在该用户
if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
    return -2
end

-- 3. 原子扣减库存 + 标记已购（在同一 Lua 脚本内，Redis 保证原子性）
redis.call('decr', KEYS[1])
redis.call('sadd', KEYS[2], ARGV[1])

return 1
