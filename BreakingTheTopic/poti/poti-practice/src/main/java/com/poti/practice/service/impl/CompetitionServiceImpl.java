package com.poti.practice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poti.common.R;
import com.poti.practice.entity.Competition;
import com.poti.practice.entity.CompetitionRecord;
import com.poti.practice.entity.CompetitionSignup;
import com.poti.practice.feign.QuestionFeignClient;
import com.poti.practice.feign.UserFeignClient;
import com.poti.practice.mapper.CompetitionMapper;
import com.poti.practice.mapper.CompetitionRecordMapper;
import com.poti.practice.mapper.CompetitionSignupMapper;
import com.poti.practice.mapper.CompetitionUserMapper;
import com.poti.practice.service.CompetitionService;
import com.poti.practice.service.ZhipuJudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition> implements CompetitionService {

    @Autowired
    private CompetitionSignupMapper signupMapper;

    @Autowired
    private CompetitionRecordMapper recordMapper;

    @Autowired
    private CompetitionUserMapper userMapper;

    @Autowired
    private QuestionFeignClient questionFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ZhipuJudgeService zhipuJudgeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> pageList(Long userId, Integer pageNum, Integer pageSize, String tab) {
        LocalDateTime now = LocalDateTime.now();

        // 仅展示已发布的竞赛，动态状态由 start_time/end_time 推导
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 1);
        if ("signup".equals(tab)) {
            // 报名中：未结束且我未报名——对应按钮「立即报名」
            wrapper.gt(Competition::getEndTime, now);
            wrapper.notExists("SELECT 1 FROM competition_signup cs WHERE cs.competition_id = competition.id AND cs.user_id = " + userId);
            wrapper.orderByAsc(Competition::getStartTime);
        } else if ("ongoing".equals(tab)) {
            // 进行中：未结束且我已报名——对应按钮「查看详情」（未开赛）/「进入比赛」（已开赛）
            wrapper.gt(Competition::getEndTime, now);
            wrapper.exists("SELECT 1 FROM competition_signup cs WHERE cs.competition_id = competition.id AND cs.user_id = " + userId);
            wrapper.orderByAsc(Competition::getStartTime);
        } else if ("ended".equals(tab)) {
            // 已结束：now > end_time
            wrapper.lt(Competition::getEndTime, now);
            wrapper.orderByDesc(Competition::getEndTime);
        } else {
            wrapper.orderByDesc(Competition::getStartTime);
        }

        long total = baseMapper.selectCount(wrapper);
        int offset = (pageNum - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);
        List<Competition> competitions = baseMapper.selectList(wrapper);

        // 一次聚合查询报名人数 + 一次查询我的报名状态，避免 N+1
        List<Long> compIds = competitions.stream().map(Competition::getId).collect(Collectors.toList());
        Map<Long, Long> signupCounts = new HashMap<>();
        Map<Long, Boolean> mySignedMap = new HashMap<>();
        if (!compIds.isEmpty()) {
            for (Map<String, Object> row : signupMapper.countByCompetitionIds(compIds)) {
                Object cid = row.get("competitionId");
                Object cnt = row.get("cnt");
                if (cid instanceof Number && cnt instanceof Number) {
                    signupCounts.put(((Number) cid).longValue(), ((Number) cnt).longValue());
                }
            }
            LambdaQueryWrapper<CompetitionSignup> sw = new LambdaQueryWrapper<>();
            sw.eq(CompetitionSignup::getUserId, userId)
              .in(CompetitionSignup::getCompetitionId, compIds);
            for (CompetitionSignup s : signupMapper.selectList(sw)) {
                mySignedMap.put(s.getCompetitionId(), true);
            }
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Competition c : competitions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("title", c.getTitle());
            item.put("description", c.getDescription());
            item.put("questionCount", c.getQuestionCount());
            item.put("durationMinutes", c.getDurationMinutes());
            item.put("startTime", c.getStartTime());
            item.put("endTime", c.getEndTime());
            item.put("state", deriveState(c, now));
            item.put("signupCount", signupCounts.getOrDefault(c.getId(), 0L));
            item.put("mySigned", mySignedMap.getOrDefault(c.getId(), false));
            list.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public Map<String, Object> detail(Long userId, Long id) {
        Competition c = getById(id);
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            throw new RuntimeException("竞赛不存在或未发布");
        }
        LocalDateTime now = LocalDateTime.now();

        boolean mySigned = signupMapper.selectCount(new LambdaQueryWrapper<CompetitionSignup>()
                .eq(CompetitionSignup::getCompetitionId, id)
                .eq(CompetitionSignup::getUserId, userId)) > 0;

        // 我的成绩：已交卷才返回 score/correctCount/costSeconds/rank
        Map<String, Object> myRecord = null;
        CompetitionRecord record = getMyRecord(id, userId);
        if (record != null && record.getSubmitTime() != null) {
            myRecord = new HashMap<>();
            myRecord.put("score", record.getScore());
            myRecord.put("correctCount", record.getCorrectCount());
            myRecord.put("costSeconds", record.getCostSeconds());
            Integer ahead = recordMapper.countAhead(id, record.getScore(), record.getCostSeconds());
            myRecord.put("rank", (ahead == null ? 0 : ahead) + 1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", c.getId());
        result.put("title", c.getTitle());
        result.put("description", c.getDescription());
        result.put("categoryId", c.getCategoryId());
        result.put("questionCount", c.getQuestionCount());
        result.put("durationMinutes", c.getDurationMinutes());
        result.put("startTime", c.getStartTime());
        result.put("endTime", c.getEndTime());
        result.put("scorePerQuestion", c.getScorePerQuestion());
        result.put("state", deriveState(c, now));
        result.put("signupCount", signupMapper.selectCount(
                new LambdaQueryWrapper<CompetitionSignup>().eq(CompetitionSignup::getCompetitionId, id)));
        result.put("mySigned", mySigned);
        result.put("myRecord", myRecord);
        return result;
    }

    @Override
    public void signup(Long userId, Long id) {
        Competition c = getById(id);
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            throw new RuntimeException("竞赛不存在或未发布");
        }
        // 报名窗口：截止时间前均可报名；开赛前仅会员可提前报名（优先报名权益），非会员开赛前 24 小时内开放
        if (LocalDateTime.now().isAfter(c.getEndTime())) {
            throw new RuntimeException("报名已截止");
        }
        if (LocalDateTime.now().isBefore(c.getStartTime()) && !isVipUser(userId)) {
            LocalDateTime openTime = c.getStartTime().minusHours(24);
            if (LocalDateTime.now().isBefore(openTime)) {
                throw new RuntimeException("报名将于开赛前 24 小时开放，开通会员可提前报名");
            }
        }
        Long existed = signupMapper.selectCount(new LambdaQueryWrapper<CompetitionSignup>()
                .eq(CompetitionSignup::getCompetitionId, id)
                .eq(CompetitionSignup::getUserId, userId));
        if (existed != null && existed > 0) {
            throw new RuntimeException("已报名过，请勿重复报名");
        }
        CompetitionSignup signup = new CompetitionSignup();
        signup.setCompetitionId(id);
        signup.setUserId(userId);
        try {
            signupMapper.insert(signup);
        } catch (DuplicateKeyException e) {
            // 唯一索引兜底：并发重复报名
            throw new RuntimeException("已报名过，请勿重复报名");
        }
    }

    @Override
    public List<Map<String, Object>> start(Long userId, Long id) {
        Competition c = getById(id);
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            throw new RuntimeException("竞赛不存在或未发布");
        }
        if (signupMapper.selectCount(new LambdaQueryWrapper<CompetitionSignup>()
                .eq(CompetitionSignup::getCompetitionId, id)
                .eq(CompetitionSignup::getUserId, userId)) == 0) {
            throw new RuntimeException("请先报名再开赛");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(c.getStartTime())) {
            throw new RuntimeException("竞赛尚未开始");
        }
        if (now.isAfter(c.getEndTime())) {
            throw new RuntimeException("竞赛已结束");
        }
        // 每场竞赛仅可交卷一次；已交卷拦截，未交卷的走断点续答（恢复题目快照）
        CompetitionRecord existed = getMyRecord(id, userId);
        if (existed != null && existed.getSubmitTime() != null) {
            throw new RuntimeException("您已交过卷，请在竞赛详情页查看成绩");
        }

        List<Map<String, Object>> questions;
        if (existed != null) {
            // 断点续答：按开赛时的题目快照恢复，题目顺序不变
            questions = restoreQuestionsFromSnapshot(existed.getQuestionIds());
            if (questions.isEmpty()) {
                throw new RuntimeException("题目快照恢复失败，请稍后重试");
            }
            return questions;
        }

        // 首次开赛：混合抽题，解答题(4)与填空题(3)按配额抽取，其余用单选/多选(1,2)补足，最后打乱顺序
        int wantCount = c.getQuestionCount() != null ? Math.max(1, c.getQuestionCount()) : 10;
        int essayQuota = Math.min(2, Math.max(1, wantCount / 6));   // 解答题：15题→2道
        int blankQuota = Math.min(3, Math.max(1, wantCount / 5));   // 填空题：15题→3道
        questions = new ArrayList<>();
        appendRandomQuestions(questions, c.getCategoryId(), essayQuota, "4");
        appendRandomQuestions(questions, c.getCategoryId(), blankQuota, "3");
        appendRandomQuestions(questions, c.getCategoryId(), wantCount - questions.size(), "1,2");
        if (questions.isEmpty()) {
            throw new RuntimeException("抽题失败，请稍后重试");
        }
        Collections.shuffle(questions);

        // 剥离答案/解析等判分元数据，仅透传前端作答所需字段，防止用户从响应中作弊
        List<Map<String, Object>> safeQuestions = new ArrayList<>();
        for (Map<String, Object> q : questions) {
            Map<String, Object> safe = new HashMap<>();
            safe.put("id", q.get("id"));
            safe.put("categoryId", q.get("categoryId"));
            safe.put("title", q.get("title"));
            safe.put("content", q.get("content"));
            safe.put("type", q.get("type"));
            safe.put("difficulty", q.get("difficulty"));
            safe.put("options", q.get("options"));
            safeQuestions.add(safe);
        }

        // 题目ID快照落库，保证每位参赛者题目公平可追溯
        List<Long> questionIds = new ArrayList<>();
        for (Map<String, Object> q : safeQuestions) {
            Object idObj = q.get("id");
            if (idObj instanceof Number) {
                questionIds.add(((Number) idObj).longValue());
            }
        }

        CompetitionRecord record = new CompetitionRecord();
        record.setCompetitionId(id);
        record.setUserId(userId);
        try {
            record.setQuestionIds(objectMapper.writeValueAsString(questionIds));
        } catch (Exception e) {
            throw new RuntimeException("生成题目快照失败");
        }
        record.setCreateTime(now);
        try {
            recordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            // 唯一索引兜底：并发重复开赛
            throw new RuntimeException("您已参与过本场竞赛作答");
        }

        return safeQuestions;
    }

    @Override
    public Map<String, Object> submit(Long userId, Long id, List<Map<String, Object>> answers) {
        Competition c = getById(id);
        if (c == null) {
            throw new RuntimeException("竞赛不存在");
        }
        CompetitionRecord record = getMyRecord(id, userId);
        if (record == null) {
            throw new RuntimeException("请先开赛再交卷");
        }
        if (record.getSubmitTime() != null) {
            throw new RuntimeException("已交卷，请勿重复提交");
        }
        LocalDateTime now = LocalDateTime.now();
        // 仅允许竞赛进行中交卷（到时自动交卷由前端倒计时触发）
        if (now.isBefore(c.getStartTime()) || now.isAfter(c.getEndTime())) {
            throw new RuntimeException("不在竞赛进行时间内");
        }

        // 组装用户答案：questionId -> answer
        Map<Long, String> userAnswers = new HashMap<>();
        if (answers != null) {
            for (Map<String, Object> a : answers) {
                Object qid = a.get("questionId");
                if (qid == null) {
                    continue;
                }
                Object ans = a.get("answer");
                userAnswers.put(Long.valueOf(qid.toString()), ans == null ? "" : ans.toString());
            }
        }

        // 解析开赛时的题目快照
        List<Long> questionIds;
        try {
            questionIds = objectMapper.readValue(record.getQuestionIds(), new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            throw new RuntimeException("题目快照解析失败");
        }

        // 逐题判分：1/2 单选多选精确匹配；3 填空标准化比对（支持 | 多候选）；4 解答题 AI 判分按比例得分
        // 同时记录每题明细 {questionId, answer, correct, score}，供交卷后试卷回顾
        int scorePerQuestion = c.getScorePerQuestion() != null ? c.getScorePerQuestion() : 10;
        int correctCount = 0;
        int totalScore = 0;
        List<Map<String, Object>> answerDetails = new ArrayList<>();
        for (Long qid : questionIds) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("questionId", qid);
            String userAns = userAnswers.get(qid);
            boolean unanswered = userAns == null || userAns.trim().isEmpty();
            detail.put("answer", unanswered ? "" : userAns);
            if (unanswered) {
                detail.put("correct", false);
                detail.put("score", 0);
                answerDetails.add(detail);
                continue;
            }
            Map<String, Object> meta = fetchQuestionMeta(qid);
            if (meta == null) {
                log.warn("竞赛{}交卷判分：题目{}元数据缺失，按0分处理", id, qid);
                detail.put("correct", false);
                detail.put("score", 0);
                answerDetails.add(detail);
                continue;
            }
            int type = meta.get("type") instanceof Number ? ((Number) meta.get("type")).intValue() : 1;
            String correctAns = meta.get("answer") == null ? null : meta.get("answer").toString();
            if (correctAns == null || correctAns.trim().isEmpty()) {
                detail.put("correct", false);
                detail.put("score", 0);
                answerDetails.add(detail);
                continue;
            }
            if (type == 3) {
                // 填空题：标准化比对（多候选用 | 分隔）
                boolean ok = matchBlank(correctAns, userAns);
                detail.put("correct", ok);
                detail.put("score", ok ? scorePerQuestion : 0);
                if (ok) {
                    correctCount++;
                    totalScore += scorePerQuestion;
                }
            } else if (type == 4) {
                // 解答题：AI 判分 0-100，按比例得分；失败降级为关键词命中判定
                int aiScore = zhipuJudgeService.judge(
                        meta.get("title") == null ? "" : meta.get("title").toString(),
                        correctAns,
                        meta.get("analysis") == null ? "" : meta.get("analysis").toString(),
                        userAns);
                if (aiScore < 0) {
                    aiScore = keywordFallback(correctAns, userAns) ? 100 : 0;
                    log.warn("竞赛{}解答题{} AI判分失败，关键词降级判定：{}", id, qid, aiScore);
                }
                int qScore = (int) Math.round(scorePerQuestion * aiScore / 100.0);
                detail.put("correct", aiScore >= 60);
                detail.put("score", qScore);
                totalScore += qScore;
                if (aiScore >= 60) {
                    correctCount++;
                }
            } else {
                // 单选/多选/判断：字母精确匹配（忽略大小写）
                boolean ok = correctAns.trim().equalsIgnoreCase(userAns.trim());
                detail.put("correct", ok);
                detail.put("score", ok ? scorePerQuestion : 0);
                if (ok) {
                    correctCount++;
                    totalScore += scorePerQuestion;
                }
            }
            answerDetails.add(detail);
        }
        int score = totalScore;
        // 用时 = 交卷时间 - 开赛时间，上限为竞赛时长
        int cap = (c.getDurationMinutes() != null ? c.getDurationMinutes() : 30) * 60;
        int costSeconds = 0;
        if (record.getCreateTime() != null) {
            costSeconds = (int) Duration.between(record.getCreateTime(), now).getSeconds();
        }
        if (costSeconds < 0) {
            costSeconds = 0;
        }
        if (costSeconds > cap) {
            costSeconds = cap;
        }

        record.setScore(score);
        record.setCorrectCount(correctCount);
        record.setCostSeconds(costSeconds);
        record.setSubmitTime(now);
        try {
            // 存储判分明细 [{questionId, answer, correct, score}]，供试卷回顾
            record.setAnswers(objectMapper.writeValueAsString(answerDetails));
        } catch (Exception e) {
            // 答案明细序列化失败不影响判分主流程
        }
        recordMapper.updateById(record);

        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("correctCount", correctCount);
        result.put("costSeconds", costSeconds);
        return result;
    }

    @Override
    public List<Map<String, Object>> rank(Long id) {
        Competition c = getById(id);
        if (c == null) {
            throw new RuntimeException("竞赛不存在");
        }
        // 排序规则：分数高在前；同分用时短在前；同分同用时交卷早在前，取前50
        LambdaQueryWrapper<CompetitionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompetitionRecord::getCompetitionId, id)
               .isNotNull(CompetitionRecord::getScore)
               .orderByDesc(CompetitionRecord::getScore)
               .orderByAsc(CompetitionRecord::getCostSeconds)
               .orderByAsc(CompetitionRecord::getSubmitTime)
               .last("LIMIT 50");
        List<CompetitionRecord> records = recordMapper.selectList(wrapper);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        // 跨库批量查询昵称/头像（poti_user.user 表）
        List<Long> userIds = records.stream().map(CompetitionRecord::getUserId)
                .distinct().collect(Collectors.toList());
        Map<Long, Map<String, Object>> userMap = new HashMap<>();
        for (Map<String, Object> u : userMapper.selectNicknameAndAvatar(userIds)) {
            Object uid = u.get("id");
            if (uid instanceof Number) {
                userMap.put(((Number) uid).longValue(), u);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (CompetitionRecord r : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("score", r.getScore());
            item.put("costSeconds", r.getCostSeconds());
            item.put("submitTime", r.getSubmitTime());
            Map<String, Object> u = userMap.get(r.getUserId());
            String nickname = u == null ? null : (String) u.get("nickname");
            item.put("nickname", maskNickname(nickname));
            item.put("avatarUrl", u == null ? null : u.get("avatarUrl"));
            result.add(item);
        }
        return result;
    }

    @Override
    public Map<String, Object> myList(Long userId, Integer pageNum, Integer pageSize) {
        long total = recordMapper.countMySubmitted(userId);
        int offset = (pageNum - 1) * pageSize;
        List<Map<String, Object>> list = recordMapper.selectMyList(userId, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public List<Map<String, Object>> review(Long userId, Long id) {
        Competition c = getById(id);
        if (c == null) {
            throw new RuntimeException("竞赛不存在");
        }
        CompetitionRecord record = getMyRecord(id, userId);
        if (record == null) {
            throw new RuntimeException("请先开赛再查看试卷回顾");
        }
        if (record.getSubmitTime() == null) {
            throw new RuntimeException("交卷后可查看试卷回顾");
        }

        // 明细 questionId -> {answer, correct, score}
        Map<Long, Map<String, Object>> detailMap = new HashMap<>();
        try {
            List<Map<String, Object>> details = objectMapper.readValue(
                    record.getAnswers(), new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> d : details) {
                if (d.get("questionId") != null) {
                    detailMap.put(Long.valueOf(d.get("questionId").toString()), d);
                }
            }
        } catch (Exception e) {
            log.warn("竞赛{}答题明细解析失败，回顾将缺少判分标记: {}", id, e.getMessage());
        }

        // 按快照顺序拉取题目（含答案/解析）
        List<Map<String, Object>> questions = new ArrayList<>();
        try {
            List<Long> questionIds = objectMapper.readValue(
                    record.getQuestionIds(), new TypeReference<List<Long>>() {});
            if (questionIds != null && !questionIds.isEmpty()) {
                String joined = questionIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                R<List<Map<String, Object>>> resp = questionFeignClient.getReviewQuestions(joined);
                if (resp != null && resp.getCode() != null && resp.getCode() == 200 && resp.getData() != null) {
                    questions.addAll(resp.getData());
                }
            }
        } catch (Exception e) {
            log.warn("竞赛{}回顾取题失败: {}", id, e.getMessage());
        }
        if (questions.isEmpty()) {
            throw new RuntimeException("试卷回顾加载失败，请稍后重试");
        }

        // 组装回顾列表：题干/题型/选项 + 我的作答/参考答案/解析/判分
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> q : questions) {
            if (!(q.get("id") instanceof Number)) {
                continue;
            }
            Long qid = ((Number) q.get("id")).longValue();
            Map<String, Object> d = detailMap.get(qid);
            Map<String, Object> item = new HashMap<>();
            item.put("questionId", qid);
            item.put("title", q.get("title"));
            item.put("content", q.get("content"));
            item.put("type", q.get("type"));
            item.put("options", q.get("options"));
            item.put("correctAnswer", q.get("answer"));
            item.put("analysis", q.get("analysis"));
            item.put("userAnswer", d == null ? "" : d.get("answer"));
            Object correctMark = d == null ? null : d.get("correct");
            Object scoreMark = d == null ? null : d.get("score");
            // 兼容旧版明细（无判分标记）：1/2/3 题型可确定性重算；4 解答题无法还原，保持未判分
            if (d != null && correctMark == null) {
                int t = q.get("type") instanceof Number ? ((Number) q.get("type")).intValue() : 1;
                String ua = d.get("answer") == null ? "" : d.get("answer").toString();
                String ca = q.get("answer") == null ? "" : q.get("answer").toString();
                if (!ua.trim().isEmpty() && !ca.trim().isEmpty() && t != 4) {
                    boolean ok = t == 3 ? matchBlank(ca, ua) : ca.trim().equalsIgnoreCase(ua.trim());
                    correctMark = ok;
                    scoreMark = ok ? c.getScorePerQuestion() : 0;
                }
            }
            item.put("correct", correctMark);
            item.put("score", scoreMark);
            result.add(item);
        }
        return result;
    }

    /** 查询当前用户在某场竞赛的作答记录 */
    private CompetitionRecord getMyRecord(Long competitionId, Long userId) {
        return recordMapper.selectOne(new LambdaQueryWrapper<CompetitionRecord>()
                .eq(CompetitionRecord::getCompetitionId, competitionId)
                .eq(CompetitionRecord::getUserId, userId));
    }

    /** 按题型批次追加抽题（单题型失败时跳过，由其他题型补足；跨批次去重） */
    private void appendRandomQuestions(List<Map<String, Object>> acc, Long categoryId, int limit, String types) {
        if (limit <= 0) {
            return;
        }
        try {
            R<List<Map<String, Object>>> resp = questionFeignClient.getRandomQuestions(categoryId, limit, types);
            if (resp != null && resp.getCode() != null && resp.getCode() == 200 && resp.getData() != null) {
                for (Map<String, Object> q : resp.getData()) {
                    String qid = String.valueOf(q.get("id"));
                    boolean exists = acc.stream().anyMatch(x -> String.valueOf(x.get("id")).equals(qid));
                    if (!exists) {
                        acc.add(q);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("竞赛抽题批次失败（types={}）: {}", types, e.getMessage());
        }
    }

    /** 断点续答：按快照 questionIds 恢复题目（题目服务已剥离答案/解析） */
    private List<Map<String, Object>> restoreQuestionsFromSnapshot(String questionIdsJson) {
        List<Map<String, Object>> questions = new ArrayList<>();
        try {
            List<Long> ids = objectMapper.readValue(questionIdsJson, new TypeReference<List<Long>>() {});
            if (ids == null || ids.isEmpty()) {
                return questions;
            }
            String joined = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
            R<List<Map<String, Object>>> resp = questionFeignClient.getQuestionsByIds(joined);
            if (resp != null && resp.getCode() != null && resp.getCode() == 200 && resp.getData() != null) {
                questions.addAll(resp.getData());
            }
        } catch (Exception e) {
            log.warn("恢复题目快照失败: {}", e.getMessage());
        }
        return questions;
    }

    /** 拉取题目判分元数据（type/answer/analysis/title） */
    private Map<String, Object> fetchQuestionMeta(Long questionId) {
        try {
            R<Map<String, Object>> resp = questionFeignClient.getQuestionById(questionId);
            if (resp != null && resp.getCode() != null && resp.getCode() == 200 && resp.getData() != null) {
                return resp.getData();
            }
        } catch (Exception e) {
            // 单题获取失败按答错处理
        }
        return null;
    }

    /** 填空题判分：标准化后与多候选答案比对（候选以 | 分隔） */
    private boolean matchBlank(String correctAns, String userAns) {
        String u = normalizeText(userAns);
        for (String cand : correctAns.split("\\|")) {
            if (normalizeText(cand).equals(u)) {
                return true;
            }
        }
        return false;
    }

    /** 文本标准化：trim、小写、全角转半角、空白归一 */
    private String normalizeText(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().toLowerCase()
                .replace('（', '(').replace('）', ')')
                .replace('，', ',').replace('：', ':').replace('；', ';')
                .replaceAll("\\s+", " ");
    }

    /** 解答题降级判分：答案按关键词拆分（| , 、 空格），命中过半即视为通过 */
    private boolean keywordFallback(String correctAns, String userAns) {
        String[] keywords = correctAns.split("[|,，、\\s]+");
        int total = 0;
        int hit = 0;
        for (String k : keywords) {
            if (k.trim().isEmpty()) {
                continue;
            }
            total++;
            if (normalizeText(userAns).contains(normalizeText(k))) {
                hit++;
            }
        }
        return total > 0 && hit * 2 >= total;
    }

    /** 由时间窗推导竞赛状态：报名中/进行中/已结束 */
    private String deriveState(Competition c, LocalDateTime now) {
        if (now.isBefore(c.getStartTime())) {
            return "报名中";
        }
        if (!now.isAfter(c.getEndTime())) {
            return "进行中";
        }
        return "已结束";
    }

    /** 昵称脱敏：保留首尾字符，中间打码；单字符原样返回 */
    private String maskNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return "匿名用户";
        }
        nickname = nickname.trim();
        int len = nickname.length();
        if (len == 1) {
            return nickname;
        }
        if (len == 2) {
            return nickname.charAt(0) + "*";
        }
        return nickname.charAt(0) + "*" + nickname.charAt(len - 1);
    }

    /** 是否会员（poti-user internal 接口）；user 服务不可用时降级视为非会员，与 AI 提问限额的降级方向一致 */
    private boolean isVipUser(Long userId) {
        try {
            R<Map<String, Object>> res = userFeignClient.getVipStatus(userId);
            return res != null && res.getData() != null && Boolean.TRUE.equals(res.getData().get("vip"));
        } catch (Exception e) {
            log.warn("查询会员状态失败，按非会员处理: userId={}", userId, e);
            return false;
        }
    }
}
