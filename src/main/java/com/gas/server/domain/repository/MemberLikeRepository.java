package com.gas.server.domain.repository;

import com.gas.server.domain.entity.MemberLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberLikeRepository extends JpaRepository<MemberLikeEntity, Long> {

}
