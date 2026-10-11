package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.admin.entity.Admin;
import com.poti.admin.entity.Role;
import com.poti.admin.mapper.AdminMapper;
import com.poti.admin.mapper.RoleMapper;
import com.poti.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/role")
public class RoleController {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private AdminMapper adminMapper;

    /** 全部启用角色（下拉选择用） */
    @GetMapping("/all")
    public R<List<Role>> all() {
        try {
            LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Role::getStatus, 1).orderByAsc(Role::getId);
            return R.success(roleMapper.selectList(wrapper));
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return R.error("获取角色列表失败");
        }
    }

    /** 角色列表（含禁用，供管理页展示） */
    @GetMapping("/list")
    public R<List<Role>> list() {
        try {
            LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(Role::getId);
            return R.success(roleMapper.selectList(wrapper));
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return R.error("获取角色列表失败");
        }
    }

    @PostMapping("/create")
    public R<Void> create(@RequestBody Role role) {
        try {
            if (role.getName() == null || role.getName().trim().isEmpty()) {
                return R.error("角色名称不能为空");
            }
            if (role.getCode() == null || role.getCode().trim().isEmpty()) {
                return R.error("角色编码不能为空");
            }
            String code = role.getCode().trim();
            if ("super".equalsIgnoreCase(code)) {
                return R.error("角色编码 super 为系统保留，请更换");
            }
            if (!code.matches("^[a-zA-Z][a-zA-Z0-9_]{1,29}$")) {
                return R.error("角色编码需以字母开头，2-30位字母/数字/下划线");
            }
            Long count = roleMapper.selectCount(new LambdaQueryWrapper<Role>().eq(Role::getCode, code));
            if (count > 0) {
                return R.error("角色编码已存在");
            }
            role.setId(null);
            role.setName(role.getName().trim());
            role.setCode(code);
            if (role.getStatus() == null) {
                role.setStatus(1);
            }
            roleMapper.insert(role);
            return R.success(null);
        } catch (Exception e) {
            log.error("创建角色失败", e);
            return R.error("创建角色失败");
        }
    }

    @PutMapping("/update")
    public R<Void> update(@RequestBody Role role) {
        try {
            if (role.getId() == null) {
                return R.error("参数错误");
            }
            Role existing = roleMapper.selectById(role.getId());
            if (existing == null) {
                return R.error("角色不存在");
            }
            if ("super".equals(existing.getCode())) {
                // 超级管理员角色仅允许修改名称/备注
                Role patch = new Role();
                patch.setId(existing.getId());
                patch.setName(role.getName() != null ? role.getName().trim() : existing.getName());
                patch.setRemark(role.getRemark() != null ? role.getRemark() : existing.getRemark());
                roleMapper.updateById(patch);
                return R.success(null);
            }
            if (role.getCode() != null && !role.getCode().trim().isEmpty()) {
                String code = role.getCode().trim();
                if (!code.equals(existing.getCode())) {
                    if ("super".equalsIgnoreCase(code)) {
                        return R.error("角色编码 super 为系统保留，请更换");
                    }
                    Long count = roleMapper.selectCount(new LambdaQueryWrapper<Role>().eq(Role::getCode, code).ne(Role::getId, role.getId()));
                    if (count > 0) {
                        return R.error("角色编码已存在");
                    }
                }
                existing.setCode(code);
            }
            if (role.getName() != null && !role.getName().trim().isEmpty()) {
                existing.setName(role.getName().trim());
            }
            if (role.getPermissions() != null) {
                existing.setPermissions(role.getPermissions());
            }
            if (role.getRemark() != null) {
                existing.setRemark(role.getRemark());
            }
            if (role.getStatus() != null) {
                existing.setStatus(role.getStatus());
            }
            roleMapper.updateById(existing);
            return R.success(null);
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return R.error("更新角色失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        try {
            Role role = roleMapper.selectById(id);
            if (role == null) {
                return R.error("角色不存在");
            }
            if ("super".equals(role.getCode())) {
                return R.error("超级管理员角色不允许删除");
            }
            Long used = adminMapper.selectCount(new LambdaQueryWrapper<Admin>().eq(Admin::getRoleId, id));
            if (used > 0) {
                return R.error("该角色已分配给 " + used + " 个管理员，请先调整后再删除");
            }
            roleMapper.deleteById(id);
            return R.success(null);
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return R.error("删除角色失败");
        }
    }
}
