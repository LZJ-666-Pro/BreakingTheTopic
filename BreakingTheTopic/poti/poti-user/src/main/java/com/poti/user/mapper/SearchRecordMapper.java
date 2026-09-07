package com.poti.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.user.entity.SearchRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SearchRecordMapper extends BaseMapper<SearchRecord> {

    @Select("SELECT keyword, search_count, user_count FROM search_record " +
            "ORDER BY (search_count * 0.6 + user_count * 0.4) DESC, last_search_time DESC " +
            "LIMIT #{limit}")
    List<SearchRecord> selectHotKeywords(@Param("limit") int limit);
}
