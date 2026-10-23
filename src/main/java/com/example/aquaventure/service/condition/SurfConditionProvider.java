package com.example.aquaventure.service.condition;

import java.util.List;

/** Behind an interface so the weather/marine data source can be swapped without touching callers. */
public interface SurfConditionProvider {

    List<DailyCondition> getConditions(double latitude, double longitude);
}
