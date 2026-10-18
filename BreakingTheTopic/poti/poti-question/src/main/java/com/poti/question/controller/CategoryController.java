package com.poti.question.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.common.R;
import com.poti.question.entity.Category;
import com.poti.question.entity.CategoryGroup;
import com.poti.question.mapper.CategoryGroupMapper;
import com.poti.question.mapper.CategoryGroupRelMapper;
import com.poti.question.mapper.QuestionMapper;
import com.poti.question.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/question/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryGroupMapper categoryGroupMapper;

    @Autowired
    private CategoryGroupRelMapper categoryGroupRelMapper;

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 题库页动态数据：启用分组标签 + 分类列表（含题数与所属分组标识）。
     * group_key=hidden 的分类由前端过滤，不在宫格展示。
     */
    @GetMapping("/groups")
    public R<Map<String, Object>> groups() {
        try {
            // 启用中的分组标签，按 sort 升序
            List<Map<String, Object>> groups = new ArrayList<>();
            LambdaQueryWrapper<CategoryGroup> groupWrapper = new LambdaQueryWrapper<>();
            groupWrapper.eq(CategoryGroup::getStatus, 1)
                        .orderByAsc(CategoryGroup::getSort)
                        .orderByAsc(CategoryGroup::getId);
            for (CategoryGroup group : categoryGroupMapper.selectList(groupWrapper)) {
                Map<String, Object> g = new LinkedHashMap<>();
                g.put("key", group.getGroupKey());
                g.put("name", group.getName());
                groups.add(g);
            }

            // 未删除分类
            LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
            categoryWrapper.eq(Category::getDeleted, 0)
                           .orderByAsc(Category::getSort);
            List<Category> categories = categoryService.list(categoryWrapper);

            // categoryId -> [groupKey]
            Map<Long, List<String>> keyMap = new LinkedHashMap<>();
            for (Map<String, Object> row : categoryGroupRelMapper.selectAllCategoryGroupKeys()) {
                long categoryId = ((Number) row.get("categoryId")).longValue();
                keyMap.computeIfAbsent(categoryId, k -> new ArrayList<>())
                      .add(String.valueOf(row.get("groupKey")));
            }

            // categoryId -> 题目数
            Map<Long, Long> countMap = new LinkedHashMap<>();
            for (Map<String, Object> row : questionMapper.countByCategory()) {
                Object categoryId = row.get("categoryId");
                if (categoryId != null) {
                    countMap.put(((Number) categoryId).longValue(), ((Number) row.get("cnt")).longValue());
                }
            }

            List<Map<String, Object>> categoryList = new ArrayList<>();
            for (Category category : categories) {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("id", category.getId());
                c.put("name", category.getName());
                c.put("icon", category.getIcon());
                c.put("iconUrl", category.getIconUrl());
                c.put("description", category.getDescription());
                c.put("groupKeys", keyMap.getOrDefault(category.getId(), new ArrayList<>()));
                c.put("questionCount", countMap.getOrDefault(category.getId(), 0L));
                categoryList.add(c);
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("groups", groups);
            data.put("categories", categoryList);
            return R.success(data);
        } catch (Exception e) {
            return R.error("获取题库分组数据失败");
        }
    }

    @GetMapping("/list")
    public R<List<Category>> getList() {
        try {
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getDeleted, 0)
                   .orderByAsc(Category::getSort);
            
            List<Category> categories = categoryService.list(wrapper);
            return R.success(categories);
        } catch (Exception e) {
            return R.error("获取分类列表失败");
        }
    }

    /**
     * internal：全量分类 id+名称（服务间调用，错题统计等跨库解析分类名使用）
     */
    @GetMapping("/internal/names")
    public R<List<Map<String, Object>>> internalCategoryNames() {
        try {
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getDeleted, 0)
                   .orderByAsc(Category::getSort);
            List<Map<String, Object>> list = new ArrayList<>();
            for (Category c : categoryService.list(wrapper)) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", c.getId());
                m.put("name", c.getName());
                list.add(m);
            }
            return R.success(list);
        } catch (Exception e) {
            return R.error("获取分类名称失败");
        }
    }

    @PostMapping("/save")
    public R<Category> saveCategory(@RequestBody Category category) {
        try {
            if (category.getParentId() == null) {
                category.setParentId(0L);
            }
            if (category.getSort() == null) {
                category.setSort(0);
            }
            categoryService.save(category);
            return R.success(category);
        } catch (Exception e) {
            return R.error("创建分类失败");
        }
    }

    @PutMapping("/update")
    public R<Category> updateCategory(@RequestBody Category category) {
        try {
            Category existingCategory = categoryService.getById(category.getId());
            if (existingCategory == null) {
                return R.error("分类不存在");
            }
            
            if (category.getName() != null) {
                existingCategory.setName(category.getName());
            }
            if (category.getIcon() != null) {
                existingCategory.setIcon(category.getIcon());
            }
            if (category.getSort() != null) {
                existingCategory.setSort(category.getSort());
            }
            
            categoryService.updateById(existingCategory);
            return R.success(existingCategory);
        } catch (Exception e) {
            return R.error("更新分类失败");
        }
    }

    @DeleteMapping("/{id}")
    public R<String> deleteCategory(@PathVariable Long id) {
        try {
            Category category = categoryService.getById(id);
            if (category == null) {
                return R.error("分类不存在");
            }
            category.setDeleted(1);
            categoryService.updateById(category);
            return R.success("删除成功");
        } catch (Exception e) {
            return R.error("删除分类失败");
        }
    }
}
