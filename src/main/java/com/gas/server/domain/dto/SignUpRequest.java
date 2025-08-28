package com.gas.server.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "nickname는 공백일 수 없습니다.")
        @Size(min = 2, max = 20, message = "nickname은 2자 이상 20자 이하여야 합니다.")
        String nickname,

        @NotBlank(message = "profile는 공백일 수 없습니다.")
        @Size(max = 20, message = "profile은 20자 이하여야 합니다.")
        String profile
) {

}
