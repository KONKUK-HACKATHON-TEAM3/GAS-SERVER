package com.gas.server.domain.dto;

import java.time.LocalDateTime;

public record FamilyStory(
        String nickname,
        String imageUrl,
        LocalDateTime createdAt
) {

    public static FamilyStory of(
            final String nickname,
            final String imageUrl,
            final LocalDateTime createdAt
    ) {
        return new FamilyStory(nickname, imageUrl, createdAt);
    }
}
