package com.gas.server.domain.controller;

import com.gas.server.domain.dto.FeedListResponse;
import com.gas.server.domain.service.FeedService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class FeedController {

    private final FeedService feedService;

    @GetMapping(path = "/feeds")
    public ResponseEntity<FeedListResponse> getFeeds(
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(
                feedService.getFeeds(memberId, date)
        );
    }
}
