package com.poti.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.admin.dto.UserResetDataRequest;
import com.poti.admin.entity.User;
import com.poti.admin.mapper.UserDataCleanMapper;
import com.poti.admin.mapper.UserMapper;
import com.poti.admin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserDataCleanMapper userDataCleanMapper;

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

    @Override
    public Map<String, Integer> resetData(Long id, UserResetDataRequest request) {
        User user = this.getById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (request == null || !request.hasAny()) {
            throw new IllegalArgumentException("请至少勾选一个要重置的数据维度");
        }

        // 顺序：先删明细与点赞，再清统计，保证聚合数据最后落定
        Map<String, Integer> result = new LinkedHashMap<>();
        if (Boolean.TRUE.equals(request.getPractice())) {
            result.put("practice", userDataCleanMapper.deletePracticeRecords(id));
            result.put("stats", userDataCleanMapper.resetUserStats(id));
        }
        if (Boolean.TRUE.equals(request.getWrongbook())) {
            result.put("wrongbook", userDataCleanMapper.deleteWrongbookRecords(id));
        }
        if (Boolean.TRUE.equals(request.getFavorite())) {
            result.put("favorite", userDataCleanMapper.deleteFavoriteRecords(id));
        }
        if (Boolean.TRUE.equals(request.getCheckin())) {
            result.put("checkin", userDataCleanMapper.deleteCheckinRecords(id));
            // 签到被清空后连续天数与积分必须归零，否则聚合统计与明细不一致
            if (!Boolean.TRUE.equals(request.getPractice())) {
                result.put("stats", userDataCleanMapper.resetUserStats(id));
            }
        }
        if (Boolean.TRUE.equals(request.getDiscussion())) {
            result.put("discussionLike", userDataCleanMapper.deleteDiscussionLikes(id));
            result.put("discussion", userDataCleanMapper.deleteDiscussions(id));
        }
        return result;
    }
}
