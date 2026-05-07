package com.yucairoad.controller;

import com.yucairoad.common.Result;
import com.yucairoad.dto.UpdateProfileRequest;
import com.yucairoad.entity.SysUser;
import com.yucairoad.service.SysUserService;
import com.yucairoad.utils.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final SysUserService sysUserService;

    public UserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping("/profile")
    public Result<SysUser> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        SysUser user = sysUserService.getUserProfile(userId);
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<SysUser> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        SysUser user = sysUserService.getUserProfile(userId);
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        sysUserService.updateUserProfile(user);
        user.setPassword(null);
        return Result.success("更新成功", user);
    }
}
