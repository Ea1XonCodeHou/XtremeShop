package com.eaxon.xtreme_common.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.DigestUtils;

public class PasswordUtils {

    // 生成随机盐值（16位十六进制）
    public static String generateSalt() {
        return RandomStringUtils.randomAlphanumeric(16);
    }

    // 密码加盐加密：MD5(password + salt)
    public static String encode(String password, String salt) {
        return DigestUtils.md5DigestAsHex((password + salt).getBytes());
    }

    // 验证密码
    public static boolean verify(String inputPassword, String encodedPassword, String salt) {
        return encode(inputPassword, salt).equals(encodedPassword);
    }
}
