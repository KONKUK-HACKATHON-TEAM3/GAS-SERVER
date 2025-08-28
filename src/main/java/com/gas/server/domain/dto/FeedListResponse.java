package com.gas.server.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public record FeedListResponse(
        List<Feed> feedList
) {

    public static FeedListResponse of(final List<Feed> feedList) {
        return new FeedListResponse(feedList);
    }
}
