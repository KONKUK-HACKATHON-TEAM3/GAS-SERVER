package com.gas.server.domain.service;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.gas.server.domain.dto.Feed;
import com.gas.server.domain.dto.FeedListResponse;
import com.gas.server.domain.entity.FeedEntity;
import com.gas.server.domain.entity.MemberEntity;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        // 태그 생성
        String tags;

        if (s3Service.isImageFile(media)) {
            // 이미지인 경우 OpenAI Vision API 호출
            tags = openAIService.generateTags(mediaUrl, text);
            if (tags != null) {
                log.info("Generated tags for image: {}", tags);
            }
        } else {
            // 비디오인 경우 텍스트가 있을 때만 태그 생성
            tags = openAIService.generateTagsForVideo(text);
            if (tags != null) {
                log.info("Generated tags for video: {}", tags);
            }
        }

        // 피드 엔티티 생성 및 저장
        FeedEntity feed = FeedEntity.builder()
                .memberId(memberId)
                .imageUrl(mediaUrl)
                .text(text)
                .tag(tags)
                .build();

        if (!memberMissionRepository.existsByMemberIdAndMissionIdAndMissionDate(memberId, 2L, LocalDate.now())) {
            memberMissionRepository.save(
                    com.gas.server.domain.entity.MemberMissionEntity.builder()
                            .memberId(memberId)
                            .missionId(2L)
                            .missionDate(LocalDate.now())
                            .build()
            );
        }

        feedRepository.save(feed);
        log.info("Feed saved successfully for member: {}", memberId);
    }
}
