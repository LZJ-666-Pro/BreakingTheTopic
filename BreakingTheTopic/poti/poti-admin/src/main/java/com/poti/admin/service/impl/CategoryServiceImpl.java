package com.poti.admin.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.admin.entity.Category;
import com.poti.admin.mapper.CategoryMapper;
import com.poti.admin.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@DS("question")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public Page<Category> pageList(int page, int size, String name) {
        Page<Category> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Category::getName, name);
        }
        wrapper.orderByAsc(Category::getSort);
        return this.page(pageParam, wrapper);
    }

    @Override
    public List<Category> listAll() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        return this.list(wrapper);
    }

    @Override
    public boolean addCategory(Category category) {
        return this.save(category);
    }

    @Override
    public boolean updateCategory(Category category) {
        return this.updateById(category);
    }

    @Override
    public boolean deleteCategory(Long id) {
        return this.removeById(id);
    }
}
