package com.poti.admin.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    Map<String, Object> getOverview();

    Map<String, Object> getQuestionStats();

    Map<String, Object> getUserStats();
     
    List<Map<String, Object>> getCategoryStats();

    List<Map<String, Object>> getRecentActiveUsers();
}
