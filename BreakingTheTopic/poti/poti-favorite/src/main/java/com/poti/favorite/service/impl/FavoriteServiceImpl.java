package com.poti.favorite.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.favorite.entity.Favorite;
import com.poti.favorite.mapper.FavoriteMapper;
import com.poti.favorite.service.FavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {
}
