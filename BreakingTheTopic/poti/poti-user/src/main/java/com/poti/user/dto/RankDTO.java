package com.poti.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class RankDTO {
    private List<RankItemDTO> topThree;
    private List<RankItemDTO> rankList;
    private RankItemDTO myRank;
    private Boolean hasMore;
    
    @Data
    public static class RankItemDTO {
        private Long id;
        private Integer rank;
        private String nickname;
        private String avatarUrl;
        private Object score;
    }
}
