package com.gas.server.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gas.server.domain.enums.ProfileType;

@JsonInclude(Include.NON_NULL)
public record Feed(
        Long feedId,
        ProfileType profile,
        String nickname,
        String text,
        String imageUrl,
        String tag,
        Integer likeCount,
        Boolean likeStatus
) {

    public static Feed of(
            final Long feedId,
            final ProfileType profile,
            final String nickname,
            final String text,
            final String imageUrl,
            final String tag,
            final Integer likeCount,
            final Boolean likeStatus
    ) {
        return new Feed(
                feedId, profile, nickname, text, imageUrl, tag, likeCount, likeStatus);
    }
}
