package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.DailyQuestion;
import com.poti.admin.mapper.DailyMapper;
import com.poti.admin.mapper.PracticeStatMapper;
import com.poti.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 每日一题管理接口（后台）。
 */
@RestController
@RequestMapping("/admin/daily")
@RequiredArgsConstructor
public class DailyController {

    private final DailyMapper dailyMapper;
    private final PracticeStatMapper practiceStatMapper;

    /** 分页列表：日期倒序，附带题目内容、当日完成人数与正确率 */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {

        LambdaQueryWrapper<DailyQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DailyQuestion::getDate);
        Page<DailyQuestion> page = dailyMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (DailyQuestion d : page.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", d.getId());
            item.put("questionId", d.getQuestionId());
            item.put("date", String.valueOf(d.getDate()));
            item.put("reason", d.getReason());
            item.put("hint", d.getHint());

            Map<String, Object> brief = dailyMapper.selectQuestionBrief(d.getQuestionId());
            item.put("content", brief == null ? "（题目已删除）" : brief.get("content"));
            item.put("difficulty", brief == null ? null : brief.get("difficulty"));

            try {
                Map<String, Object> stat = practiceStatMapper.selectDailyStat(d.getQuestionId(), String.valueOf(d.getDate()));
                long total = stat == null || stat.get("total") == null ? 0 : Long.parseLong(String.valueOf(stat.get("total")));
                long correct = stat == null || stat.get("correct") == null ? 0 : Long.parseLong(String.valueOf(stat.get("correct")));
                item.put("doneCount", total);
                item.put("accuracy", total == 0 ? null : Math.round(correct * 1000.0 / total) / 10.0);
            } catch (Exception e) {
                item.put("doneCount", 0);
                item.put("accuracy", null);
            }
            records.add(item);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", records);
        data.put("total", page.getTotal());
        return R.success(data);
    }

    /** 新增（同一日期仅一题） */
    @PostMapping
    public R<Void> create(@RequestBody Map<String, Object> body) {
        Long questionId = parseLong(body.get("questionId"));
        LocalDate date = parseDate(body.get("date"));
        if (questionId == null) {
            return R.error("请选择题目");
        }
        if (date == null) {
            return R.error("请选择日期");
        }
        if (existsOn(date, null)) {
            return R.error(date + " 已配置过每日一题，请直接编辑");
        }
        DailyQuestion d = new DailyQuestion();
        d.setQuestionId(questionId);
        d.setDate(date);
        d.setReason(str(body.get("reason")));
        d.setHint(str(body.get("hint")));
        dailyMapper.insert(d);
        return R.success();
    }

    /** 编辑 */
    @PutMapping
    public R<Void> update(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body.get("id"));
        if (id == null) {
            return R.error("缺少 ID");
        }
        DailyQuestion db = dailyMapper.selectById(id);
        if (db == null) {
            return R.error("记录不存在或已被删除");
        }
        Long questionId = parseLong(body.get("questionId"));
        LocalDate date = parseDate(body.get("date"));
        if (questionId == null || date == null) {
            return R.error("题目与日期不能为空");
        }
        if (existsOn(date, id)) {
            return R.error(date + " 已配置过每日一题");
        }
        db.setQuestionId(questionId);
        db.setDate(date);
        db.setReason(str(body.get("reason")));
        db.setHint(str(body.get("hint")));
        dailyMapper.updateById(db);
        return R.success();
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        DailyQuestion db = dailyMapper.selectById(id);
        if (db == null) {
            return R.error("记录不存在或已被删除");
        }
        dailyMapper.deleteById(id);
        return R.success();
    }

    private boolean existsOn(LocalDate date, Long excludeId) {
        LambdaQueryWrapper<DailyQuestion> w = new LambdaQueryWrapper<>();
        w.eq(DailyQuestion::getDate, date);
        if (excludeId != null) {
            w.ne(DailyQuestion::getId, excludeId);
        }
        return dailyMapper.selectCount(w) > 0;
    }

    private Long parseLong(Object o) {
        try {
            return o == null ? null : Long.valueOf(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDate(Object o) {
        try {
            return o == null ? null : LocalDate.parse(String.valueOf(o));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
