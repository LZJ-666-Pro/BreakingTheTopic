package com.poti.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.admin.entity.User;
import com.poti.admin.mapper.UserMapper;
import com.poti.admin.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Page<User> pageList(int page, int size, String nickname, Integer status) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(nickname)) {
            wrapper.like(User::getNickname, nickname);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByAsc(User::getId);
        return this.page(pageParam, wrapper);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        return this.updateById(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        return this.removeById(id);
    }
}
