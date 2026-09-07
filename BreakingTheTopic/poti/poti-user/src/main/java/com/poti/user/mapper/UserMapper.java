package com.poti.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据微信 openid 查询用户
     * @param openid 微信 openid
     * @return 用户实体
     */
    @Select("SELECT * FROM user WHERE openid = #{openid} AND deleted = 0")
    User selectByOpenid(String openid);
    
    /**
     * 根据唯一ID查询用户
     * @param uniqueId 唯一ID
     * @return 用户实体
     */
    @Select("SELECT * FROM user WHERE unique_id = #{uniqueId} AND deleted = 0")
    User selectByUniqueId(String uniqueId);
    
    /**
     * 更新用户的唯一ID
     * @param userId 用户ID
     * @param uniqueId 唯一ID
     * @return 影响行数
     */
    @Update("UPDATE user SET unique_id = #{uniqueId}, update_time = NOW() WHERE id = #{userId} AND deleted = 0")
    int updateUniqueId(@Param("userId") Long userId, @Param("uniqueId") String uniqueId);
}