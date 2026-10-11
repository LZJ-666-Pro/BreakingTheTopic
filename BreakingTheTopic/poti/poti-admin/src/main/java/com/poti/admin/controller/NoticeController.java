package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.Notice;
import com.poti.admin.mapper.NoticeMapper;
import com.poti.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 公告管理接口（后台）。
 */
@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeMapper noticeMapper;

    /** 分页列表 */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer displayType,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Notice::getStatus, status);
        }
        if (displayType != null) {
            wrapper.eq(Notice::getDisplayType, displayType);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Notice::getTitle, keyword.trim());
        }
        wrapper.orderByDesc(Notice::getId);

        Page<Notice> page = noticeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        return R.success(data);
    }

    /** 新增公告 */
    @PostMapping
    public R<Void> create(@RequestBody Map<String, Object> body) {
        Notice notice = new Notice();
        parse(body, notice);
        if (notice.getTitle() == null || notice.getTitle().isBlank()) {
            return R.error("公告标题不能为空");
        }
        if (notice.getContent() == null || notice.getContent().isBlank()) {
            return R.error("公告内容不能为空");
        }
        notice.setId(null);
        notice.setReadCount(0);
        notice.setClickCount(0);
        if (notice.getTargetType() != null && notice.getTargetType() == 2
                && (notice.getTargetUserIds() == null || notice.getTargetUserIds().isBlank())) {
            return R.error("指定用户模式下请填写用户ID");
        }
        noticeMapper.insert(notice);
        return R.success();
    }

    /** 编辑公告 */
    @PutMapping
    public R<Void> update(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") == null ? null : Long.valueOf(String.valueOf(body.get("id")));
        if (id == null) {
            return R.error("缺少公告 ID");
        }
        Notice db = noticeMapper.selectById(id);
        if (db == null) {
            return R.error("公告不存在或已被删除");
        }
        parse(body, db);
        db.setId(id);
        noticeMapper.updateById(db);
        return R.success();
    }

    /** 启用 / 停用 */
    @PutMapping("/status/{id}")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return R.error("状态非法（1启用 0停用）");
        }
        Notice db = noticeMapper.selectById(id);
        if (db == null) {
            return R.error("公告不存在或已被删除");
        }
        db.setStatus(status);
        noticeMapper.updateById(db);
        return R.success();
    }

    /** 删除公告 */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Notice db = noticeMapper.selectById(id);
        if (db == null) {
            return R.error("公告不存在或已被删除");
        }
        noticeMapper.deleteById(id);
        return R.success();
    }

    private void parse(Map<String, Object> body, Notice target) {
        if (body.containsKey("title")) {
            target.setTitle(str(body.get("title")).trim());
        }
        if (body.containsKey("content")) {
            target.setContent(String.valueOf(body.get("content")));
        }
        if (body.containsKey("displayType")) {
            target.setDisplayType(intVal(body.get("displayType"), 0));
        }
        if (body.containsKey("targetType")) {
            target.setTargetType(intVal(body.get("targetType"), 0));
        }
        if (body.containsKey("targetUserIds")) {
            target.setTargetUserIds(str(body.get("targetUserIds")).trim());
        }
        if (body.containsKey("forceRead")) {
            target.setForceRead(intVal(body.get("forceRead"), 0));
        }
        // 强制阅读时不可关闭
        if (target.getForceRead() != null && target.getForceRead() == 1) {
            target.setClosable(0);
        } else if (body.containsKey("closable")) {
            target.setClosable(intVal(body.get("closable"), 1));
        }
        if (body.containsKey("startTime")) {
            target.setStartTime(parseTime(body.get("startTime")));
        }
        if (body.containsKey("endTime")) {
            target.setEndTime(parseTime(body.get("endTime")));
        }
        if (body.containsKey("status")) {
            target.setStatus(intVal(body.get("status"), 1));
        }
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

    private LocalDateTime parseTime(Object o) {
        if (o == null || String.valueOf(o).isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(String.valueOf(o).replace("T", " ").replace("Z", "").substring(0, 19));
        } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
            return null;
        }
    }
}
