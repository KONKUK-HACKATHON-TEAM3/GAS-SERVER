package com.gas.server.domain.service;

import com.gas.server.domain.entity.MemberEntity;
import com.gas.server.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void updateFcmToken(Long memberId, String fcmToken) {
        MemberEntity member = memberRepository.findByIdOrElseThrow(memberId);

        member.updateFcmToken(fcmToken);
        memberRepository.save(member);

        log.info("FCM token updated for member: {}", memberId);
    }
}
