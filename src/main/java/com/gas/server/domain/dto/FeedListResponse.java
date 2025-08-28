package com.gas.server.domain.dto;

import java.util.List;

public record FeedListResponse(
        List<Feed> feedList
) {

    public static FeedListResponse of(final List<Feed> feedList) {
        return new FeedListResponse(feedList);
    }
}
