package com.eaxon.xtreme_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_pojo.dto.UserLoginDTO;
import com.eaxon.xtreme_pojo.dto.UserProfileDTO;
import com.eaxon.xtreme_pojo.dto.UserRegisterDTO;
import com.eaxon.xtreme_pojo.vo.UserLoginVO;
import com.eaxon.xtreme_pojo.vo.UserProfileVO;
import com.eaxon.xtreme_server.context.BaseContext;
import com.eaxon.xtreme_server.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private static final String SESSION_COOKIE_NAME = "X-Session-Id";
    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserRegisterDTO dto) {
        log.info("用户注册: {}", dto.getPhone());
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO dto, HttpServletResponse response) {
        log.info("用户登录: {}", dto.getPhone());
        UserLoginVO vo = userService.login(dto);
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, vo.getSessionId());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 60);
        response.addCookie(cookie);
        return Result.success(vo);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = request.getHeader(SESSION_COOKIE_NAME);

        if ((sessionId == null || sessionId.isBlank()) && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }

        userService.logout(sessionId);

        Cookie clearCookie = new Cookie(SESSION_COOKIE_NAME, "");
        clearCookie.setPath("/");
        clearCookie.setMaxAge(0);
        response.addCookie(clearCookie);
        return Result.success();
    }

    /** 获取当前用户个人信息（需登录） */
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(userService.getProfile(userId));
    }

    /** 修改当前用户昵称（需登录） */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserProfileDTO dto) {
        Long userId = BaseContext.getCurrentId();
        userService.updateProfile(userId, dto);
        return Result.success();
    }
}
