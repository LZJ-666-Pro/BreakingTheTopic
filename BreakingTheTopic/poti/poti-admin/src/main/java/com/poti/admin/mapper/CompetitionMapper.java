package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.Competition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 竞赛 Mapper（跨库 poti_practice.competition）。
 */
@Mapper
@DS("practice")
public interface CompetitionMapper extends BaseMapper<Competition> {
}
