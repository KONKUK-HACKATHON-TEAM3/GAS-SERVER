package com.gas.server.domain.controller;

import com.gas.server.domain.dto.SignUpRequest;
import com.gas.server.domain.dto.SignUpResponse;
import com.gas.server.domain.enums.ProfileType;
import com.gas.server.domain.service.HomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
