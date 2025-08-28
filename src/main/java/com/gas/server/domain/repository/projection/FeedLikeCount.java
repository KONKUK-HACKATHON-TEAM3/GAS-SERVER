package com.gas.server.domain.repository.projection;

public interface FeedLikeCount {

    Long getFeedId();

    long getCnt();

    default int getCountAsInt() {
        return Math.toIntExact(getCnt());
    }
}
