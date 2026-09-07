package com.poti.favorite.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.common.R;
import com.poti.favorite.entity.Favorite;
import com.poti.favorite.mapper.FavoriteMapper;
import com.poti.favorite.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @PostMapping("/add")
    public R<Void> addFavorite(@RequestHeader("X-User-Id") Long userId,
                                 @RequestBody Map<String, Object> request) {
        try {
            Long questionId = Long.parseLong(request.get("questionId").toString());
            Long categoryId = request.get("categoryId") != null ? Long.parseLong(request.get("categoryId").toString()) : null;
            String title = request.get("title") != null ? request.get("title").toString() : null;
            Integer type = request.get("type") != null ? Integer.parseInt(request.get("type").toString()) : null;
            Integer difficulty = request.get("difficulty") != null ? Integer.parseInt(request.get("difficulty").toString()) : null;
            
            Favorite existingFavorite = favoriteMapper.selectByUserAndQuestion(userId, questionId);
            if (existingFavorite != null) {
                if (existingFavorite.getDeleted() == 1) {
                    favoriteMapper.restoreFavorite(userId, questionId, categoryId, title, type, difficulty);
                }
                return R.success();
            }
            
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setQuestionId(questionId);
            favorite.setCategoryId(categoryId);
            favorite.setTitle(title);
            favorite.setType(type);
            favorite.setDifficulty(difficulty);
            
            favoriteService.save(favorite);
            
            return R.success();
        } catch (Exception e) {
            log.error("添加收藏失败", e);
            return R.error("添加收藏失败");
        }
    }

    @DeleteMapping("/remove/{questionId}")
    public R<Void> removeFavorite(@RequestHeader("X-User-Id") Long userId,
                                  @PathVariable Long questionId) {
        try {
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, userId)
                   .eq(Favorite::getQuestionId, questionId);
            
            favoriteService.remove(wrapper);
            return R.success();
        } catch (Exception e) {
            log.error("取消收藏失败", e);
            return R.error("取消收藏失败");
        }
    }

    @GetMapping("/list")
    public R<List<Favorite>> getFavorites(@RequestHeader("X-User-Id") Long userId,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, userId)
                   .orderByDesc(Favorite::getCreateTime);
            
            List<Favorite> favorites = favoriteService.list(wrapper);
            return R.success(favorites);
        } catch (Exception e) {
            log.error("获取收藏列表失败", e);
            return R.error("获取收藏列表失败");
        }
    }

    @GetMapping("/check/{questionId}")
    public R<Boolean> checkFavorite(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long questionId) {
        try {
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, userId)
                   .eq(Favorite::getQuestionId, questionId);
            
            boolean isFavorited = favoriteService.count(wrapper) > 0;
            return R.success(isFavorited);
        } catch (Exception e) {
            log.error("检查收藏状态失败", e);
            return R.error("检查收藏状态失败");
        }
    }

    @GetMapping("/internal/count")
    public R<Integer> getFavoriteCount(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        try {
            Long userId = headerUserId != null ? headerUserId : paramUserId;
            if (userId == null) {
                return R.error("用户ID不能为空");
            }
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, userId);
            
            int count = (int) favoriteService.count(wrapper);
            return R.success(count);
        } catch (Exception e) {
            log.error("获取收藏数量失败", e);
            return R.error("获取收藏数量失败");
        }
    }

    @DeleteMapping("/clear")
    public R<Void> clearAllFavorites(@RequestHeader("X-User-Id") Long userId) {
        try {
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, userId);
            
            favoriteService.remove(wrapper);
            return R.success();
        } catch (Exception e) {
            log.error("清空收藏失败", e);
            return R.error("清空收藏失败");
        }
    }
}
