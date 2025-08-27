package com.gas.server.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "mission")
public class MissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "point", nullable = false)
    private Integer point;

    @Column(name = "description", length = 50, nullable = false)
    private String description;

    @Builder
    public MissionEntity(
            Long id,
            String name,
            Integer point,
            String description
    ) {
        this.id = id;
        this.name = name;
        this.point = point;
        this.description = description;
    }
}
