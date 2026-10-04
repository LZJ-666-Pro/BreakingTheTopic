package com.poti.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.admin.entity.User;

public interface UserService extends IService<User> {

    Page<User> pageList(int page, int size, String nickname, Integer status);

    boolean updateStatus(Long id, Integer status);

    boolean deleteUser(Long id);
}
