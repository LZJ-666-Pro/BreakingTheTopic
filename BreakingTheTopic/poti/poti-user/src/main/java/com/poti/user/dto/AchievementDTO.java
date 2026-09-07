package com.poti.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class AchievementDTO {
    private AchievementSummaryDTO summary;
    private List<AchievementItemDTO> achievements;
    
    @Data
    public static class AchievementSummaryDTO {
        private Integer total;
        private Integer achieved;
        private Integer points;
    }
    
    @Data
    public static class AchievementItemDTO {
        private Long id;
        private String name;
        private String icon;
        private String description;
        private String category;
        private String type;
        private Integer conditionValue;
        private Integer points;
        private Boolean achieved;
        private ProgressDTO progress;
        private String hint;
    }
    
    @Data
    public static class ProgressDTO {
        private Integer current;
        private Integer total;
        private Integer percent;
    }
}
