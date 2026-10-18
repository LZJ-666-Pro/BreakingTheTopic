package com.poti.question.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.common.R;
import com.poti.question.entity.DailyAnswer;
import com.poti.question.entity.DailyQuestion;
import com.poti.question.entity.Question;
import com.poti.question.entity.SpecialQuestion;
import com.poti.question.entity.SpecialTopic;
import com.poti.question.mapper.DailyAnswerMapper;
import com.poti.question.mapper.DailyQuestionMapper;
import com.poti.question.mapper.QuestionMapper;
import com.poti.question.mapper.SpecialQuestionMapper;
import com.poti.question.mapper.SpecialTopicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 刷题专题与每日一题（小程序端，公开接口）。
 */
@RestController
@RequestMapping("/question")
public class SpecialController {

    @Autowired
    private SpecialTopicMapper specialTopicMapper;
    @Autowired
    private SpecialQuestionMapper specialQuestionMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private DailyQuestionMapper dailyQuestionMapper;
    @Autowired
    private DailyAnswerMapper dailyAnswerMapper;

    // ==================== 专题 ====================

    /** 专题列表（仅上架，按推荐权重排序） */
    @GetMapping("/special/list")
    public R<List<Map<String, Object>>> specialList() {
        try {
            LambdaQueryWrapper<SpecialTopic> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SpecialTopic::getStatus, 1)
                   .orderByDesc(SpecialTopic::getSort)
                   .orderByDesc(SpecialTopic::getId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (SpecialTopic t : specialTopicMapper.selectList(wrapper)) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", t.getId());
                item.put("name", t.getName());
                item.put("cover", t.getCover());
                item.put("description", t.getDescription());
                item.put("difficulty", t.getDifficulty());
                item.put("estimatedDays", t.getEstimatedDays());
                item.put("unlockType", t.getUnlockType());
                item.put("questionCount", specialQuestionMapper.selectCount(
                        new LambdaQueryWrapper<SpecialQuestion>().eq(SpecialQuestion::getTopicId, t.getId())));
                result.add(item);
            }
            return R.success(result);
        } catch (Exception e) {
            return R.error("获取专题列表失败");
        }
    }

    /** 专题题目列表（按专题内顺序） */
    @GetMapping("/special/{id}/questions")
    public R<Map<String, Object>> specialQuestions(@PathVariable Long id) {
        try {
            SpecialTopic topic = specialTopicMapper.selectById(id);
            if (topic == null || topic.getStatus() == null || topic.getStatus() != 1) {
                return R.error("专题不存在或已下架");
            }
            LambdaQueryWrapper<SpecialQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SpecialQuestion::getTopicId, id)
                   .orderByAsc(SpecialQuestion::getSort);
            List<Map<String, Object>> questions = new ArrayList<>();
            int index = 1;
            for (SpecialQuestion sq : specialQuestionMapper.selectList(wrapper)) {
                Question q = questionMapper.selectById(sq.getQuestionId());
                if (q == null || (q.getDeleted() != null && q.getDeleted() != 0)) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", q.getId());
                item.put("index", index++);
                // 题干优先取 title（题库题目题干存于 title 字段），旧数据回退 content
                item.put("content", q.getTitle() != null && !q.getTitle().isEmpty() ? q.getTitle() : q.getContent());
                item.put("difficulty", q.getDifficulty());
                questions.add(item);
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", topic.getId());
            data.put("name", topic.getName());
            data.put("description", topic.getDescription());
            data.put("difficulty", topic.getDifficulty());
            data.put("estimatedDays", topic.getEstimatedDays());
            data.put("unlockType", topic.getUnlockType());
            data.put("questions", questions);
            return R.success(data);
        } catch (Exception e) {
            return R.error("获取专题题目失败");
        }
    }

    // ==================== 每日一题 ====================

    /**
     * 今日一题（未配置时返回空数据）。
     * 登录用户传 userId 时额外返回 hasAnswered——当天已作答则小程序端不再展示入口。
     */
    @GetMapping("/daily/today")
    public R<Map<String, Object>> today(@RequestParam(required = false) Long userId) {
        try {
            Map<String, Object> data = buildDaily(LocalDate.now());
            if (userId != null) {
                data.put("hasAnswered", hasAnswered(userId, LocalDate.now()));
            }
            return R.success(data);
        } catch (Exception e) {
            return R.error("获取今日一题失败");
        }
    }

    /** 上报每日一题作答完成：用户维度一天一条，答对答错都算「已答」 */
    @PostMapping("/daily/submit")
    public R<Void> submitDailyAnswer(@RequestBody Map<String, Object> body) {
        try {
            Object uid = body.get("userId");
            Object qid = body.get("questionId");
            if (uid == null || qid == null) {
                return R.error("参数不完整");
            }
            Long userId = Long.valueOf(String.valueOf(uid));
            Long questionId = Long.valueOf(String.valueOf(qid));
            boolean correct = Boolean.parseBoolean(String.valueOf(body.get("isCorrect")));

            LocalDate today = LocalDate.now();
            DailyAnswer answer = new DailyAnswer();
            answer.setUserId(userId);
            answer.setQuestionId(questionId);
            answer.setAnswerDate(today);
            answer.setIsCorrect(correct ? 1 : 0);
            try {
                dailyAnswerMapper.insert(answer);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 当天已上报过：仅更新对错结果（幂等，重复提交不报错）
                DailyAnswer update = new DailyAnswer();
                update.setIsCorrect(correct ? 1 : 0);
                dailyAnswerMapper.update(update, new LambdaQueryWrapper<DailyAnswer>()
                        .eq(DailyAnswer::getUserId, userId)
                        .eq(DailyAnswer::getAnswerDate, today));
            }
            return R.success(null);
        } catch (Exception e) {
            return R.error("上报每日一题作答失败");
        }
    }

    private boolean hasAnswered(Long userId, LocalDate date) {
        return dailyAnswerMapper.selectCount(new LambdaQueryWrapper<DailyAnswer>()
                .eq(DailyAnswer::getUserId, userId)
                .eq(DailyAnswer::getAnswerDate, date)) > 0;
    }

    /** 历史每日一题（最近 limit 天，按日期倒序） */
    @GetMapping("/daily/history")
    public R<List<Map<String, Object>>> history(@RequestParam(defaultValue = "7") int limit) {
        try {
            int size = Math.min(Math.max(limit, 1), 31);
            LambdaQueryWrapper<DailyQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.le(DailyQuestion::getDate, LocalDate.now())
                   .orderByDesc(DailyQuestion::getDate)
                   .last("LIMIT " + size);
            List<Map<String, Object>> result = new ArrayList<>();
            for (DailyQuestion d : dailyQuestionMapper.selectList(wrapper)) {
                result.add(buildDaily(d.getDate()));
            }
            return R.success(result);
        } catch (Exception e) {
            return R.error("获取每日一题历史失败");
        }
    }

    private Map<String, Object> buildDaily(LocalDate date) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("date", String.valueOf(date));
        data.put("questionId", null);
        data.put("content", null);
        data.put("difficulty", null);
        data.put("reason", "");
        data.put("hint", "");
        LambdaQueryWrapper<DailyQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyQuestion::getDate, date).last("LIMIT 1");
        DailyQuestion daily = dailyQuestionMapper.selectOne(wrapper);
        if (daily != null) {
            data.put("questionId", daily.getQuestionId());
            data.put("reason", daily.getReason());
            data.put("hint", daily.getHint());
            Question q = questionMapper.selectById(daily.getQuestionId());
            if (q != null && (q.getDeleted() == null || q.getDeleted() == 0)) {
                // 题干优先取 title，旧数据回退 content
                data.put("content", q.getTitle() != null && !q.getTitle().isEmpty() ? q.getTitle() : q.getContent());
                data.put("difficulty", q.getDifficulty());
            }
        }
        return data;
    }
}
