package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.AiTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("admin")
public interface AiTaskMapper extends BaseMapper<AiTask> {
}
