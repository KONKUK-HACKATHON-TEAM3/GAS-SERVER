package com.gas.server.domain.repository;

import com.gas.server.domain.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    boolean existsByNickname(String nickname);
}
