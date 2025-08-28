package com.gas.server.domain.service;

import com.gas.server.domain.dto.SignUpResponse;
import com.gas.server.domain.entity.MemberEntity;
import com.gas.server.domain.enums.ProfileType;
import com.gas.server.domain.repository.MemberRepository;
import com.gas.server.global.exception.BusinessException;
import com.gas.server.global.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final MemberRepository memberRepository;

    @Transactional
    public SignUpResponse signUp(String nickname, ProfileType profileType) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorType.INVALID_NICKNAME_ERROR);
        }

        MemberEntity member = memberRepository.save(
                MemberEntity.builder()
                        .nickname(nickname)
                        .profileType(profileType)
                        .build()
        );

        return SignUpResponse.of(member.getId());
    }
}
