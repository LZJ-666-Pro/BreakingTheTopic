package com.poti.question.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.common.R;
import com.poti.question.entity.Category;
import com.poti.question.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

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
