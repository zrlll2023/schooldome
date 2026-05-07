package com.yucairoad.service;

import com.yucairoad.entity.SysUser;

public interface SysUserService {

    String register(String username, String password, String nickname);

    String login(String username, String password);

    SysUser getUserProfile(Long userId);

    void updateUserProfile(SysUser user);
}
