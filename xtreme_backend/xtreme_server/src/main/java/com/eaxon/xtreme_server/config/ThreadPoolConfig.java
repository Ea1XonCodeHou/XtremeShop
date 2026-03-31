package com.eaxon.xtreme_server.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 秒杀异步落库线程池配置
 *
 * 设计原则：
 * - 核心线程常驻（corePoolSize=5），快速处理落库任务
 * - 队列缓冲（queueCapacity=500），应对瞬时峰值
 * - CallerRunsPolicy：队列满时由提交者线程兜底执行，不丢弃任务
 * - 线程命名方便排查（seckill-order-{n}）
 *
 * Redis Lua 脚本校验通过后，任务投入此线程池异步写入 MySQL，
 * 保证接口响应 < 5ms（仅 Redis RTT），落库在后台完成。
 */
@EnableAsync
@Configuration
public class ThreadPoolConfig {

    /**
     * 秒杀订单异步落库专用线程池
     * 通过 @Async("seckillOrderExecutor") 指定使用
     */
    @Bean("seckillOrderExecutor")
    public Executor seckillOrderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 常驻线程：正常并发下由这 5 条线程处理落库
        executor.setCorePoolSize(5);

        // 最大线程：高峰时扩展，超过队列容量后启动
        executor.setMaxPoolSize(15);

        // 等待队列：最多缓冲 500 条待落库任务
        executor.setQueueCapacity(500);

        // 非核心线程空闲 60s 回收
        executor.setKeepAliveSeconds(60);

        // 线程命名（方便日志追踪）
        executor.setThreadNamePrefix("seckill-order-");

        // 拒绝策略：队列满 + 线程池满时，由调用者线程执行（不丢单）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 优雅关闭：等待已提交任务执行完
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }
}
