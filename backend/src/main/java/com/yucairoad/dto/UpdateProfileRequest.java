package com.yucairoad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String nickname;

    private String avatar;
}
