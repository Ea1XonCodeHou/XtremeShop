package com.eaxon.xtreme_pojo.vo;

import lombok.Data;

@Data
public class MerchantLoginVO {
    private Long merchantId;
    private String phone;
    private String name;
    private String logoUrl;
    private String token; // merchant session token
}
