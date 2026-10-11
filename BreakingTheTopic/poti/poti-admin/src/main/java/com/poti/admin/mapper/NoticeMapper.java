package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告 Mapper（poti_admin 库，默认数据源）。
 */
@Mapper
@DS("admin")
public interface NoticeMapper extends BaseMapper<Notice> {
}
