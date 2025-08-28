package com.gas.server.domain.service;

import com.gas.server.domain.dto.DailyMission;
import com.gas.server.domain.dto.FamilyMember;
import com.gas.server.domain.dto.FamilyStory;
import com.gas.server.domain.dto.HomeResponse;
import com.gas.server.domain.dto.SignUpResponse;
import com.gas.server.domain.dto.WeeklyRanking;
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

    private List<DailyMission> getDailyMissions(Long memberId) {
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
                        completedMissionIds.contains(mission.getId())
                ))
                .toList();
    }

    private List<FamilyStory> getFamilyStories(Long memberId) {
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

    private WeeklyRanking getWeeklyRanking(Long memberId) {
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

    private List<FamilyMember> getFamilyMembers(Long memberId) {
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
}
