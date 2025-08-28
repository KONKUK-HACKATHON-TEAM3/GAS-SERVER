package com.gas.server.domain.service;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.gas.server.domain.dto.Feed;
import com.gas.server.domain.dto.FeedDateListResponse;
import com.gas.server.domain.dto.FeedListResponse;
import com.gas.server.domain.entity.FeedEntity;
import com.gas.server.domain.entity.MemberEntity;
import com.gas.server.domain.entity.MemberLikeEntity;
import com.gas.server.domain.repository.FeedRepository;
import com.gas.server.domain.repository.MemberLikeRepository;
import com.gas.server.domain.repository.MemberMissionRepository;
import com.gas.server.domain.repository.MemberRepository;
import com.gas.server.domain.repository.projection.FeedLikeCount;
import com.gas.server.global.exception.BusinessException;
import com.gas.server.global.exception.ErrorType;
import com.gas.server.global.openai.OpenAIService;
import com.gas.server.global.s3.S3Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
    private final MemberLikeRepository memberLikeRepository;
    private final MemberMissionRepository memberMissionRepository;
    private final S3Service s3Service;
    private final OpenAIService openAIService;

    @Transactional(readOnly = true)
    public FeedListResponse getFeeds(final Long memberId, final LocalDate date) {
        // 회원 존재 여부 확인
        MemberEntity member = memberRepository.findByIdOrElseThrow(memberId);

        // 해당 날짜의 시작과 끝 시간 계산
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        // 해당 날짜의 피드 목록 조회
        List<FeedEntity> feeds = feedRepository.
                findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(start, end);

        // 피드가 없는 경우 빈 리스트 반환
        if (feeds.isEmpty()) {
            return FeedListResponse.of(List.of());
        }

        // 피드 ID 리스트 추출
        List<Long> feedIds = feeds.stream()
                .map(FeedEntity::getId)
                .toList();

        if (feedIds.isEmpty()) {
            return FeedListResponse.of(List.of());
        }

        // 사용자가 좋아요한 피드 ID 목록 조회
        Set<Long> likedFeedIds = memberLikeRepository.findLikedFeedIds(memberId, feedIds);

        // 각 피드의 좋아요 개수 조회
        Map<Long, Integer> likeCounts = memberLikeRepository.countByFeedIds(feedIds).stream()
                .collect(toMap(
                        FeedLikeCount::getFeedId,
                        FeedLikeCount::getCountAsInt,
                        (a, b) -> a
                ));

        // 피드 작성자 정보 조회
        Set<Long> authorIds = feeds.stream()
                .map(FeedEntity::getMemberId)
                .collect(toSet());

        Map<Long, MemberEntity> authorMap = memberRepository.findAllById(authorIds).stream()
                .collect(toMap(MemberEntity::getId, it -> it));

        // Feed DTO 리스트 생성
        List<Feed> feedList = feeds.stream()
                .map(feed -> {
                    MemberEntity author = authorMap.get(feed.getMemberId());

                    if (author == null) {
                        if (Objects.equals(feed.getMemberId(), memberId)) {
                            author = member;
                        } else {
                            return null;
                        }
                    }

                    String displayNickname = Objects.equals(feed.getMemberId(), memberId) ? "나" : author.getNickname();

                    return Feed.of(
                            feed.getId(),
                            author.getProfileType(),
                            displayNickname,
                            feed.getText(),
                            feed.getImageUrl(),
                            feed.getTag(),
                            likeCounts.getOrDefault(feed.getId(), 0),
                            likedFeedIds.contains(feed.getId())
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        return FeedListResponse.of(feedList);
    }

    @Transactional
    public void postFeed(
            final Long memberId,
            final MultipartFile media,
            final String text
    ) {
        // 회원 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorType.NOT_FOUND_MEMBER_ERROR);
        }

        // S3에 파일 업로드
        String mediaUrl = s3Service.uploadFile(media);
        log.info("File uploaded to S3: {}", mediaUrl);

        // 피드 엔티티 생성 및 저장 (태그는 null로)
        FeedEntity feed = FeedEntity.builder()
                .memberId(memberId)
                .imageUrl(mediaUrl)
                .text(text)
                .tag(null)  // 태그는 나중에 비동기로 업데이트
                .build();

        feed = feedRepository.save(feed);
        log.info("Feed saved successfully for member: {}", memberId);

        // 미션 2: 오늘의 사진 업로드 시 완료
        if (!memberMissionRepository.existsByMemberIdAndMissionIdAndMissionDate(memberId, 2L, LocalDate.now())) {
            memberMissionRepository.save(
                    com.gas.server.domain.entity.MemberMissionEntity.builder()
                            .memberId(memberId)
                            .missionId(2L)
                            .missionDate(LocalDate.now())
                            .build()
            );
            log.info("Mission (ID: 2) completed for member: {}", memberId);
        }

        // 비동기로 태그 생성 및 업데이트
        if (s3Service.isImageFile(media)) {
            Long feedId = feed.getId();
            CompletableFuture.runAsync(() -> {
                processTagGenerationAsync(feedId, memberId, mediaUrl, text);
            });
        } else if (s3Service.isVideoFile(media) && text != null) {
            Long feedId = feed.getId();
            CompletableFuture.runAsync(() -> {
                processVideoTagGenerationAsync(feedId, text);
            });
        }
    }

    @Async
    @Transactional
    public void processTagGenerationAsync(Long feedId, Long memberId, String mediaUrl, String text) {
        try {
            // OpenAI Vision API 호출
            OpenAIService.TagGenerationResult result = openAIService.generateTagsWithFoodDetection(mediaUrl, text);

            // 태그 업데이트
            FeedEntity feed = feedRepository.findById(feedId).orElse(null);
            if (feed != null && result.getTags() != null) {
                feed.updateTag(result.getTags());
                feedRepository.save(feed);
                log.info("Tags updated for feed {}: {}", feedId, result.getTags());
            }

            // 음식 감지 시 미션 4 완료
            if (result.isFoodDetected() &&
                    !memberMissionRepository.existsByMemberIdAndMissionIdAndMissionDate(memberId, 4L,
                            LocalDate.now())) {
                memberMissionRepository.save(
                        com.gas.server.domain.entity.MemberMissionEntity.builder()
                                .memberId(memberId)
                                .missionId(4L)
                                .missionDate(LocalDate.now())
                                .build()
                );
                log.info("Food mission (ID: 4) completed for member: {}", memberId);
            }
        } catch (Exception e) {
            log.error("Failed to generate tags for feed {}: {}", feedId, e.getMessage());
        }
    }

    @Async
    @Transactional
    public void processVideoTagGenerationAsync(Long feedId, String text) {
        try {
            String tags = openAIService.generateTagsForVideo(text);

            FeedEntity feed = feedRepository.findById(feedId).orElse(null);
            if (feed != null && tags != null) {
                feed.updateTag(tags);
                feedRepository.save(feed);
                log.info("Video tags updated for feed {}: {}", feedId, tags);
            }
        } catch (Exception e) {
            log.error("Failed to generate video tags for feed {}: {}", feedId, e.getMessage());
        }
    }

    @Transactional
    public void postLike(final Long feedId, final Long memberId) {
        // 회원 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorType.NOT_FOUND_MEMBER_ERROR);
        }

        // 피드 존재 여부 확인
        if (!feedRepository.existsById(feedId)) {
            throw new BusinessException(ErrorType.NOT_FOUND_FEED_ERROR);
        }

        // 이미 좋아요를 눌렀는지 확인
        if (memberLikeRepository.existsByMemberIdAndFeedId(memberId, feedId)) {
            return;
        }

        // 좋아요 저장
        memberLikeRepository.save(
                MemberLikeEntity.builder()
                        .memberId(memberId)
                        .feedId(feedId)
                        .build()
        );
    }

    @Transactional
    public void deleteLike(final Long feedId, final Long memberId) {
        // 회원 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorType.NOT_FOUND_MEMBER_ERROR);
        }

        // 피드 존재 여부 확인
        if (!feedRepository.existsById(feedId)) {
            throw new BusinessException(ErrorType.NOT_FOUND_FEED_ERROR);
        }

        // 좋아요 삭제
        memberLikeRepository.deleteByMemberIdAndFeedId(memberId, feedId);
    }
    
    @Transactional(readOnly = true)
    public FeedDateListResponse getFeedDates(final String yearMonth) {
        try {
            // yyyy-MM 형식 파싱
            YearMonth ym = YearMonth.parse(yearMonth);
            
            // 해당 월의 시작일과 종료일 계산
            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            
            // 해당 월에 피드가 있는 날짜들 조회
            List<LocalDate> feedDates = feedRepository.findDistinctDatesByCreatedAtBetween(
                    startDateTime, endDateTime
            );
            
            return FeedDateListResponse.of(feedDates);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorType.INVALID_DATE_TIME_FORMAT_ERROR);
        }
    }
}
