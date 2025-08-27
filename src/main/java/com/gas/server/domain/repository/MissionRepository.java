package com.gas.server.domain.repository;

import com.gas.server.domain.entity.MissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<MissionEntity, Long> {

}
