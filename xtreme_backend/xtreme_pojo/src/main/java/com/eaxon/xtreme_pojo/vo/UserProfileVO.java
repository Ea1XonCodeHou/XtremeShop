package com.eaxon.xtreme_pojo.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserProfileVO implements Serializable {

    private Long id;

    private String phone;

    private String username;

    private LocalDateTime createdAt;
}
