package com.poti.admin.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.poti.admin.entity.Admin;
import com.poti.admin.mapper.AdminMapper;
import com.poti.admin.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Admin login(String username, String password) {
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            return null;
        }
        if (admin.getStatus() != 1) {
            return null;
        }
        if (!BCrypt.checkpw(password, admin.getPassword())) {
            return null;
        }
        return admin;
    }

    @Override
    public Admin getById(Long id) {
        return adminMapper.selectById(id);
    }

    @Override
    public Admin getByUsername(String username) {
        return adminMapper.selectByUsername(username);
    }

    @Override
    public void save(Admin admin) {
        adminMapper.insert(admin);
    }

    @Override
    public void updateLastLogin(Long id, String ip) {
        UpdateWrapper<Admin> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.set("last_login_time", LocalDateTime.now());
        updateWrapper.set("last_login_ip", ip);
        adminMapper.update(null, updateWrapper);
    }

    @Override
    public void updateById(Admin admin) {
        adminMapper.updateById(admin);
    }
}
