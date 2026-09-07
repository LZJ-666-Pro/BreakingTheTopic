package com.poti.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.Category;
import com.poti.admin.service.CategoryService;
import com.poti.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public R<Page<Category>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        Page<Category> result = categoryService.pageList(page, size, name);
        return R.success(result);
    }

    @GetMapping("/list")
    public R<List<Category>> listAll() {
        List<Category> list = categoryService.listAll();
        return R.success(list);
    }

    @GetMapping("/{id}")
    public R<Category> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return R.error("分类不存在");
        }
        return R.success(category);
    }

    @PostMapping
    public R<Void> add(@RequestBody Category category) {
        boolean success = categoryService.addCategory(category);
        return success ? R.success(null) : R.error("添加失败");
    }

    @PutMapping
    public R<Void> update(@RequestBody Category category) {
        boolean success = categoryService.updateCategory(category);
        return success ? R.success(null) : R.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        boolean success = categoryService.deleteCategory(id);
        return success ? R.success(null) : R.error("删除失败");
    }
}
