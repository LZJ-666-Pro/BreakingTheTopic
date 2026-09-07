package com.poti.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.User;
import com.poti.admin.service.UserService;
import com.poti.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/page")
    public R<Page<User>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Integer status) {
        Page<User> result = userService.pageList(page, size, nickname, status);
        return R.success(result);
    }

    @GetMapping("/{id}")
    public R<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return R.error("用户不存在");
        }
        return R.success(user);
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = userService.updateStatus(id, status);
        return success ? R.success(null) : R.error("操作失败");
    }
}
