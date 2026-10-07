package com.poti.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.admin.dto.UserResetDataRequest;
import com.poti.admin.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {

    Page<User> pageList(int page, int size, String nickname, Integer status);

    boolean updateStatus(Long id, Integer status);

    boolean deleteUser(Long id);

    /**
     * 重置用户学习数据（按维度勾选，物理删除）。
     *
     * @return 各维度清理结果：维度名 -> 删除行数；学习统计归零记为 "stats"
     */
    Map<String, Integer> resetData(Long id, UserResetDataRequest request);
}
