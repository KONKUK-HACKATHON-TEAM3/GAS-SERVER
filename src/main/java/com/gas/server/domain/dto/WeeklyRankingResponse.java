package com.gas.server.domain.dto;

import java.util.List;

public record WeeklyRankingResponse(
        String weeklyPrize,
        List<WeeklyRanking> weeklyRanking
) {

    public static WeeklyRankingResponse of(
            final String weeklyPrize,
            final List<WeeklyRanking> weeklyRanking
    ) {
        return new WeeklyRankingResponse(weeklyPrize, weeklyRanking);
    }
}
