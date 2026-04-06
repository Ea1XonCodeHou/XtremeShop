package com.eaxon.xtreme_server.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.eaxon.xtreme_common.utils.PasswordUtils;
import com.eaxon.xtreme_pojo.dto.UserLoginDTO;
import com.eaxon.xtreme_pojo.dto.UserProfileDTO;
import com.eaxon.xtreme_pojo.dto.UserRegisterDTO;
import com.eaxon.xtreme_pojo.entity.User;
import com.eaxon.xtreme_pojo.vo.UserLoginVO;
import com.eaxon.xtreme_pojo.vo.UserProfileVO;
import com.eaxon.xtreme_server.mapper.UserMapper;
import com.eaxon.xtreme_server.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    private static final long SESSION_EXPIRE_MINUTES = 30;

    private String sessionKey(String sessionId) {
        return "session:" + sessionId;
    }

    private String userSessionKey(Long userId) {
        return "user:session:" + userId;
    }

    @Override
    public void register(UserRegisterDTO dto) {
        // 检查手机号是否已注册
        User existing = userMapper.selectByPhone(dto.getPhone());
        if (existing != null) {
            throw new RuntimeException("该手机号已注册");
        }

        String salt = PasswordUtils.generateSalt();
        String encodedPassword = PasswordUtils.encode(dto.getPassword(), salt);

        User user = new User();
        user.setPhone(dto.getPhone());
        user.setUsername(dto.getPhone());
        user.setPassword(encodedPassword);
        user.setSalt(salt);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsActive(1);

        userMapper.insert(user);
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        User user = userMapper.selectByPhone(dto.getPhone());
        if (user == null) {
            throw new RuntimeException("手机号未注册");
        }
        if (!PasswordUtils.verify(dto.getPassword(), user.getPassword(), user.getSalt())) {
            throw new RuntimeException("密码错误");
        }

        // 单会话策略：同一用户新登录前，先清理旧 session
        String oldSessionId = redisTemplate.opsForValue().get(userSessionKey(user.getId()));
        if (oldSessionId != null && !oldSessionId.isBlank()) {
            redisTemplate.delete(sessionKey(oldSessionId));
        }

        // 生成新 sessionId 存入 Redis
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        log.info("用户登录成功 - userId: {}, phone: {}, token(sessionId): {}", user.getId(), user.getPhone(), sessionId);
        redisTemplate.opsForValue().set(
            sessionKey(sessionId),
                String.valueOf(user.getId()),
                SESSION_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );
        redisTemplate.opsForValue().set(
            userSessionKey(user.getId()),
            sessionId,
            SESSION_EXPIRE_MINUTES,
            TimeUnit.MINUTES
        );

        UserLoginVO vo = new UserLoginVO();
        vo.setUserId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setUsername(user.getUsername());
        vo.setSessionId(sessionId);
        return vo;
    }

    @Override
    public void logout(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        String sessionKey = sessionKey(sessionId);
        String userIdStr = redisTemplate.opsForValue().get(sessionKey);

        redisTemplate.delete(sessionKey);
        if (userIdStr != null && !userIdStr.isBlank()) {
            redisTemplate.delete(userSessionKey(Long.parseLong(userIdStr)));
        }
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setUsername(user.getUsername());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    @Override
    public void updateProfile(Long userId, UserProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            user.setUsername(dto.getUsername());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
