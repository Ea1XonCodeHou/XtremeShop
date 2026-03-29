package com.eaxon.xtreme_server.controller;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_pojo.dto.MerchantInfoDTO;
import com.eaxon.xtreme_pojo.dto.MerchantLoginDTO;
import com.eaxon.xtreme_pojo.dto.MerchantRegisterDTO;
import com.eaxon.xtreme_pojo.entity.Merchant;
import com.eaxon.xtreme_pojo.vo.MerchantLoginVO;
import com.eaxon.xtreme_server.context.BaseContext;
import com.eaxon.xtreme_server.service.MerchantService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private static final String TOKEN_HEADER = "X-Merchant-Token";
    private final MerchantService merchantService;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody MerchantRegisterDTO dto) {
        log.info("商家注册: {}", dto.getPhone());
        merchantService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<MerchantLoginVO> login(@RequestBody MerchantLoginDTO dto) {
        log.info("商家登录: {}", dto.getPhone());
        MerchantLoginVO vo = merchantService.login(dto);
        return Result.success(vo);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        merchantService.logout(token);
        return Result.success();
    }

    /** 获取当前商家店铺信息 */
    @GetMapping("/info")
    public Result<Merchant> getInfo() {
        Long merchantId = BaseContext.getCurrentId();
        Merchant merchant = merchantService.getById(merchantId);
        // 清除敏感字段再返回
        merchant.setPassword(null);
        merchant.setSalt(null);
        return Result.success(merchant);
    }

    /** 更新店铺名称/简介/Logo */
    @PutMapping("/info")
    public Result<Void> updateInfo(@RequestBody MerchantInfoDTO dto) {
        Long merchantId = BaseContext.getCurrentId();
        merchantService.updateInfo(merchantId, dto);
        return Result.success();
    }
}
