package com.yucairoad.controller;

import com.yucairoad.common.Result;
import com.yucairoad.dto.LoginRequest;
import com.yucairoad.dto.LoginResponse;
import com.yucairoad.dto.RegisterRequest;
import com.yucairoad.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final SysUserService sysUserService;

    public AuthController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        String token = sysUserService.register(request.getUsername(), request.getPassword(), request.getNickname());
        return Result.success("注册成功", LoginResponse.of(token));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = sysUserService.login(request.getUsername(), request.getPassword());
        return Result.success("登录成功", LoginResponse.of(token));
    }
}
