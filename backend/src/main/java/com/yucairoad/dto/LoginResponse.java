package com.yucairoad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;

    public static LoginResponse of(String token) {
        return new LoginResponse(token);
    }
}
