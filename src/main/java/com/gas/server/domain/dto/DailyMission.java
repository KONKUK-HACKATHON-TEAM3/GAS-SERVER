package com.gas.server.domain.dto;

public record DailyMission(
        String name,
        Integer point,
        String description,
        Boolean clearStatus
) {

    public static DailyMission of(
            final String name,
            final Integer point,
            final String description,
            final Boolean clearStatus
    ) {
        return new DailyMission(name, point, description, clearStatus);
    }
}
