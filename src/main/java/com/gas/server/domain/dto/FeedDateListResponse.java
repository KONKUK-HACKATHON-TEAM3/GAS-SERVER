package com.gas.server.domain.dto;

import java.time.LocalDate;
import java.util.List;

public record FeedDateListResponse(
        List<LocalDate> dateList
) {
    
    public static FeedDateListResponse of(List<LocalDate> dateList) {
        return new FeedDateListResponse(dateList);
    }
}