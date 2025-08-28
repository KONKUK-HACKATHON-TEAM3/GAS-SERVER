package com.gas.server.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public record DailyMission(
        String name,
        Integer point,
        String description,
        Boolean clearStatus,
        String route
) {

    public static DailyMission of(
            final String name,
            final Integer point,
            final String description,
            final Boolean clearStatus,
            final String route
    ) {
        return new DailyMission(name, point, description, clearStatus, route);
    }
}
