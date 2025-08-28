package com.gas.server.domain.repository;

import com.gas.server.domain.entity.FeedEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<FeedEntity, Long> {

    List<FeedEntity> findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            LocalDateTime start,
            LocalDateTime end
    );

    List<FeedEntity> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
