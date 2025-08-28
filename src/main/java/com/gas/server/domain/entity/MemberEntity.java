package com.gas.server.domain.entity;

import com.gas.server.domain.enums.ProfileType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname", length = 20, nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", length = 20, nullable = false)
    private ProfileType profileType;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    @Builder
    public MemberEntity(
            Long id,
            String nickname,
            ProfileType profileType,
            String fcmToken
    ) {
        this.id = id;
        this.nickname = nickname;
        this.profileType = profileType;
        this.fcmToken = fcmToken;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
