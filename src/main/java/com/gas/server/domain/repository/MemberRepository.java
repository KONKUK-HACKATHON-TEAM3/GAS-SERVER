package com.gas.server.domain.repository;

import com.gas.server.domain.entity.MemberEntity;
import com.gas.server.global.exception.BusinessException;
import com.gas.server.global.exception.ErrorType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    boolean existsByNickname(String nickname);

    default MemberEntity findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(
                () -> new BusinessException(ErrorType.NOT_FOUND_MEMBER_ERROR)
        );
    }
}
