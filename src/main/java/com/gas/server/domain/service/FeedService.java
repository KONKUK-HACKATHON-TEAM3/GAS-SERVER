package com.gas.server.domain.service;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.gas.server.domain.dto.Feed;
import com.gas.server.domain.dto.FeedListResponse;
import com.gas.server.domain.entity.FeedEntity;
import com.gas.server.domain.entity.MemberEntity;
import com.gas.server.domain.repository.FeedRepository;
import com.gas.server.domain.repository.MemberLikeRepository;
import com.gas.server.domain.repository.MemberRepository;
import com.gas.server.domain.repository.projection.FeedLikeCount;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
    private final MemberLikeRepository memberLikeRepository;

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
}
