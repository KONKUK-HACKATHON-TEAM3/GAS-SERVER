package com.gas.server.domain.dto;

import java.util.List;

public record WeeklyRankingResponse(
        String weeklyPrize,
        List<WeeklyRankingItem> weeklyRanking
) {

    public static WeeklyRankingResponse of(
            final String weeklyPrize,
            final List<WeeklyRankingItem> weeklyRanking
    ) {
        return new WeeklyRankingResponse(weeklyPrize, weeklyRanking);
    }
}
