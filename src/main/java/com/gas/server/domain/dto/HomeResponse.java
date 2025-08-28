package com.gas.server.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public record HomeResponse(
        List<DailyMission> dailyMissionList,
        List<FamilyStory> familyStoryList,
        List<WeeklyRankingItem> weeklyRanking,
        List<FamilyMember> familyList
) {

    public static HomeResponse of(
            final List<DailyMission> dailyMissionList,
            final List<FamilyStory> familyStoryList,
            final List<WeeklyRankingItem> weeklyRanking,
            final List<FamilyMember> familyList
    ) {
        return new HomeResponse(dailyMissionList, familyStoryList, weeklyRanking, familyList);
    }
}
