package com.poti.question.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.question.entity.Category;
import com.poti.question.mapper.CategoryMapper;
import com.poti.question.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
