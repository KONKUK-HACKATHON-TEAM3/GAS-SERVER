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

    @Query(value = """
            SELECT DISTINCT CAST(f.created_at AS date) 
            FROM feed f
            WHERE f.created_at >= :startDate
              AND f.created_at < :endDate
            ORDER BY CAST(f.created_at AS date)
            """, nativeQuery = true)
    List<LocalDate> findDistinctDatesByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
