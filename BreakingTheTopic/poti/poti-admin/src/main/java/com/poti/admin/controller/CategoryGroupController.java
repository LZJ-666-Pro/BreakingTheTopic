package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.CategoryGroup;
import com.poti.admin.entity.CategoryGroupRel;
import com.poti.admin.mapper.CategoryGroupMapper;
import com.poti.admin.mapper.CategoryGroupRelMapper;
import com.poti.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 题库分组标签接口（后台）。
 * <p>
 * 管理小程序题库页顶部的筛选标签（热门/后端/前端/...），
 * 分类与分组标签的绑定在分类接口中维护。
 * </p>
 */
@RestController
@RequestMapping("/admin/category-group")
@RequiredArgsConstructor
public class CategoryGroupController {

    private final CategoryGroupMapper groupMapper;
    private final CategoryGroupRelMapper relMapper;

    /**
     * 分页分组标签（含每个分组下分类数量），按 sort 升序。
     */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<CategoryGroup> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(CategoryGroup::getName, name.trim());
        }
        wrapper.orderByAsc(CategoryGroup::getSort).orderByAsc(CategoryGroup::getId);
        Page<CategoryGroup> page = groupMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (CategoryGroup group : page.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", group.getId());
            item.put("name", group.getName());
            item.put("groupKey", group.getGroupKey());
            item.put("sort", group.getSort());
            item.put("status", group.getStatus());
            item.put("createTime", group.getCreateTime());
            item.put("categoryCount", relMapper.selectCount(
                    new LambdaQueryWrapper<CategoryGroupRel>().eq(CategoryGroupRel::getGroupId, group.getId())));
            list.add(item);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        return R.success(data);
    }

    /**
     * 新增分组标签（名称与标识均查重）。
     */
    @PostMapping
    public R<Void> create(@RequestBody CategoryGroup group) {
        String name = group.getName() == null ? "" : group.getName().trim();
        String key = group.getGroupKey() == null ? "" : group.getGroupKey().trim();
        if (name.isEmpty() || key.isEmpty()) {
            return R.error("分组名称和标识不能为空");
        }
        if (!key.matches("[a-zA-Z0-9_-]+")) {
            return R.error("分组标识只能包含字母、数字、下划线和短横线");
        }
        if (existsName(name, null) || existsKey(key, null)) {
            return R.error("分组名称或标识已存在");
        }
        group.setName(name);
        group.setGroupKey(key);
        if (group.getSort() == null) {
            group.setSort(0);
        }
        if (group.getStatus() == null) {
            group.setStatus(1);
        }
        group.setId(null);
        groupMapper.insert(group);
        return R.success();
    }

    /**
     * 编辑分组标签。
     */
    @PutMapping
    public R<Void> update(@RequestBody CategoryGroup group) {
        if (group.getId() == null) {
            return R.error("缺少分组 ID");
        }
        CategoryGroup db = groupMapper.selectById(group.getId());
        if (db == null) {
            return R.error("分组不存在或已被删除");
        }
        String name = group.getName() == null ? "" : group.getName().trim();
        if (name.isEmpty()) {
            return R.error("分组名称不能为空");
        }
        if (existsName(name, group.getId())) {
            return R.error("分组名称「" + name + "」已存在");
        }
        db.setName(name);
        if (group.getSort() != null) {
            db.setSort(group.getSort());
        }
        if (group.getStatus() != null) {
            db.setStatus(group.getStatus());
        }
        groupMapper.updateById(db);
        return R.success();
    }

    /**
     * 删除分组标签（逻辑删除，并解除与分类的绑定）。
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        CategoryGroup db = groupMapper.selectById(id);
        if (db == null) {
            return R.error("分组不存在或已被删除");
        }
        groupMapper.deleteById(id);
        relMapper.delete(new LambdaQueryWrapper<CategoryGroupRel>().eq(CategoryGroupRel::getGroupId, id));
        return R.success();
    }

    private boolean existsName(String name, Long excludeId) {
        LambdaQueryWrapper<CategoryGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryGroup::getName, name);
        if (excludeId != null) {
            wrapper.ne(CategoryGroup::getId, excludeId);
        }
        return groupMapper.selectCount(wrapper) > 0;
    }

    private boolean existsKey(String key, Long excludeId) {
        LambdaQueryWrapper<CategoryGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryGroup::getGroupKey, key);
        if (excludeId != null) {
            wrapper.ne(CategoryGroup::getId, excludeId);
        }
        return groupMapper.selectCount(wrapper) > 0;
    }
}
