package com.poti.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.admin.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    Page<Category> pageList(int page, int size, String name);

    List<Category> listAll();

    boolean addCategory(Category category);

    boolean updateCategory(Category category);

    boolean deleteCategory(Long id);
}
