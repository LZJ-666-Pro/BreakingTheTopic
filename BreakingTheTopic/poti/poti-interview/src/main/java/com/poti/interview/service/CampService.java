package com.poti.interview.service;

import java.util.List;
import java.util.Map;

public interface CampService {

    List<Map<String, Object>> getCampList(Long userId);

    Map<String, Object> getCampDetail(Long userId, String campId);

    Map<String, Object> joinCamp(Long userId, String campId);

    List<Map<String, Object>> getMyCamps(Long userId);

    Map<String, Object> getCampHome(Long userId, String campId);

    Map<String, Object> checkin(Long userId, String campId);
}
