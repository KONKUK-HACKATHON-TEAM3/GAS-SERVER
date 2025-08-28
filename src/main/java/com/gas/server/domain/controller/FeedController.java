package com.gas.server.domain.controller;

import com.gas.server.domain.dto.FeedDateListResponse;
import com.gas.server.domain.dto.FeedListResponse;
import com.gas.server.domain.service.FeedService;
import com.gas.server.global.exception.BusinessException;
import com.gas.server.global.exception.ErrorType;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class FeedController {

    private final FeedService feedService;

    @GetMapping(path = "/feeds", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FeedListResponse> getFeeds(
            @RequestParam @Positive(message = "memberId는 양수여야 합니다.") Long memberId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(
                feedService.getFeeds(memberId, date)
        );
    }

    @PostMapping(path = "/feeds", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> postFeed(
            @RequestParam @Positive(message = "memberId는 양수여야 합니다.") Long memberId,
            @RequestPart MultipartFile media,
            @RequestParam(required = false) String text
    ) {
        if (StringUtils.hasText(text) && text.length() > 150) {
            throw new BusinessException(ErrorType.INVALID_TEXT_COUNT_ERROR);
        }

        feedService.postFeed(memberId, media, text);

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/feeds/{feedId}")
    public ResponseEntity<Void> postLike(
            @PathVariable @Positive(message = "feedId는 양수여야 합니다.") Long feedId,
            @RequestParam @Positive(message = "memberId는 양수여야 합니다.") Long memberId
    ) {
        feedService.postLike(feedId, memberId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/feeds/{feedId}")
    public ResponseEntity<Void> deleteLike(
            @PathVariable @Positive(message = "feedId는 양수여야 합니다.") Long feedId,
            @RequestParam @Positive(message = "memberId는 양수여야 합니다.") Long memberId
    ) {
        feedService.deleteLike(feedId, memberId);

        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/feeds/dates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FeedDateListResponse> getFeedDates(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String yearMonth
    ) {
        return ResponseEntity.ok(
                feedService.getFeedDates(yearMonth)
        );
    }
}
