package com.poti.interview.controller;

import com.poti.common.R;
import com.poti.interview.service.CampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/camp")
public class CampController {

    @Autowired
    private CampService campService;

    /** 特训营列表 */
    @GetMapping("/list")
    public R<List<Map<String, Object>>> getCampList(@RequestHeader("X-User-Id") Long userId) {
        try {
            return R.success(campService.getCampList(userId));
        } catch (Exception e) {
            log.error("获取特训营列表失败", e);
            return R.error("获取特训营列表失败");
        }
    }

    /** 特训营详情 */
    @GetMapping("/detail/{campId}")
    public R<Map<String, Object>> getCampDetail(@RequestHeader("X-User-Id") Long userId,
                                                @PathVariable String campId) {
        try {
            Map<String, Object> detail = campService.getCampDetail(userId, campId);
            if (detail == null) {
                return R.error("训练营不存在");
            }
            return R.success(detail);
        } catch (Exception e) {
            log.error("获取特训营详情失败", e);
            return R.error("获取特训营详情失败");
        }
    }

    /** 加入训练营 */
    @PostMapping("/join")
    public R<Map<String, Object>> joinCamp(@RequestHeader("X-User-Id") Long userId,
                                           @RequestBody Map<String, Object> request) {
        try {
            String campId = (String) request.get("campId");
            if (campId == null || campId.trim().isEmpty()) {
                return R.error("参数错误");
            }
            return R.success(campService.joinCamp(userId, campId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("加入训练营失败", e);
            return R.error("加入训练营失败");
        }
    }

    /** 我的训练营列表 */
    @GetMapping("/my")
    public R<List<Map<String, Object>>> getMyCamps(@RequestHeader("X-User-Id") Long userId) {
        try {
            return R.success(campService.getMyCamps(userId));
        } catch (Exception e) {
            log.error("获取我的训练营失败", e);
            return R.error("获取我的训练营失败");
        }
    }

    /** 训练营首页数据（今日任务 + 进度 + 打卡记录） */
    @GetMapping("/home/{campId}")
    public R<Map<String, Object>> getCampHome(@RequestHeader("X-User-Id") Long userId,
                                              @PathVariable String campId) {
        try {
            Map<String, Object> home = campService.getCampHome(userId, campId);
            if (home == null) {
                return R.error("训练营不存在");
            }
            return R.success(home);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取训练营首页失败", e);
            return R.error("获取训练营首页失败");
        }
    }

    /** 每日打卡 */
    @PostMapping("/checkin")
    public R<Map<String, Object>> checkin(@RequestHeader("X-User-Id") Long userId,
                                          @RequestBody Map<String, Object> request) {
        try {
            String campId = (String) request.get("campId");
            if (campId == null || campId.trim().isEmpty()) {
                return R.error("参数错误");
            }
            return R.success(campService.checkin(userId, campId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("特训营打卡失败", e);
            return R.error("打卡失败，请稍后重试");
        }
    }
}
