package com.eaxon.xtreme_server.interceptor;

import com.eaxon.xtreme_server.context.BaseContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantInterceptor implements HandlerInterceptor {

    private static final String TOKEN_HEADER = "X-Merchant-Token";
    private static final long SESSION_EXPIRE_MINUTES = 30;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(TOKEN_HEADER);

        if (token == null || token.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"message\":\"请先登录商家账号\"}");
            return false;
        }

        String merchantIdStr = redisTemplate.opsForValue().get("merchant:session:" + token);
        if (merchantIdStr == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"message\":\"登录已过期，请重新登录\"}");
            return false;
        }

        // 滑动续期
        redisTemplate.expire("merchant:session:" + token, SESSION_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.expire("merchant:token:" + merchantIdStr, SESSION_EXPIRE_MINUTES, TimeUnit.MINUTES);

        BaseContext.setCurrentId(Long.parseLong(merchantIdStr));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
