package com.gas.server.domain.dto;

public record WeeklyRankingItem(
        String nickname,
        Integer score
) {

    public static WeeklyRankingItem of(
            final String nickname,
            final Integer score
    ) {
        return new WeeklyRankingItem(nickname, score);
    }
}
