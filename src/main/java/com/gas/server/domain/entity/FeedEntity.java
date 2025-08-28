package com.gas.server.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feed")
public class FeedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "image_url", columnDefinition = "text", nullable = false)
    private String imageUrl;

    @Column(name = "text", length = 200)
    private String text;

    @Column(name = "tag", length = 50)
    private String tag;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void updateTag(String tag) {
        this.tag = tag;
    }

    @Builder
    public FeedEntity(
            Long id,
            Long memberId,
            String imageUrl,
            String text,
            String tag,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.imageUrl = imageUrl;
        this.text = text;
        this.tag = tag;
        this.createdAt = createdAt;
    }
}
