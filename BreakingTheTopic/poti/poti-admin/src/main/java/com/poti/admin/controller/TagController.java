package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.Tag;
import com.poti.admin.mapper.TagMapper;
import com.poti.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签管理接口（后台）。
 */
@RestController
@RequestMapping("/admin/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagMapper tagMapper;

    /**
     * 分页列表：支持关键字、分组、状态筛选，按 sort 倒序。
     */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Tag::getName, keyword.trim());
        }
        if (groupName != null && !groupName.isBlank()) {
            wrapper.eq(Tag::getGroupName, groupName);
        }
        if (status != null) {
            wrapper.eq(Tag::getStatus, status);
        }
        wrapper.orderByDesc(Tag::getSort).orderByAsc(Tag::getId);

        Page<Tag> page = tagMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Tag tag : page.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", tag.getId());
            item.put("name", tag.getName());
            item.put("groupName", tag.getGroupName());
            item.put("color", tag.getColor());
            item.put("description", tag.getDescription());
            item.put("sort", tag.getSort());
            item.put("hot", tag.getHot());
            item.put("status", tag.getStatus());
            item.put("createTime", tag.getCreateTime());
            item.put("questionCount", tagMapper.countQuestionsByTag(tag.getName()));
            records.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", records);
        data.put("total", page.getTotal());
        return R.success(data);
    }

    /**
     * 全部启用标签（题目表单多选下拉用），按 sort 倒序。
     */
    @GetMapping("/all")
    public R<List<Map<String, Object>>> all() {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getStatus, 1).orderByDesc(Tag::getSort).orderByAsc(Tag::getId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Tag tag : tagMapper.selectList(wrapper)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", tag.getId());
            item.put("name", tag.getName());
            item.put("groupName", tag.getGroupName());
            item.put("color", tag.getColor());
            item.put("hot", tag.getHot());
            list.add(item);
        }
        return R.success(list);
    }

    /**
     * 新增标签（名称查重）。
     */
    @PostMapping
    public R<Void> create(@RequestBody Tag tag) {
        String name = tag.getName() == null ? "" : tag.getName().trim();
        if (name.isEmpty()) {
            return R.error("标签名称不能为空");
        }
        tag.setName(name);
        if (exists(name, null)) {
            return R.error("标签「" + name + "」已存在");
        }
        if (tag.getSort() == null) {
            tag.setSort(0);
        }
        if (tag.getHot() == null) {
            tag.setHot(0);
        }
        if (tag.getStatus() == null) {
            tag.setStatus(1);
        }
        tag.setId(null);
        tagMapper.insert(tag);
        return R.success();
    }

    /**
     * 编辑标签。
     */
    @PutMapping
    public R<Void> update(@RequestBody Tag tag) {
        if (tag.getId() == null) {
            return R.error("缺少标签 ID");
        }
        String name = tag.getName() == null ? "" : tag.getName().trim();
        if (name.isEmpty()) {
            return R.error("标签名称不能为空");
        }
        if (exists(name, tag.getId())) {
            return R.error("标签「" + name + "」已存在");
        }
        Tag db = tagMapper.selectById(tag.getId());
        if (db == null) {
            return R.error("标签不存在或已被删除");
        }
        db.setName(name);
        db.setGroupName(tag.getGroupName() == null ? "" : tag.getGroupName());
        db.setColor(tag.getColor() == null ? "" : tag.getColor());
        db.setDescription(tag.getDescription() == null ? "" : tag.getDescription());
        if (tag.getSort() != null) {
            db.setSort(tag.getSort());
        }
        if (tag.getHot() != null) {
            db.setHot(tag.getHot());
        }
        if (tag.getStatus() != null) {
            db.setStatus(tag.getStatus());
        }
        tagMapper.updateById(db);
        return R.success();
    }

    /**
     * 删除标签（逻辑删除，题目 tags 字段保持不变）。
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Tag db = tagMapper.selectById(id);
        if (db == null) {
            return R.error("标签不存在或已被删除");
        }
        tagMapper.deleteById(id);
        return R.success();
    }

    /**
     * 批量给题目打标签 / 移除标签。
     * <p>
     * body: { tagNames: ["动态规划","字节跳动"], questionIds: [1,2,3], action: "add" | "remove" }
     * 直接读写 question.tags 逗号分隔字段，已有标签去重后追加，移除按精确词剔除。
     * </p>
     */
    @PostMapping("/batch-tag")
    public R<String> batchTag(@RequestBody Map<String, Object> body) {
        List<String> tagNames = new ArrayList<>();
        Object names = body.get("tagNames");
        if (names instanceof List<?> l) {
            for (Object o : l) {
                String n = String.valueOf(o).trim();
                if (!n.isEmpty() && !tagNames.contains(n)) {
                    tagNames.add(n);
                }
            }
        }
        List<Long> questionIds = new ArrayList<>();
        Object ids = body.get("questionIds");
        if (ids instanceof List<?> l) {
            for (Object o : l) {
                questionIds.add(Long.valueOf(String.valueOf(o)));
            }
        }
        String action = String.valueOf(body.getOrDefault("action", "add"));
        if (tagNames.isEmpty()) {
            return R.error("请选择标签");
        }
        if (questionIds.isEmpty()) {
            return R.error("请选择题目");
        }

        int updated = 0;
        boolean add = "add".equals(action);
        for (Long qid : questionIds) {
            String current = tagMapper.getQuestionTags(qid);
            if (current == null) {
                continue; // 题目不存在或已删除
            }
            List<String> parts = new ArrayList<>();
            for (String p : current.replace("，", ",").split(",")) {
                String t = p.trim();
                if (!t.isEmpty() && !parts.contains(t)) {
                    parts.add(t);
                }
            }
            String next;
            if (add) {
                boolean changed = false;
                for (String n : tagNames) {
                    if (!parts.contains(n)) {
                        parts.add(n);
                        changed = true;
                    }
                }
                if (!changed) {
                    continue;
                }
                next = String.join(",", parts);
            } else {
                List<String> kept = new ArrayList<>(parts);
                kept.removeAll(tagNames);
                if (kept.size() == parts.size()) {
                    continue; // 没有需要移除的
                }
                next = String.join(",", kept);
            }
            tagMapper.updateQuestionTags(qid, next);
            updated++;
        }
        return R.success((add ? "已为 " : "已从 ") + updated + " 道题目" + (add ? "添加标签" : "移除标签"));
    }

    private boolean exists(String name, Long excludeId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, name);
        if (excludeId != null) {
            wrapper.ne(Tag::getId, excludeId);
        }
        return tagMapper.selectCount(wrapper) > 0;
    }
}
