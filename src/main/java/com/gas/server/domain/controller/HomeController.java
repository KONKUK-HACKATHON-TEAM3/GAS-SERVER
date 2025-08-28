package com.gas.server.domain.controller;

import com.gas.server.domain.dto.HomeResponse;
import com.gas.server.domain.dto.SignUpRequest;
import com.gas.server.domain.dto.SignUpResponse;
import com.gas.server.domain.dto.WeeklyRankingResponse;
import com.gas.server.domain.enums.ProfileType;
import com.gas.server.domain.service.HomeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class HomeController {

    private final HomeService homeService;

    @PostMapping(
            path = "/sign-up",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SignUpResponse> signUp(
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        ProfileType profileType = ProfileType.fromValue(signUpRequest.profile());

        return ResponseEntity.ok(
                homeService.signUp(signUpRequest.nickname(), profileType)
        );
    }

    @GetMapping(path = "/home", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HomeResponse> getHome(
            @RequestParam @Positive(message = "memberId는 양수여야 합니다.") Long memberId
    ) {
        return ResponseEntity.ok(
                homeService.getHome(memberId)
        );
    }

    @GetMapping(path = "/ranking", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WeeklyRankingResponse> getRanking(
            @RequestParam @Positive(message = "memberId는 양수여야 합니다.") Long memberId
    ) {
        return ResponseEntity.ok(
                homeService.getRanking(memberId)
        );
    }
}
