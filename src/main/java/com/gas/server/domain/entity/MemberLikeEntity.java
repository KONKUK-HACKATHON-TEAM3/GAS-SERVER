package com.gas.server.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "member_like",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_member_like_member_id_feed_id",
                columnNames = {"member_id", "feed_id"}
        )
)
public class MemberLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "feed_id", nullable = false)
    private Long feedId;

    @Builder
    public MemberLikeEntity(
            Long id,
            Long memberId,
            Long feedId
    ) {
        this.id = id;
        this.memberId = memberId;
        this.feedId = feedId;
    }
}
