package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 小程序侧系统配置写入（跨库 poti_user.system_config）
 *
 * 后台「系统配置」保存 contact_email 时同步到 user 库，
 * 供小程序「联系我们」页（GET /config/contact）读取。
 */
@DS("user")
@Mapper
public interface UserConfigMapper {

    @Update("UPDATE system_config SET config_value = #{value} WHERE config_key = #{key}")
    int updateValue(@Param("key") String key, @Param("value") String value);

    @Insert("INSERT INTO system_config (config_key, config_value, config_desc) VALUES (#{key}, #{value}, #{desc})")
    int insertValue(@Param("key") String key, @Param("value") String value, @Param("desc") String desc);
}
