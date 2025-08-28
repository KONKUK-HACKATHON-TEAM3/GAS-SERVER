package com.gas.server.domain.enums;

import com.gas.server.global.exception.BusinessException;
import com.gas.server.global.exception.ErrorType;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProfileType {

    FATHER,
    MOTHER,
    GRANDFATHER,
    GRANDMOTHER,
    BOY,
    GIRL,
    MAN_1,
    WOMAN_1,
    MAN_2,
    WOMAN_2,
    ;

    private static final Map<String, ProfileType> PROFILE_TYPE_MAP = new HashMap<>();

    static {
        for (ProfileType profileType : ProfileType.values()) {
            PROFILE_TYPE_MAP.put(profileType.name(), profileType);
        }
    }

    public static ProfileType fromValue(String value) {
        ProfileType profileType = PROFILE_TYPE_MAP.get(value.toUpperCase());

        if (profileType == null) {
            throw new BusinessException(ErrorType.INVALID_PROFILE_TYPE_ERROR);
        }

        return profileType;
    }
}
