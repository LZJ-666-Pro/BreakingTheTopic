package com.poti.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.user.entity.CheckinRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {
    
    @Select("SELECT * FROM checkin_record WHERE user_id = #{userId} AND checkin_date = #{date} AND deleted = 0")
    CheckinRecord findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Select("SELECT COUNT(*) FROM checkin_record WHERE user_id = #{userId} AND checkin_date >= #{startDate} AND deleted = 0")
    int countConsecutiveDays(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}
