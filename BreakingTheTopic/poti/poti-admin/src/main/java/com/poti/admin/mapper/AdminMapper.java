package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
@DS("admin")
public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT * FROM sys_admin WHERE username = #{username} AND deleted = 0")
    Admin selectByUsername(String username);
}
