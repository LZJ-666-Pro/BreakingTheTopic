package com.poti.admin.service;

import com.poti.admin.entity.Admin;

public interface AdminService {

    Admin login(String username, String password);

    Admin getById(Long id);

    Admin getByUsername(String username);

    void save(Admin admin);

    void updateLastLogin(Long id, String ip);

    void updateById(Admin admin);
}
