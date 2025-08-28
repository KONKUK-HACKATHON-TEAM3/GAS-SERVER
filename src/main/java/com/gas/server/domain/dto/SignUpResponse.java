package com.gas.server.domain.dto;

public record SignUpResponse(
        Long memberId
) {

    public static SignUpResponse of(Long memberId) {
        return new SignUpResponse(memberId);
    }
}
