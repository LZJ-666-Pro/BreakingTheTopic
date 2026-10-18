package com.poti.user.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 后台系统配置读取（跨库 poti_admin.sys_config）
 *
 * 后台「系统配置」页保存的行为开关（register_enabled / discussion_enabled 等）
 * 存在 poti_admin.sys_config，业务侧通过本 Mapper 实时读取。
 */
@DS("admin")
@Mapper
public interface AdminConfigMapper {

    /** 按配置键读取启用的配置值，不存在返回 null（业务按默认开启处理） */
    @Select("SELECT config_value FROM sys_config WHERE config_key = #{key} AND status = 1 AND deleted = 0 LIMIT 1")
    String selectValueByKey(@Param("key") String key);

    /** 批量读取配置键值对（网站信息展示等场景，一次跨库查询） */
    @Select("<script>SELECT config_key AS configKey, config_value AS configValue FROM sys_config " +
            "WHERE config_key IN <foreach collection='keys' item='k' open='(' separator=',' close=')'>#{k}</foreach> " +
            "AND status = 1 AND deleted = 0</script>")
    List<Map<String, Object>> selectValuesByKeys(@Param("keys") List<String> keys);

    /** 开关是否开启：配置缺失视为开启（默认放行），显式 false 才拦截 */
    default boolean isEnabled(String key) {
        String v = selectValueByKey(key);
        return v == null || !v.trim().equalsIgnoreCase("false");
    }
}
