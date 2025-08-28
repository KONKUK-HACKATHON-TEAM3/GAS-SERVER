package com.gas.server.domain.repository;

import com.gas.server.domain.entity.MemberMissionEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberMissionRepository extends JpaRepository<MemberMissionEntity, Long> {

    List<MemberMissionEntity> findByMemberIdAndMissionDate(Long memberId, LocalDate missionDate);

    boolean existsByMemberIdAndMissionIdAndMissionDate(Long memberId, Long missionId, LocalDate missionDate);
}
