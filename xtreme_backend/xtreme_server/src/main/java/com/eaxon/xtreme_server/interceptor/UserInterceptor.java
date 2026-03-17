package com.eaxon.xtreme_server.interceptor;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.eaxon.xtreme_server.context.BaseContext;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserInterceptor implements HandlerInterceptor {

    private static final String SESSION_HEADER = "X-Session-Id";
    private static final long SESSION_EXPIRE_MINUTES = 30;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 优先从请求头读取 sessionId，不存在时再读取 Cookie
        String sessionId = request.getHeader(SESSION_HEADER);
        if ((sessionId == null || sessionId.isBlank()) && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (SESSION_HEADER.equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }

        if (sessionId == null || sessionId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"message\":\"请先登录\"}");
            return false;
        }

        // 从 Redis 验证 session
        String userIdStr = redisTemplate.opsForValue().get("session:" + sessionId);
        if (userIdStr == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"message\":\"登录已过期，请重新登录\"}");
            return false;
        }

        // 滑动过期：活跃用户每次访问都续期 30 分钟
        redisTemplate.expire("session:" + sessionId, SESSION_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.expire("user:session:" + userIdStr, SESSION_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 存入 ThreadLocal，供后续业务使用
        BaseContext.setCurrentId(Long.parseLong(userIdStr));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
