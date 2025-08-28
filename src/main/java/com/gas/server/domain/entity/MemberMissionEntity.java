package com.gas.server.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "member_mission",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_member_mission_member_id_mission_id_mission_date",
                columnNames = {"member_id", "mission_id", "mission_date"}
        )
)
public class MemberMissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "mission_id", nullable = false)
    private Long missionId;

    @Column(name = "mission_date", nullable = false)
    private LocalDate missionDate;

    @PrePersist
    public void prePersist() {
        this.missionDate = LocalDate.now();
    }

    @Builder
    public MemberMissionEntity(
            Long id,
            Long memberId,
            Long missionId,
            LocalDate missionDate
    ) {
        this.id = id;
        this.memberId = memberId;
        this.missionId = missionId;
        this.missionDate = missionDate;
    }
}
