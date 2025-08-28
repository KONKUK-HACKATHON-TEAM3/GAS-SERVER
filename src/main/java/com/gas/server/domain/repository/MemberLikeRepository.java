package com.gas.server.domain.repository;

import com.gas.server.domain.entity.MemberLikeEntity;
import com.gas.server.domain.repository.projection.FeedLikeCount;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberLikeRepository extends JpaRepository<MemberLikeEntity, Long> {

    @Query("""
            SELECT ml.feedId
            FROM MemberLikeEntity ml
            WHERE ml.memberId = :memberId
              AND ml.feedId IN :feedIds
            """)
    Set<Long> findLikedFeedIds(
            @Param("memberId") Long memberId,
            @Param("feedIds") List<Long> feedIds
    );

    @Query("""
            SELECT ml.feedId AS feedId, COUNT(ml) AS cnt
            FROM MemberLikeEntity ml
            WHERE ml.feedId IN :feedIds
            GROUP BY ml.feedId
            """)
    List<FeedLikeCount> countByFeedIds(
            @Param("feedIds") List<Long> feedIds
    );

    boolean existsByMemberIdAndFeedId(Long memberId, Long feedId);

    void deleteByMemberIdAndFeedId(Long memberId, Long feedId);
}
