package com.gas.server.domain.service;

import com.gas.server.domain.entity.MemberMissionEntity;
import com.gas.server.domain.entity.MissionEntity;
import com.gas.server.domain.repository.MemberMissionRepository;
import com.gas.server.domain.repository.MemberRepository;
import com.gas.server.domain.repository.MissionRepository;
import com.gas.server.global.fcm.FcmService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {

    private final MemberMissionRepository memberMissionRepository;
    private final MissionRepository missionRepository;
    private final MemberRepository memberRepository;
    private final FcmService fcmService;

    @Transactional
    public void completeMission(Long memberId, Long missionId) {
        // 이미 오늘 완료한 미션인지 확인
        if (memberMissionRepository.existsByMemberIdAndMissionIdAndMissionDate(memberId, missionId, LocalDate.now())) {
            log.info("Mission {} already completed today by member {}", missionId, memberId);
            return;
        }

        // 미션 완료 전 랭킹 계산
        Long previousTopMemberId = getCurrentTopMemberId();
        boolean wasTopMemberBefore = memberId.equals(previousTopMemberId);

        // 미션 완료 저장
        memberMissionRepository.save(
                MemberMissionEntity.builder()
                        .memberId(memberId)
                        .missionId(missionId)
                        .missionDate(LocalDate.now())
                        .build()
        );
        log.info("Mission {} completed by member {}", missionId, memberId);

        // 미션 완료 후 랭킹 계산
        Long currentTopMemberId = getCurrentTopMemberId();
        boolean isTopMemberNow = memberId.equals(currentTopMemberId);

        // 미션을 완료한 사람이 1등이 아니었다가 1등이 된 경우
        if (!wasTopMemberBefore && isTopMemberNow) {
            log.info("Member {} became the new top member!", memberId);
            fcmService.sendRankingChangeNotification(memberId);
        }
    }

    private Long getCurrentTopMemberId() {
        Map<Long, Integer> memberScores = calculateMemberScores();

        return memberScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Map<Long, Integer> calculateMemberScores() {
        List<MemberMissionEntity> allCompletedMissions = memberMissionRepository.findAll();
        List<MissionEntity> allMissions = missionRepository.findAll();

        // 미션 ID별 포인트 매핑
        Map<Long, Integer> missionPointsMap = allMissions.stream()
                .collect(Collectors.toMap(MissionEntity::getId, MissionEntity::getPoint));

        // 멤버별 총 점수 계산
        return allCompletedMissions.stream()
                .collect(Collectors.groupingBy(
                        MemberMissionEntity::getMemberId,
                        Collectors.summingInt(mm -> missionPointsMap.getOrDefault(mm.getMissionId(), 0))
                ));
    }
}
