package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.Competition;
import com.poti.admin.entity.CompetitionRecord;
import com.poti.admin.entity.CompetitionSignup;
import com.poti.admin.entity.User;
import com.poti.admin.mapper.CompetitionMapper;
import com.poti.admin.mapper.CompetitionRecordMapper;
import com.poti.admin.mapper.CompetitionSignupMapper;
import com.poti.admin.mapper.UserMapper;
import com.poti.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 竞赛管理接口（后台）。
 * 竞赛数据在 poti_practice 库，用户昵称/头像在 poti_user 库，
 * 均通过 dynamic-datasource @DS 直查（不依赖其他服务）。
 */
@RestController
@RequestMapping("/admin/competition")
@RequiredArgsConstructor
public class CompetitionAdminController {

    private final CompetitionMapper competitionMapper;
    private final CompetitionSignupMapper signupMapper;
    private final CompetitionRecordMapper recordMapper;
    private final UserMapper userMapper;

    /** 动态状态：草稿（status=0） */
    private static final String STATE_DRAFT = "draft";
    /** 动态状态：报名中（已发布且未到开赛时间） */
    private static final String STATE_SIGNUP = "signup";
    /** 动态状态：进行中（开赛～结束时间窗内） */
    private static final String STATE_ONGOING = "ongoing";
    /** 动态状态：已结束（超过结束时间） */
    private static final String STATE_ENDED = "ended";

    /**
     * 分页列表（含动态 state、报名数、交卷数）。
     * 报名数/交卷数用两条聚合 SQL 批量组装，避免复杂 join。
     */
    @PostMapping("/page")
    public R<Map<String, Object>> page(@RequestBody Map<String, Object> body) {
        long pageNum = longVal(body.get("pageNum"), 1);
        long pageSize = longVal(body.get("pageSize"), 10);
        String title = strVal(body.get("title"));

        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isBlank()) {
            wrapper.like(Competition::getTitle, title.trim());
        }
        wrapper.orderByDesc(Competition::getId);
        Page<Competition> page = competitionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 两条聚合 SQL 组装报名数/交卷数
        Map<Long, Long> signupCounts = new HashMap<>();
        Map<Long, Long> submitCounts = new HashMap<>();
        List<Competition> records = page.getRecords();
        if (!records.isEmpty()) {
            List<Long> ids = records.stream().map(Competition::getId).toList();
            for (Map<String, Object> row : signupMapper.selectSignupCountBatch(ids)) {
                signupCounts.put(toLong(row.get("competitionId")), toLong(row.get("cnt")));
            }
            for (Map<String, Object> row : recordMapper.selectSubmitCountBatch(ids)) {
                submitCounts.put(toLong(row.get("competitionId")), toLong(row.get("cnt")));
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Competition c : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("title", c.getTitle());
            item.put("description", c.getDescription());
            item.put("categoryId", c.getCategoryId());
            item.put("questionCount", c.getQuestionCount());
            item.put("durationMinutes", c.getDurationMinutes());
            item.put("startTime", c.getStartTime());
            item.put("endTime", c.getEndTime());
            item.put("scorePerQuestion", c.getScorePerQuestion());
            item.put("status", c.getStatus());
            item.put("createTime", c.getCreateTime());
            // 动态状态（不落库，按时间窗推导）
            item.put("state", stateOf(c, now));
            item.put("signupCount", signupCounts.getOrDefault(c.getId(), 0L));
            item.put("submitCount", submitCounts.getOrDefault(c.getId(), 0L));
            list.add(item);
        }
        return R.success(pageData(list, page.getTotal()));
    }

    /**
     * 新建（落为草稿）或更新草稿。
     * 校验：end>start、题目数量 1-50、时长 1-180 分钟；仅草稿状态可编辑。
     */
    @PostMapping("/save")
    public R<Void> save(@RequestBody Competition competition,
                        @RequestHeader(value = "X-User-Id", required = false) Long adminId) {
        if (adminId == null) {
            return R.error("未登录");
        }
        if (competition.getTitle() == null || competition.getTitle().isBlank()) {
            return R.error("竞赛名称不能为空");
        }
        if (competition.getTitle().length() > 100) {
            return R.error("竞赛名称不能超过100字");
        }
        if (competition.getCategoryId() == null) {
            return R.error("请选择题库分类");
        }
        if (competition.getQuestionCount() == null || competition.getQuestionCount() < 1
                || competition.getQuestionCount() > 50) {
            return R.error("题目数量须在 1-50 之间");
        }
        if (competition.getDurationMinutes() == null || competition.getDurationMinutes() < 1
                || competition.getDurationMinutes() > 180) {
            return R.error("答题时长须在 1-180 分钟之间");
        }
        if (competition.getStartTime() == null || competition.getEndTime() == null) {
            return R.error("请设置开赛时间与结束时间");
        }
        if (!competition.getEndTime().isAfter(competition.getStartTime())) {
            return R.error("结束时间必须晚于开赛时间");
        }
        if (competition.getScorePerQuestion() == null || competition.getScorePerQuestion() < 1) {
            competition.setScorePerQuestion(10);
        }

        if (competition.getId() == null) {
            // 新建：一律先落草稿，由「发布」操作进入状态机
            competition.setId(null);
            competition.setStatus(0);
            competition.setCreatedBy(adminId);
            competitionMapper.insert(competition);
        } else {
            Competition db = competitionMapper.selectById(competition.getId());
            if (db == null) {
                return R.error("竞赛不存在或已被删除");
            }
            if (db.getStatus() == null || db.getStatus() != 0) {
                return R.error("仅草稿状态可编辑");
            }
            competition.setStatus(0);
            competition.setCreatedBy(db.getCreatedBy());
            competitionMapper.updateById(competition);
        }
        return R.success();
    }

    /** 发布：status 0→1（不可逆），重复发布报错 */
    @PostMapping("/publish/{id}")
    public R<Void> publish(@PathVariable Long id) {
        Competition db = competitionMapper.selectById(id);
        if (db == null) {
            return R.error("竞赛不存在或已被删除");
        }
        if (db.getStatus() != null && db.getStatus() != 0) {
            return R.error("已发布过");
        }
        Competition upd = new Competition();
        upd.setId(id);
        upd.setStatus(1);
        competitionMapper.updateById(upd);
        return R.success();
    }

    /**
     * 成绩列表（已交卷记录），按 得分 desc、用时 asc、交卷时间 asc 排序，
     * 名次 = 分页偏移 + 行号；昵称/头像从 poti_user 库批量回填。
     */
    @GetMapping("/{id}/records")
    public R<Map<String, Object>> records(@PathVariable Long id,
                                          @RequestParam(defaultValue = "1") long pageNum,
                                          @RequestParam(defaultValue = "10") long pageSize) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null) {
            return R.error("竞赛不存在或已被删除");
        }

        LambdaQueryWrapper<CompetitionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompetitionRecord::getCompetitionId, id)
                .isNotNull(CompetitionRecord::getSubmitTime)
                .orderByDesc(CompetitionRecord::getScore)
                .orderByAsc(CompetitionRecord::getCostSeconds)
                .orderByAsc(CompetitionRecord::getSubmitTime);
        Page<CompetitionRecord> page = recordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 批量回填昵称/头像（poti_user 库）
        List<Long> userIds = page.getRecords().stream().map(CompetitionRecord::getUserId).distinct().toList();
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User u : userMapper.selectBatchIds(userIds)) {
                userMap.put(u.getId(), u);
            }
        }

        List<Map<String, Object>> list = new ArrayList<>();
        long rankBase = (pageNum - 1) * pageSize;
        for (int i = 0; i < page.getRecords().size(); i++) {
            CompetitionRecord r = page.getRecords().get(i);
            User u = userMap.get(r.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", rankBase + i + 1);
            item.put("userId", r.getUserId());
            item.put("nickname", u != null && u.getNickname() != null ? u.getNickname() : "用户" + r.getUserId());
            item.put("avatar", u != null ? u.getAvatarUrl() : null);
            item.put("score", r.getScore());
            item.put("correctCount", r.getCorrectCount());
            item.put("costSeconds", r.getCostSeconds());
            item.put("submitTime", r.getSubmitTime());
            list.add(item);
        }
        return R.success(pageData(list, page.getTotal()));
    }

    /**
     * 删除竞赛（仅草稿或已结束状态可删）。
     * 主表逻辑删；子表清理：signup 无 deleted 列走物理 DELETE，record 保留作历史成绩。
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Competition db = competitionMapper.selectById(id);
        if (db == null) {
            return R.error("竞赛不存在或已被删除");
        }
        String state = stateOf(db, LocalDateTime.now());
        if (!STATE_DRAFT.equals(state) && !STATE_ENDED.equals(state)) {
            return R.error("仅草稿或已结束的竞赛可删除");
        }
        competitionMapper.deleteById(id);
        // signup 表无 deleted 列，物理 DELETE 对应报名记录
        LambdaQueryWrapper<CompetitionSignup> sw = new LambdaQueryWrapper<>();
        sw.eq(CompetitionSignup::getCompetitionId, id);
        signupMapper.delete(sw);
        return R.success();
    }

    /** 动态状态推导：status=0 草稿；否则按时间窗推导（与需求文档 2.3 状态机一致） */
    private String stateOf(Competition c, LocalDateTime now) {
        if (c.getStatus() == null || c.getStatus() == 0) {
            return STATE_DRAFT;
        }
        if (now.isBefore(c.getStartTime())) {
            return STATE_SIGNUP;
        }
        if (!now.isAfter(c.getEndTime())) {
            return STATE_ONGOING;
        }
        return STATE_ENDED;
    }

    private Map<String, Object> pageData(List<?> list, long total) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", total);
        return data;
    }

    private Long toLong(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    private long longVal(Object o, long def) {
        try {
            return o == null ? def : Long.parseLong(String.valueOf(o));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private String strVal(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
