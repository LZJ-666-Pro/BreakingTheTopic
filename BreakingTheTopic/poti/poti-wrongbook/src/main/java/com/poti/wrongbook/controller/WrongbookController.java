package com.poti.wrongbook.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.common.R;
import com.poti.wrongbook.entity.Wrongbook;
import com.poti.wrongbook.service.WrongbookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/wrongbook")
public class WrongbookController {

    @Autowired
    private WrongbookService wrongbookService;

    @PostMapping("/add")
    public R<Void> addToWrongbook(@RequestHeader("X-User-Id") Long userId,
                                   @RequestBody java.util.Map<String, Object> request) {
        try {
            Long questionId = Long.parseLong(request.get("questionId").toString());

            LambdaQueryWrapper<Wrongbook> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Wrongbook::getUserId, userId)
                   .eq(Wrongbook::getQuestionId, questionId);

            Wrongbook existingWrongbook = wrongbookService.getOne(wrapper);
            if (existingWrongbook != null) {
                existingWrongbook.setWrongCount(existingWrongbook.getWrongCount() + 1);
                existingWrongbook.setLastWrongTime(java.time.LocalDateTime.now());
                wrongbookService.updateById(existingWrongbook);
                return R.success();
            }

            Wrongbook newWrongbook = new Wrongbook();
            newWrongbook.setUserId(userId);
            newWrongbook.setQuestionId(questionId);
            
            if (request.get("categoryId") != null) {
                newWrongbook.setCategoryId(Long.parseLong(request.get("categoryId").toString()));
            }
            if (request.get("title") != null) {
                newWrongbook.setTitle(request.get("title").toString());
            }
            if (request.get("type") != null) {
                newWrongbook.setType(Integer.parseInt(request.get("type").toString()));
            }
            if (request.get("difficulty") != null) {
                newWrongbook.setDifficulty(Integer.parseInt(request.get("difficulty").toString()));
            }
            
            newWrongbook.setWrongCount(1);
            newWrongbook.setLastWrongTime(java.time.LocalDateTime.now());
            newWrongbook.setMastered(0);
            wrongbookService.save(newWrongbook);

            return R.success();
        } catch (Exception e) {
            log.error("添加错题失败", e);
            return R.error("添加错题失败");
        }
    }

    @DeleteMapping("/{questionId}")
    public R<Void> removeFromWrongbook(@RequestHeader("X-User-Id") Long userId,
                                        @PathVariable Long questionId) {
        try {
            LambdaQueryWrapper<Wrongbook> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Wrongbook::getUserId, userId)
                   .eq(Wrongbook::getQuestionId, questionId);

            wrongbookService.remove(wrapper);
            return R.success();
        } catch (Exception e) {
            log.error("移除错题失败", e);
            return R.error("移除错题失败");
        }
    }

    @DeleteMapping("/clear")
    public R<Void> clearAllWrongbook(@RequestHeader("X-User-Id") Long userId) {
        try {
            LambdaQueryWrapper<Wrongbook> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Wrongbook::getUserId, userId);

            wrongbookService.remove(wrapper);
            return R.success();
        } catch (Exception e) {
            log.error("清空错题本失败", e);
            return R.error("清空错题本失败");
        }
    }

    @GetMapping("/list")
    public R<List<Wrongbook>> getWrongbookList(@RequestHeader("X-User-Id") Long userId) {
        try {
            LambdaQueryWrapper<Wrongbook> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Wrongbook::getUserId, userId)
                   .orderByDesc(Wrongbook::getCreateTime);

            List<Wrongbook> wrongbookList = wrongbookService.list(wrapper);
            return R.success(wrongbookList);
        } catch (Exception e) {
            log.error("获取错题列表失败", e);
            return R.error("获取错题列表失败");
        }
    }

    @GetMapping("/check/{questionId}")
    public R<Boolean> checkInWrongbook(@RequestHeader("X-User-Id") Long userId,
                                        @PathVariable Long questionId) {
        try {
            LambdaQueryWrapper<Wrongbook> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Wrongbook::getUserId, userId)
                   .eq(Wrongbook::getQuestionId, questionId);

            boolean isInWrongbook = wrongbookService.count(wrapper) > 0;
            return R.success(isInWrongbook);
        } catch (Exception e) {
            log.error("检查错题状态失败", e);
            return R.error("检查错题状态失败");
        }
    }

    @GetMapping("/internal/count")
    public R<Integer> getWrongbookCount(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        try {
            Long userId = headerUserId != null ? headerUserId : paramUserId;
            if (userId == null) {
                return R.error("用户ID不能为空");
            }
            LambdaQueryWrapper<Wrongbook> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Wrongbook::getUserId, userId);

            int count = (int) wrongbookService.count(wrapper);
            return R.success(count);
        } catch (Exception e) {
            log.error("获取错题数量失败", e);
            return R.error("获取错题数量失败");
        }
    }

    @PutMapping("/mastered/{questionId}")
    public R<Void> markAsMastered(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long questionId) {
        try {
            LambdaQueryWrapper<Wrongbook> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Wrongbook::getUserId, userId)
                   .eq(Wrongbook::getQuestionId, questionId);

            Wrongbook wrongbook = wrongbookService.getOne(wrapper);
            if (wrongbook != null) {
                wrongbook.setMastered(1);
                wrongbook.setLastReviewTime(java.time.LocalDateTime.now());
                wrongbook.setReviewTimes(wrongbook.getReviewTimes() != null ? wrongbook.getReviewTimes() + 1 : 1);
                wrongbookService.updateById(wrongbook);
            }
            return R.success();
        } catch (Exception e) {
            log.error("标记已掌握失败", e);
            return R.error("标记已掌握失败");
        }
    }

    @GetMapping("/statistics")
    public R<java.util.Map<String, Object>> getStatistics(@RequestHeader("X-User-Id") Long userId) {
        try {
            java.util.Map<String, Object> statistics = wrongbookService.getStatistics(userId);
            return R.success(statistics);
        } catch (Exception e) {
            log.error("获取错题统计失败", e);
            return R.error("获取错题统计失败");
        }
    }
}
