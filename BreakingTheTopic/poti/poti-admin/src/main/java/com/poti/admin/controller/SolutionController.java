package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.Solution;
import com.poti.admin.mapper.SolutionMapper;
import com.poti.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 题解审核接口（后台）。
 */
@RestController
@RequestMapping("/admin/solution")
@RequiredArgsConstructor
public class SolutionController {

    private final SolutionMapper solutionMapper;

    /** 分页列表（status：0待审核 1已通过 2已拒绝，不传查全部；reported=1 只看被举报） */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long questionId,
            @RequestParam(required = false) Integer reported) {

        LambdaQueryWrapper<Solution> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Solution::getStatus, status);
        }
        if (questionId != null) {
            wrapper.eq(Solution::getQuestionId, questionId);
        }
        if (reported != null && reported == 1) {
            wrapper.gt(Solution::getReportCount, 0);
            wrapper.orderByDesc(Solution::getReportCount);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Solution::getTitle, keyword.trim())
                    .or().like(Solution::getAuthorName, keyword.trim()));
        }
        wrapper.orderByDesc(Solution::getPinned).orderByDesc(Solution::getId);

        Page<Solution> page = solutionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        return R.success(data);
    }

    /** 详情（含题目简介） */
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Solution s = solutionMapper.selectById(id);
        if (s == null) {
            return R.error("题解不存在或已被删除");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", s.getId());
        data.put("questionId", s.getQuestionId());
        data.put("userId", s.getUserId());
        data.put("authorName", s.getAuthorName());
        data.put("title", s.getTitle());
        data.put("content", s.getContent());
        data.put("isOfficial", s.getIsOfficial());
        data.put("status", s.getStatus());
        data.put("rejectReason", s.getRejectReason());
        data.put("quality", s.getQuality());
        data.put("pinned", s.getPinned());
        data.put("viewCount", s.getViewCount());
        data.put("likeCount", s.getLikeCount());
        data.put("starCount", s.getStarCount());
        data.put("reportCount", s.getReportCount());
        data.put("createTime", s.getCreateTime());
        data.put("question", solutionMapper.selectQuestionBrief(s.getQuestionId()));
        return R.success(data);
    }

    /** 审核通过 / 拒绝 */
    @PutMapping("/review/{id}")
    public R<Void> review(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Solution db = solutionMapper.selectById(id);
        if (db == null) {
            return R.error("题解不存在或已被删除");
        }
        Integer status = body.get("status") == null ? null : Integer.valueOf(String.valueOf(body.get("status")));
        if (status == null || (status != 1 && status != 2)) {
            return R.error("审核状态非法（1通过 2拒绝）");
        }
        db.setStatus(status);
        String reason = body.get("rejectReason") == null ? "" : String.valueOf(body.get("rejectReason"));
        db.setRejectReason(status == 2 ? reason : "");
        solutionMapper.updateById(db);
        return R.success();
    }

    /** 编辑题解（官方题解维护） */
    @PutMapping
    public R<Void> update(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") == null ? null : Long.valueOf(String.valueOf(body.get("id")));
        if (id == null) {
            return R.error("缺少题解 ID");
        }
        Solution db = solutionMapper.selectById(id);
        if (db == null) {
            return R.error("题解不存在或已被删除");
        }
        if (body.containsKey("title") && body.get("title") != null && !String.valueOf(body.get("title")).isBlank()) {
            db.setTitle(String.valueOf(body.get("title")).trim());
        }
        if (body.containsKey("content") && body.get("content") != null && !String.valueOf(body.get("content")).isBlank()) {
            db.setContent(String.valueOf(body.get("content")));
        }
        if (body.containsKey("quality")) {
            db.setQuality(Integer.valueOf(String.valueOf(body.get("quality"))));
        }
        if (body.containsKey("pinned")) {
            db.setPinned(Integer.valueOf(String.valueOf(body.get("pinned"))));
        }
        solutionMapper.updateById(db);
        return R.success();
    }

    /** 删除题解（处理举报时可删除低质题解） */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Solution db = solutionMapper.selectById(id);
        if (db == null) {
            return R.error("题解不存在或已被删除");
        }
        solutionMapper.deleteById(id);
        return R.success();
    }

    /** 清零举报（处理完成，保留题解） */
    @PutMapping("/report/clear/{id}")
    public R<Void> clearReport(@PathVariable Long id) {
        Solution db = solutionMapper.selectById(id);
        if (db == null) {
            return R.error("题解不存在或已被删除");
        }
        db.setReportCount(0);
        solutionMapper.updateById(db);
        return R.success();
    }
}
