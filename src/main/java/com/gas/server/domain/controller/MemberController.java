package com.gas.server.domain.controller;

import com.gas.server.domain.dto.FcmTokenUpdateRequest;
import com.gas.server.domain.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Validated
public class MemberController {

    private final MemberService memberService;

    @PatchMapping(path = "/fcm-token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateFcmToken(
            @RequestParam @Positive(message = "memberId는 양수여야 합니다.") Long memberId,
            @Valid @RequestBody FcmTokenUpdateRequest request
    ) {
        memberService.updateFcmToken(memberId, request.fcmToken());

        return ResponseEntity.ok().build();
    }
}
