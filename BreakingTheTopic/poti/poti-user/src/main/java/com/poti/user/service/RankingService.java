package com.poti.user.service;

import com.poti.common.utils.Result;
import com.poti.user.dto.RankDTO;

public interface RankingService {
    
    Result<RankDTO> getRanking(Long userId, String type, Integer page, Integer limit);
    
    Result<RankDTO> getFriendRanking(Long userId, String type, Integer page, Integer limit);
}
