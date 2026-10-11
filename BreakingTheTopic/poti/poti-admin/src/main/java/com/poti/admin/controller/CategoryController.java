package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.Category;
import com.poti.admin.entity.CategoryGroup;
import com.poti.admin.entity.CategoryGroupRel;
import com.poti.admin.mapper.CategoryGroupMapper;
import com.poti.admin.mapper.CategoryGroupRelMapper;
import com.poti.admin.mapper.CategoryMapper;
import com.poti.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 题库分类（题型）管理接口（后台）。
 * <p>
 * 分页返回附带每分类题目数量与所属分组标签；
 * 新增/编辑时可同时维护所属分组标签（category_group_rel）。
 * </p>
 */
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;
    private final CategoryGroupMapper groupMapper;
    private final CategoryGroupRelMapper relMapper;

    /**
     * 分页列表：关键字筛选，按 sort 升序，附带题数与所属分组标签。
     */
    @GetMapping("/page")
    public R<Map<String, Object>> pageList(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name) {

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(Category::getName, name.trim());
        }
        wrapper.orderByAsc(Category::getSort).orderByAsc(Category::getId);
        Page<Category> result = categoryMapper.selectPage(new Page<>(page, size), wrapper);

        Map<Long, String> groupNames = groupKeyMap();
        List<Map<String, Object>> records = new ArrayList<>();
        for (Category category : result.getRecords()) {
            records.add(toItem(category, groupNames));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", records);
        data.put("total", result.getTotal());
        return R.success(data);
    }

    /**
     * 全部分类（题库表单下拉用），附带所属分组标识。
     */
    @GetMapping("/list")
    public R<List<Map<String, Object>>> listAll() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort).orderByAsc(Category::getId);
        Map<Long, String> groupNames = groupKeyMap();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Category category : categoryMapper.selectList(wrapper)) {
            list.add(toItem(category, groupNames));
        }
        return R.success(list);
    }

    /**
     * 新增分类（题型），可同时绑定分组标签。
     * <p>body: { name, icon, iconUrl, description, sort, groupKeys: ["hot","backend"] }</p>
     */
    @PostMapping
    public R<Void> add(@RequestBody Map<String, Object> body) {
        Category category = parseCategory(body);
        if (category == null) {
            return R.error("分类名称不能为空");
        }
        if (existsName(category.getName(), null)) {
            return R.error("分类「" + category.getName() + "」已存在");
        }
        category.setId(null);
        categoryMapper.insert(category);
        saveGroupRels(category.getId(), body.get("groupKeys"));
        return R.success();
    }

    /**
     * 编辑分类，重新绑定分组标签。
     */
    @PutMapping
    public R<Void> update(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("id");
        if (idObj == null) {
            return R.error("缺少分类 ID");
        }
        Long id = Long.valueOf(String.valueOf(idObj));
        Category db = categoryMapper.selectById(id);
        if (db == null) {
            return R.error("分类不存在或已被删除");
        }
        Category category = parseCategory(body);
        if (category == null) {
            return R.error("分类名称不能为空");
        }
        if (existsName(category.getName(), id)) {
            return R.error("分类「" + category.getName() + "」已存在");
        }
        db.setName(category.getName());
        if (body.containsKey("icon")) {
            db.setIcon(category.getIcon());
        }
        if (body.containsKey("iconUrl")) {
            db.setIconUrl(category.getIconUrl());
        }
        if (body.containsKey("description")) {
            db.setDescription(category.getDescription());
        }
        if (category.getSort() != null) {
            db.setSort(category.getSort());
        }
        categoryMapper.updateById(db);
        saveGroupRels(id, body.get("groupKeys"));
        return R.success();
    }

    /**
     * 删除分类（逻辑删除，并解除分组绑定）。
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Category db = categoryMapper.selectById(id);
        if (db == null) {
            return R.error("分类不存在或已被删除");
        }
        long questions = categoryMapper.countQuestionsByCategoryId(id);
        if (questions > 0) {
            return R.error("该分类下还有 " + questions + " 道题目，请先处理题目再删除");
        }
        categoryMapper.deleteById(id);
        relMapper.delete(new LambdaQueryWrapper<CategoryGroupRel>().eq(CategoryGroupRel::getCategoryId, id));
        return R.success();
    }

    private Category parseCategory(Map<String, Object> body) {
        String name = body.get("name") == null ? "" : String.valueOf(body.get("name")).trim();
        if (name.isEmpty()) {
            return null;
        }
        Category category = new Category();
        category.setName(name);
        category.setIcon(body.get("icon") == null ? null : String.valueOf(body.get("icon")));
        category.setIconUrl(body.get("iconUrl") == null ? null : String.valueOf(body.get("iconUrl")));
        category.setDescription(body.get("description") == null ? null : String.valueOf(body.get("description")));
        Object sort = body.get("sort");
        category.setSort(sort == null ? 0 : Integer.valueOf(String.valueOf(sort)));
        return category;
    }

    /** 重新绑定分类的分组标签（空数组表示清空绑定） */
    private void saveGroupRels(Long categoryId, Object groupKeysObj) {
        relMapper.delete(new LambdaQueryWrapper<CategoryGroupRel>().eq(CategoryGroupRel::getCategoryId, categoryId));
        if (!(groupKeysObj instanceof List<?> keys) || keys.isEmpty()) {
            return;
        }
        for (Object o : keys) {
            String key = String.valueOf(o).trim();
            if (key.isEmpty()) {
                continue;
            }
            CategoryGroup group = groupMapper.selectOne(new LambdaQueryWrapper<CategoryGroup>()
                    .eq(CategoryGroup::getGroupKey, key).last("LIMIT 1"));
            if (group == null) {
                continue;
            }
            CategoryGroupRel rel = new CategoryGroupRel();
            rel.setCategoryId(categoryId);
            rel.setGroupId(group.getId());
            try {
                relMapper.insert(rel);
            } catch (Exception ignored) {
                // 重复绑定忽略
            }
        }
    }

    /** groupId -> groupKey 映射 */
    private Map<Long, String> groupKeyMap() {
        Map<Long, String> map = new LinkedHashMap<>();
        for (CategoryGroup group : groupMapper.selectList(null)) {
            map.put(group.getId(), group.getGroupKey());
        }
        return map;
    }

    private Map<String, Object> toItem(Category category, Map<Long, String> groupKeys) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", category.getId());
        item.put("name", category.getName());
        item.put("icon", category.getIcon());
        item.put("iconUrl", category.getIconUrl());
        item.put("description", category.getDescription());
        item.put("sort", category.getSort());
        item.put("createTime", category.getCreateTime());
        item.put("questionCount", categoryMapper.countQuestionsByCategoryId(category.getId()));
        List<String> keys = new ArrayList<>();
        for (CategoryGroupRel rel : relMapper.selectList(new LambdaQueryWrapper<CategoryGroupRel>()
                .eq(CategoryGroupRel::getCategoryId, category.getId()))) {
            String key = groupKeys.get(rel.getGroupId());
            if (key != null) {
                keys.add(key);
            }
        }
        item.put("groupKeys", keys);
        return item;
    }

    private boolean existsName(String name, Long excludeId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, name);
        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }
        return categoryMapper.selectCount(wrapper) > 0;
    }
}
