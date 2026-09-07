package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("user")
public interface UserMapper extends BaseMapper<User> {
}
