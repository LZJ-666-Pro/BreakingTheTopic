package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.SpecialTopic;
import com.poti.admin.mapper.SpecialMapper;
import com.poti.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 专题管理接口（后台）。
 */
@RestController
@RequestMapping("/admin/special")
@RequiredArgsConstructor
public class SpecialController {

    private final SpecialMapper specialMapper;

    /** 分页列表，附带题目数 */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<SpecialTopic> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SpecialTopic::getName, keyword.trim());
        }
        if (status != null) {
            wrapper.eq(SpecialTopic::getStatus, status);
        }
        wrapper.orderByDesc(SpecialTopic::getSort).orderByDesc(SpecialTopic::getId);

        Page<SpecialTopic> page = specialMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (SpecialTopic t : page.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", t.getId());
            item.put("name", t.getName());
            item.put("cover", t.getCover());
            item.put("description", t.getDescription());
            item.put("difficulty", t.getDifficulty());
            item.put("estimatedDays", t.getEstimatedDays());
            item.put("unlockType", t.getUnlockType());
            item.put("sort", t.getSort());
            item.put("status", t.getStatus());
            item.put("createTime", t.getCreateTime());
            item.put("questionCount", specialMapper.countQuestions(t.getId()));
            records.add(item);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", records);
        data.put("total", page.getTotal());
        return R.success(data);
    }

    /** 详情（含题目列表） */
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        SpecialTopic topic = specialMapper.selectById(id);
        if (topic == null) {
            return R.error("专题不存在或已被删除");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", topic.getId());
        data.put("name", topic.getName());
        data.put("cover", topic.getCover());
        data.put("description", topic.getDescription());
        data.put("difficulty", topic.getDifficulty());
        data.put("estimatedDays", topic.getEstimatedDays());
        data.put("unlockType", topic.getUnlockType());
        data.put("sort", topic.getSort());
        data.put("status", topic.getStatus());
        data.put("questions", specialMapper.selectTopicQuestions(id));
        return R.success(data);
    }

    /** 新增专题（questionIds 顺序即题目顺序） */
    @PostMapping
    public R<Void> create(@RequestBody Map<String, Object> body) {
        SpecialTopic topic = parseTopic(body);
        List<Long> questionIds = parseIds(body.get("questionIds"));
        if (topic.getName() == null || topic.getName().isBlank()) {
            return R.error("专题名称不能为空");
        }
        if (questionIds.isEmpty()) {
            return R.error("请至少选择一道题目");
        }
        topic.setId(null);
        specialMapper.insert(topic);
        saveQuestions(topic.getId(), questionIds);
        return R.success();
    }

    /** 编辑专题（题目列表全量替换） */
    @PutMapping
    public R<Void> update(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") == null ? null : Long.valueOf(String.valueOf(body.get("id")));
        if (id == null) {
            return R.error("缺少专题 ID");
        }
        SpecialTopic db = specialMapper.selectById(id);
        if (db == null) {
            return R.error("专题不存在或已被删除");
        }
        SpecialTopic topic = parseTopic(body);
        List<Long> questionIds = parseIds(body.get("questionIds"));
        if (topic.getName() != null && !topic.getName().isBlank()) {
            db.setName(topic.getName());
        }
        // 仅当请求中显式携带 cover/description 时才覆盖，避免局部更新（如状态开关）清空内容
        if (body.containsKey("cover") && body.get("cover") != null) {
            db.setCover(String.valueOf(body.get("cover")));
        }
        if (body.containsKey("description") && body.get("description") != null) {
            db.setDescription(String.valueOf(body.get("description")));
        }
        if (topic.getDifficulty() != null) {
            db.setDifficulty(topic.getDifficulty());
        }
        if (topic.getEstimatedDays() != null) {
            db.setEstimatedDays(topic.getEstimatedDays());
        }
        if (topic.getUnlockType() != null) {
            db.setUnlockType(topic.getUnlockType());
        }
        if (topic.getSort() != null) {
            db.setSort(topic.getSort());
        }
        if (topic.getStatus() != null) {
            db.setStatus(topic.getStatus());
        }
        specialMapper.updateById(db);
        if (!questionIds.isEmpty()) {
            specialMapper.deleteTopicQuestions(id);
            saveQuestions(id, questionIds);
        }
        return R.success();
    }

    /** 删除专题（逻辑删 + 清理关联） */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        SpecialTopic db = specialMapper.selectById(id);
        if (db == null) {
            return R.error("专题不存在或已被删除");
        }
        specialMapper.deleteById(id);
        specialMapper.deleteTopicQuestions(id);
        return R.success();
    }

    private SpecialTopic parseTopic(Map<String, Object> body) {
        SpecialTopic t = new SpecialTopic();
        t.setName(str(body.get("name")));
        t.setCover(str(body.get("cover")));
        t.setDescription(str(body.get("description")));
        t.setDifficulty(intVal(body.get("difficulty"), 2));
        t.setEstimatedDays(intVal(body.get("estimatedDays"), 7));
        t.setUnlockType(intVal(body.get("unlockType"), 0));
        t.setSort(intVal(body.get("sort"), 0));
        t.setStatus(intVal(body.get("status"), 1));
        return t;
    }

    private void saveQuestions(Long topicId, List<Long> questionIds) {
        int sort = 0;
        for (Long qid : questionIds) {
            specialMapper.insertTopicQuestion(topicId, qid, sort++);
        }
    }

    private List<Long> parseIds(Object raw) {
        List<Long> ids = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object o : list) {
                ids.add(Long.valueOf(String.valueOf(o)));
            }
        }
        return ids;
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private Integer intVal(Object o, int def) {
        try {
            return o == null ? def : Integer.valueOf(String.valueOf(o));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
