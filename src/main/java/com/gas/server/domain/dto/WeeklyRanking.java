package com.gas.server.domain.dto;

public record WeeklyRanking(
        String nickname,
        Integer score
) {

    public static WeeklyRanking of(final String nickname, final Integer score) {
        return new WeeklyRanking(nickname, score);
    }
}
