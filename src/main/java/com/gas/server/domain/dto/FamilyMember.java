package com.gas.server.domain.dto;

import com.gas.server.domain.enums.ProfileType;

public record FamilyMember(
        String nickname,
        ProfileType profile
) {

    public static FamilyMember of(
            final String nickname,
            final ProfileType profile
    ) {
        return new FamilyMember(nickname, profile);
    }
}
