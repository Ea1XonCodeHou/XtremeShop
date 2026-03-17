package com.eaxon.xtreme_pojo.vo;

import lombok.Data;

@Data
public class UserLoginVO {
    private Long userId;
    private String phone;
    private String username;
    private String sessionId;
}
