package com.gas.server.domain.service;

import com.gas.server.domain.dto.DailyMission;
import com.gas.server.domain.dto.FamilyMember;
import com.gas.server.domain.dto.FamilyStory;
import com.gas.server.domain.dto.HomeResponse;
import com.gas.server.domain.dto.SignUpResponse;
import com.gas.server.domain.dto.WeeklyRanking;
import com.gas.server.domain.dto.WeeklyRankingResponse;
import com.gas.server.domain.entity.FeedEntity;
import com.gas.server.domain.entity.MemberEntity;
import com.gas.server.domain.entity.MemberMissionEntity;
import com.gas.server.domain.entity.MissionEntity;
import com.gas.server.domain.enums.ProfileType;
import com.gas.server.domain.repository.FeedRepository;
import com.gas.server.domain.repository.MemberMissionRepository;
import com.gas.server.domain.repository.MemberRepository;
import com.gas.server.domain.repository.MissionRepository;
import com.gas.server.global.exception.BusinessException;
import com.gas.server.global.exception.ErrorType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final MissionRepository missionRepository;
    private final MemberMissionRepository memberMissionRepository;

    @Transactional
    public SignUpResponse signUp(final String nickname, final ProfileType profileType) {
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

    @Transactional
    public HomeResponse getHome(final Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorType.NOT_FOUND_MEMBER_ERROR);
        }

        if (!memberMissionRepository.existsByMemberIdAndMissionIdAndMissionDate(memberId, 1L, LocalDate.now())) {
            memberMissionRepository.save(
                    MemberMissionEntity.builder()
                            .memberId(memberId)
                            .missionId(1L)
                            .missionDate(LocalDate.now())
                            .build()
            );
        }

        List<DailyMission> dailyMissions = getDailyMissions(memberId);

        List<FamilyStory> familyStories = getFamilyStories(memberId);

        WeeklyRanking weeklyRanking = getWeeklyRanking(memberId);

        List<FamilyMember> familyMembers = getFamilyMembers(memberId);

        return HomeResponse.of(dailyMissions, familyStories, weeklyRanking, familyMembers);
    }

    private List<DailyMission> getDailyMissions(final Long memberId) {
        LocalDate today = LocalDate.now();

        List<MissionEntity> allMissions = missionRepository.findAll();

        List<MemberMissionEntity> completedMissions = memberMissionRepository
                .findByMemberIdAndMissionDate(memberId, today);

        Set<Long> completedMissionIds = completedMissions.stream()
                .map(MemberMissionEntity::getMissionId)
                .collect(Collectors.toSet());

        return allMissions.stream()
                .map(mission -> DailyMission.of(
                        mission.getName(),
                        mission.getPoint(),
                        mission.getDescription(),
                        completedMissionIds.contains(mission.getId()),
                        mission.getRoute()
                ))
                .toList();
    }

    private List<FamilyStory> getFamilyStories(final Long memberId) {
        List<FeedEntity> recentFeeds = feedRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(4)
                .toList();

        return recentFeeds.stream()
                .map(feed -> {
                    String nickname = feed.getMemberId().equals(memberId)
                            ? "나"
                            : memberRepository.findByIdOrElseThrow(feed.getMemberId()).getNickname();

                    return FamilyStory.of(
                            nickname,
                            feed.getImageUrl(),
                            feed.getCreatedAt()
                    );
                })
                .toList();
    }

    private WeeklyRanking getWeeklyRanking(final Long memberId) {
        List<MemberMissionEntity> allCompletedMissions = memberMissionRepository.findAll();
        List<MissionEntity> allMissions = missionRepository.findAll();

        // 미션 ID별 포인트 매핑
        Map<Long, Integer> missionPointsMap = allMissions.stream()
                .collect(Collectors.toMap(MissionEntity::getId, MissionEntity::getPoint));

        // 멤버별 총 점수 계산
        Map<Long, Integer> memberScores = allCompletedMissions.stream()
                .collect(Collectors.groupingBy(
                        MemberMissionEntity::getMemberId,
                        Collectors.summingInt(mm -> missionPointsMap.getOrDefault(mm.getMissionId(), 0))
                ));

        // 가장 높은 점수를 가진 멤버 찾기
        if (memberScores.isEmpty()) {
            return WeeklyRanking.of("나", 0);
        }

        Map.Entry<Long, Integer> topEntry = memberScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(Map.entry(memberId, 0));

        Long topMemberId = topEntry.getKey();
        Integer topScore = topEntry.getValue();

        // 닉네임 가져오기
        String nickname;

        if (topMemberId.equals(memberId)) {
            nickname = "나";
        } else {
            MemberEntity topMember = memberRepository.findByIdOrElseThrow(topMemberId);
            nickname = topMember.getNickname();
        }

        return WeeklyRanking.of(nickname, topScore);
    }

    private List<FamilyMember> getFamilyMembers(final Long memberId) {
        List<MemberEntity> allMembers = memberRepository.findAll();

        return allMembers.stream()
                .map(member -> {
                    String nickname = member.getId().equals(memberId)
                            ? "나"
                            : member.getNickname();

                    return FamilyMember.of(nickname, member.getProfileType());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public WeeklyRankingResponse getRanking(final Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorType.NOT_FOUND_MEMBER_ERROR);
        }

        List<MemberMissionEntity> allCompletedMissions = memberMissionRepository.findAll();
        List<MissionEntity> allMissions = missionRepository.findAll();

        // 미션 ID별 포인트 매핑
        Map<Long, Integer> missionPointsMap = allMissions.stream()
                .collect(Collectors.toMap(MissionEntity::getId, MissionEntity::getPoint));

        // 멤버별 총 점수 계산
        Map<Long, Integer> memberScores = allCompletedMissions.stream()
                .collect(Collectors.groupingBy(
                        MemberMissionEntity::getMemberId,
                        Collectors.summingInt(mm -> missionPointsMap.getOrDefault(mm.getMissionId(), 0))
                ));

        // 모든 멤버 조회
        List<MemberEntity> allMembers = memberRepository.findAll();

        // 모든 멤버의 점수를 포함한 랭킹 리스트 생성 (점수가 없으면 0점)
        List<WeeklyRanking> rankings = allMembers.stream()
                .map(member -> {
                    Integer score = memberScores.getOrDefault(member.getId(), 0);
                    String nickname = member.getId().equals(memberId) ? "나" : member.getNickname();
                    return WeeklyRanking.of(nickname, score);
                })
                .sorted((a, b) -> b.score().compareTo(a.score())) // 점수 내림차순 정렬
                .toList();

        // 이번주 경품
        String weeklyPrize = "설거지 1회 면제권";

        return WeeklyRankingResponse.of(weeklyPrize, rankings);
    }
}
