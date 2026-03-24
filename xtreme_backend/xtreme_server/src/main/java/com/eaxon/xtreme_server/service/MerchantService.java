package com.eaxon.xtreme_server.service;

import com.eaxon.xtreme_pojo.dto.MerchantLoginDTO;
import com.eaxon.xtreme_pojo.dto.MerchantRegisterDTO;
import com.eaxon.xtreme_pojo.vo.MerchantLoginVO;

public interface MerchantService {

    void register(MerchantRegisterDTO dto);

    MerchantLoginVO login(MerchantLoginDTO dto);

    void logout(String token);
}
