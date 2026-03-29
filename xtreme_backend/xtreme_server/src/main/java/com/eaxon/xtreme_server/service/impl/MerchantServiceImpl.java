package com.eaxon.xtreme_server.service.impl;

import com.eaxon.xtreme_common.utils.PasswordUtils;
import com.eaxon.xtreme_common.utils.RedisIdWorker;
import com.eaxon.xtreme_pojo.dto.MerchantInfoDTO;
import com.eaxon.xtreme_pojo.dto.MerchantLoginDTO;
import com.eaxon.xtreme_pojo.dto.MerchantRegisterDTO;
import com.eaxon.xtreme_pojo.entity.Merchant;
import com.eaxon.xtreme_pojo.vo.MerchantLoginVO;
import com.eaxon.xtreme_server.mapper.MerchantMapper;
import com.eaxon.xtreme_server.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;
    private final StringRedisTemplate redisTemplate;
    private final RedisIdWorker redisIdWorker;

    private static final long SESSION_EXPIRE_MINUTES = 30;

    // Redis key 前缀与用户端严格隔离
    private String sessionKey(String token) {
        return "merchant:session:" + token;
    }

    private String merchantTokenKey(Long merchantId) {
        return "merchant:token:" + merchantId;
    }

    @Override
    public void register(MerchantRegisterDTO dto) {
        if (merchantMapper.selectByPhone(dto.getPhone()) != null) {
            throw new RuntimeException("该手机号已注册");
        }

        String salt = PasswordUtils.generateSalt();
        String encodedPassword = PasswordUtils.encode(dto.getPassword(), salt);

        Merchant merchant = new Merchant();
        merchant.setId(redisIdWorker.nextId("merchant")); // RedisIdWorker 生成全局唯一 ID
        merchant.setPhone(dto.getPhone());
        merchant.setName(dto.getName());
        merchant.setPassword(encodedPassword);
        merchant.setSalt(salt);
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchant.setIsActive(1);

        merchantMapper.insert(merchant);
        log.info("商家注册成功 - merchantId: {}, phone: {}, name: {}", merchant.getId(), merchant.getPhone(), merchant.getName());
    }

    @Override
    public MerchantLoginVO login(MerchantLoginDTO dto) {
        Merchant merchant = merchantMapper.selectByPhone(dto.getPhone());
        if (merchant == null) {
            throw new RuntimeException("手机号未注册");
        }
        if (!PasswordUtils.verify(dto.getPassword(), merchant.getPassword(), merchant.getSalt())) {
            throw new RuntimeException("密码错误");
        }

        // 单会话策略：踢掉旧 token
        String oldToken = redisTemplate.opsForValue().get(merchantTokenKey(merchant.getId()));
        if (oldToken != null && !oldToken.isBlank()) {
            redisTemplate.delete(sessionKey(oldToken));
        }

        // 生成新 token 存入 Redis
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(sessionKey(token), String.valueOf(merchant.getId()), SESSION_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(merchantTokenKey(merchant.getId()), token, SESSION_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("商家登录成功 - merchantId: {}, phone: {}, token: {}", merchant.getId(), merchant.getPhone(), token);

        MerchantLoginVO vo = new MerchantLoginVO();
        vo.setMerchantId(merchant.getId());
        vo.setPhone(merchant.getPhone());
        vo.setName(merchant.getName());
        vo.setLogoUrl(merchant.getLogoUrl());
        vo.setToken(token);
        return vo;
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) return;

        String sessionKey = sessionKey(token);
        String merchantIdStr = redisTemplate.opsForValue().get(sessionKey);

        redisTemplate.delete(sessionKey);
        if (merchantIdStr != null && !merchantIdStr.isBlank()) {
            redisTemplate.delete(merchantTokenKey(Long.parseLong(merchantIdStr)));
        }
        log.info("商家退出登录 - token: {}", token);
    }

    @Override
    public Merchant getById(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) throw new RuntimeException("商家不存在");
        return merchant;
    }

    @Override
    public void updateInfo(Long merchantId, MerchantInfoDTO dto) {
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        if (dto.getName() != null && !dto.getName().isBlank()) merchant.setName(dto.getName());
        if (dto.getDescription() != null) merchant.setDescription(dto.getDescription());
        if (dto.getLogoUrl() != null) merchant.setLogoUrl(dto.getLogoUrl());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        log.info("商家 {} 更新店铺信息", merchantId);
    }
}
