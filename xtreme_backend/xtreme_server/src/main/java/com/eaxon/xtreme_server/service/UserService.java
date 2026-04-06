package com.eaxon.xtreme_server.service;

import com.eaxon.xtreme_pojo.dto.UserLoginDTO;
import com.eaxon.xtreme_pojo.dto.UserProfileDTO;
import com.eaxon.xtreme_pojo.dto.UserRegisterDTO;
import com.eaxon.xtreme_pojo.vo.UserLoginVO;
import com.eaxon.xtreme_pojo.vo.UserProfileVO;

public interface UserService {
    void register(UserRegisterDTO dto);
    UserLoginVO login(UserLoginDTO dto);
    void logout(String sessionId);
    UserProfileVO getProfile(Long userId);
    void updateProfile(Long userId, UserProfileDTO dto);
}
