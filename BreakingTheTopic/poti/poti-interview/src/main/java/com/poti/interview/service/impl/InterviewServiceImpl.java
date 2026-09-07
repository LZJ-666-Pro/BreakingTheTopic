package com.poti.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.common.R;
import com.poti.interview.entity.InterviewQuestionDetail;
import com.poti.interview.entity.InterviewRecord;
import com.poti.interview.feign.QuestionFeignClient;
import com.poti.interview.mapper.InterviewQuestionDetailMapper;
import com.poti.interview.mapper.InterviewRecordMapper;
import com.poti.interview.service.InterviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewRecordMapper, InterviewRecord> implements InterviewService {

    @Autowired
    private InterviewRecordMapper interviewRecordMapper;

    @Autowired
    private InterviewQuestionDetailMapper questionDetailMapper;

    @Autowired
    private QuestionFeignClient questionFeignClient;

    private static final Map<String, String> DEFAULT_COLORS = new HashMap<>();
    static {
        DEFAULT_COLORS.put("java", "#FF9500");
        DEFAULT_COLORS.put("python", "#07C160");
        DEFAULT_COLORS.put("mysql", "#10AEFF");
        DEFAULT_COLORS.put("redis", "#FA5151");
        DEFAULT_COLORS.put("spring", "#6467EF");
        DEFAULT_COLORS.put("mq", "#FF8800");
    }

    @Override
    public List<Map<String, Object>> getInterviewTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        
        try {
            R<List<Map<String, Object>>> response = questionFeignClient.getCategoryList();
            if (response != null && response.getData() != null) {
                List<Map<String, Object>> categories = response.getData();
                String[] colors = {"#FF9500", "#07C160", "#10AEFF", "#FA5151", "#6467EF", "#FF8800", "#576b95", "#6190e8"};
                String[] emojis = {"☕", "🐍", "🗄️", "🔴", "🍃", "📨", "🔷", "⚡"};
                int colorIndex = 0;
                
                for (Map<String, Object> category : categories) {
                    Long parentId = category.get("parentId") != null ? 
                        Long.valueOf(category.get("parentId").toString()) : null;
                    
                    if (parentId != null && parentId > 0) {
                        continue;
                    }
                    
                    Map<String, Object> type = new HashMap<>();
                    Long categoryId = Long.valueOf(category.get("id").toString());
                    String name = (String) category.get("name");
                    
                    type.put("type", "cat_" + categoryId);
                    type.put("categoryId", categoryId);
                    type.put("title", name + "面试");
                    type.put("desc", name + "知识");
                    type.put("color", colors[colorIndex % colors.length]);
                    type.put("emoji", emojis[colorIndex % emojis.length]);
                    types.add(type);
                    colorIndex++;
                }
            }
        } catch (Exception e) {
            log.error("获取面试类型失败", e);
        }
        
        if (types.isEmpty()) {
            types.add(createDefaultType("cat_1", 1L, "Java面试", "Java基础与进阶", "#FF9500", "☕"));
            types.add(createDefaultType("cat_2", 2L, "Python面试", "Python开发技能", "#07C160", "🐍"));
            types.add(createDefaultType("cat_3", 3L, "MySQL面试", "数据库知识", "#10AEFF", "🗄️"));
        }
        
        return types;
    }
    
    private Map<String, Object> createDefaultType(String type, Long categoryId, String title, String desc, String color, String emoji) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", type);
        result.put("categoryId", categoryId);
        result.put("title", title);
        result.put("desc", desc);
        result.put("color", color);
        result.put("emoji", emoji);
        
        return result;
    }

    private String getCategoryName(String type) {
        if (type == null || !type.startsWith("cat_")) {
            return type;
        }
        
        try {
            Long categoryId = Long.valueOf(type.substring(4));
            R<List<Map<String, Object>>> response = questionFeignClient.getCategoryList();
            if (response != null && response.getData() != null) {
                for (Map<String, Object> category : response.getData()) {
                    Long id = Long.valueOf(category.get("id").toString());
                    if (id.equals(categoryId)) {
                        return (String) category.get("name");
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取分类名称失败", e);
        }
        
        return type;
    }

    @Override
    @Transactional
    public Map<String, Object> startInterview(Long userId, String type) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<InterviewRecord> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(InterviewRecord::getUserId, userId)
                       .eq(InterviewRecord::getStatus, 1)
                       .orderByDesc(InterviewRecord::getCreateTime)
                       .last("LIMIT 1");
        InterviewRecord existingRecord = interviewRecordMapper.selectOne(existingWrapper);
        
        if (existingRecord != null) {
            if (existingRecord.getType().equals(type)) {
                log.info("用户 {} 已有同类型进行中的面试 {}", userId, existingRecord.getId());
                LambdaQueryWrapper<InterviewQuestionDetail> detailWrapper = new LambdaQueryWrapper<>();
                detailWrapper.eq(InterviewQuestionDetail::getInterviewId, existingRecord.getId())
                            .orderByAsc(InterviewQuestionDetail::getOrderNum);
                List<InterviewQuestionDetail> details = questionDetailMapper.selectList(detailWrapper);
                
                List<Map<String, Object>> formattedQuestions = new ArrayList<>();
                for (InterviewQuestionDetail d : details) {
                    Map<String, Object> formattedQ = new HashMap<>();
                    formattedQ.put("questionId", d.getQuestionId());
                    formattedQ.put("content", d.getQuestionContent());
                    formattedQ.put("orderNum", d.getOrderNum());
                    formattedQuestions.add(formattedQ);
                }
                
                result.put("interviewId", existingRecord.getId());
                result.put("questions", formattedQuestions);
                result.put("totalQuestions", existingRecord.getTotalQuestions());
                return result;
            } else {
                log.info("用户 {} 有不同类型的进行中面试，自动结束旧面试 {}", userId, existingRecord.getId());
                existingRecord.setStatus(2);
                existingRecord.setEndTime(LocalDateTime.now());
                existingRecord.setAiFeedback("用户切换了面试类型，自动结束");
                interviewRecordMapper.updateById(existingRecord);
            }
        }
        
        log.info("为用户 {} 创建新的面试，类型: {}", userId, type);
        
        Long categoryId = null;
        if (type.startsWith("cat_")) {
            try {
                categoryId = Long.valueOf(type.substring(4));
                log.info("解析分类ID: {}", categoryId);
            } catch (NumberFormatException e) {
                log.warn("无法解析分类ID: {}", type);
            }
        }
        
        InterviewRecord record = new InterviewRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setTitle(getCategoryName(type) + "模拟面试");
        record.setStartTime(LocalDateTime.now());
        record.setStatus(1);
        record.setTotalQuestions(0);
        record.setCorrectCount(0);
        record.setSpendSeconds(0);
        record.setScore(BigDecimal.ZERO);
        interviewRecordMapper.insert(record);
        
        result.put("interviewId", record.getId());
        
        try {
            R<List<Map<String, Object>>> response;
            if (categoryId != null) {
                log.info("调用getRandomQuestionsByCategory接口，categoryId: {}", categoryId);
                response = questionFeignClient.getRandomQuestionsByCategory(categoryId, 10);
            } else {
                log.info("调用getRandomQuestions接口，type: {}", type);
                response = questionFeignClient.getRandomQuestions(type, 10);
            }
            
            log.info("获取题目响应: {}", response);
            
            if (response != null && response.getData() != null) {
                List<Map<String, Object>> questions = response.getData();
                List<Map<String, Object>> formattedQuestions = new ArrayList<>();
                
                int orderNum = 1;
                for (Map<String, Object> q : questions) {
                    Map<String, Object> formattedQ = new HashMap<>();
                    formattedQ.put("questionId", q.get("id"));
                    formattedQ.put("content", q.get("content"));
                    formattedQ.put("orderNum", orderNum);
                    formattedQuestions.add(formattedQ);
                    
                    InterviewQuestionDetail detail = new InterviewQuestionDetail();
                    detail.setInterviewId(record.getId());
                    detail.setQuestionId(Long.valueOf(q.get("id").toString()));
                    detail.setQuestionContent((String) q.get("content"));
                    detail.setOptions((String) q.get("options"));
                    detail.setReferenceAnswer((String) q.get("analysis"));
                    detail.setCorrectAnswer((String) q.get("answer"));
                    detail.setOrderNum(orderNum++);
                    detail.setIsCorrect(0);
                    detail.setScore(BigDecimal.ZERO);
                    questionDetailMapper.insert(detail);
                }
                
                result.put("questions", formattedQuestions);
                result.put("totalQuestions", questions.size());
                
                record.setTotalQuestions(questions.size());
                interviewRecordMapper.updateById(record);
            }
        } catch (Exception e) {
            log.error("获取题目失败", e);
            result.put("questions", Collections.emptyList());
            result.put("totalQuestions", 0);
        }
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> submitAnswer(Long userId, Long interviewId, Long questionId, 
                                             String userAnswer, String audioUrl, Integer answerTimeSeconds) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<InterviewRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(InterviewRecord::getId, interviewId)
                    .eq(InterviewRecord::getUserId, userId)
                    .eq(InterviewRecord::getStatus, 1);
        InterviewRecord record = interviewRecordMapper.selectOne(recordWrapper);
        
        if (record == null) {
            result.put("success", false);
            result.put("message", "面试记录不存在或已结束");
            return result;
        }
        
        LambdaQueryWrapper<InterviewQuestionDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(InterviewQuestionDetail::getInterviewId, interviewId)
                    .eq(InterviewQuestionDetail::getQuestionId, questionId);
        InterviewQuestionDetail detail = questionDetailMapper.selectOne(detailWrapper);
        
        if (detail == null) {
            result.put("success", false);
            result.put("message", "题目不存在");
            return result;
        }
        
        detail.setUserAnswer(userAnswer);
        detail.setAudioUrl(audioUrl);
        detail.setAnswerTimeSeconds(answerTimeSeconds);
        
        Map<String, Object> aiResult = evaluateAnswer(detail.getQuestionContent(), 
                                                       detail.getReferenceAnswer(),
                                                       detail.getCorrectAnswer(),
                                                       userAnswer);
        detail.setScore(new BigDecimal(aiResult.get("score").toString()));
        detail.setAiComment((String) aiResult.get("comment"));
        detail.setIsCorrect((Integer) aiResult.get("isCorrect") >= 6 ? 1 : 0);
        
        questionDetailMapper.updateById(detail);
        
        result.put("success", true);
        result.put("score", detail.getScore());
        result.put("aiComment", detail.getAiComment());
        result.put("isCorrect", detail.getIsCorrect());
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> finishInterview(Long userId, Long interviewId, Integer spendSeconds) {
        Map<String, Object> result = new HashMap<>();
        
        log.info("用户 {} 完成面试 {}", userId, interviewId);
        
        LambdaQueryWrapper<InterviewRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(InterviewRecord::getId, interviewId)
                    .eq(InterviewRecord::getUserId, userId)
                    .eq(InterviewRecord::getStatus, 1);
        InterviewRecord record = interviewRecordMapper.selectOne(recordWrapper);
        
        if (record == null) {
            log.warn("面试记录不存在或已结束，interviewId: {}, userId: {}", interviewId, userId);
            result.put("success", false);
            result.put("message", "面试记录不存在或已结束");
            return result;
        }
        
        LambdaQueryWrapper<InterviewQuestionDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(InterviewQuestionDetail::getInterviewId, interviewId)
                    .orderByAsc(InterviewQuestionDetail::getOrderNum);
        List<InterviewQuestionDetail> details = questionDetailMapper.selectList(detailWrapper);
        
        int correctCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;
        StringBuilder feedback = new StringBuilder();
        
        for (InterviewQuestionDetail d : details) {
            if (d.getIsCorrect() != null && d.getIsCorrect() == 1) {
                correctCount++;
            }
            if (d.getScore() != null) {
                totalScore = totalScore.add(d.getScore());
            }
        }
        
        if (!details.isEmpty()) {
            BigDecimal avgScore = totalScore.divide(new BigDecimal(details.size()), 2, RoundingMode.HALF_UP);
            record.setScore(avgScore);
            
            feedback.append("本次面试共").append(details.size()).append("题，");
            feedback.append("答对").append(correctCount).append("题。");
            
            if (avgScore.compareTo(new BigDecimal("80")) >= 0) {
                feedback.append("表现优秀，基础知识扎实，继续保持！");
            } else if (avgScore.compareTo(new BigDecimal("60")) >= 0) {
                feedback.append("表现良好，部分知识点需要加强。");
            } else {
                feedback.append("需要加强基础知识学习，建议多做练习。");
            }
        }
        
        record.setCorrectCount(correctCount);
        record.setSpendSeconds(spendSeconds);
        record.setEndTime(LocalDateTime.now());
        record.setStatus(2);
        record.setAiFeedback(feedback.toString());
        interviewRecordMapper.updateById(record);
        
        result.put("success", true);
        result.put("totalQuestions", record.getTotalQuestions());
        result.put("correctCount", correctCount);
        result.put("score", record.getScore());
        result.put("aiFeedback", record.getAiFeedback());
        result.put("spendSeconds", spendSeconds);
        result.put("details", details);
        
        return result;
    }

    @Override
    public Map<String, Object> submitInterview(Long userId, String type, List<Map<String, Object>> answers, Integer spendSeconds) {
        Map<String, Object> result = new HashMap<>();
        
        int correctCount = 0;
        for (Map<String, Object> answer : answers) {
            Boolean isCorrect = (Boolean) answer.get("isCorrect");
            if (Boolean.TRUE.equals(isCorrect)) {
                correctCount++;
            }
        }
        
        InterviewRecord record = new InterviewRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setTitle(getCategoryName(type) + "模拟面试");
        record.setStartTime(LocalDateTime.now().minusSeconds(spendSeconds));
        record.setEndTime(LocalDateTime.now());
        record.setTotalQuestions(answers.size());
        record.setCorrectCount(correctCount);
        record.setSpendSeconds(spendSeconds);
        record.setScore(new BigDecimal(answers.size() > 0 ? (correctCount * 100 / answers.size()) : 0));
        record.setStatus(2);
        interviewRecordMapper.insert(record);
        
        result.put("totalQuestions", answers.size());
        result.put("correctCount", correctCount);
        result.put("score", record.getScore());
        
        return result;
    }

    @Override
    public Map<String, Object> getHistory(Long userId, Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<InterviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewRecord::getUserId, userId)
               .eq(InterviewRecord::getStatus, 2)
               .orderByDesc(InterviewRecord::getCreateTime);
        
        Page<InterviewRecord> page = new Page<>(pageNum, pageSize);
        Page<InterviewRecord> resultPage = interviewRecordMapper.selectPage(page, wrapper);
        
        log.info("用户 {} 查询面试历史，共 {} 条记录", userId, resultPage.getTotal());
        
        result.put("total", resultPage.getTotal());
        result.put("list", resultPage.getRecords());
        
        return result;
    }

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();
        
        LambdaQueryWrapper<InterviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewRecord::getUserId, userId)
               .eq(InterviewRecord::getStatus, 2);
        
        List<InterviewRecord> records = interviewRecordMapper.selectList(wrapper);
        
        int totalInterviews = records.size();
        int totalQuestions = records.stream().mapToInt(InterviewRecord::getTotalQuestions).sum();
        int totalCorrect = records.stream().mapToInt(InterviewRecord::getCorrectCount).sum();
        
        statistics.put("totalInterviews", totalInterviews);
        statistics.put("totalQuestions", totalQuestions);
        statistics.put("totalCorrect", totalCorrect);
        statistics.put("accuracy", totalQuestions > 0 ? (totalCorrect * 100 / totalQuestions) : 0);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getInterviewDetail(Long userId, Long interviewId) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<InterviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewRecord::getUserId, userId)
               .eq(InterviewRecord::getId, interviewId);
        
        InterviewRecord record = interviewRecordMapper.selectOne(wrapper);
        
        if (record == null) {
            result.put("found", false);
            return result;
        }
        
        result.put("id", record.getId());
        result.put("title", record.getTitle());
        result.put("type", record.getType());
        result.put("startTime", record.getStartTime());
        result.put("endTime", record.getEndTime());
        result.put("totalQuestions", record.getTotalQuestions());
        result.put("correctCount", record.getCorrectCount());
        result.put("spendSeconds", record.getSpendSeconds());
        result.put("score", record.getScore());
        result.put("status", record.getStatus());
        result.put("aiFeedback", record.getAiFeedback());
        result.put("found", true);
        
        LambdaQueryWrapper<InterviewQuestionDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(InterviewQuestionDetail::getInterviewId, interviewId)
                    .orderByAsc(InterviewQuestionDetail::getOrderNum);
        List<InterviewQuestionDetail> details = questionDetailMapper.selectList(detailWrapper);
        result.put("details", details);
        
        return result;
    }

    @Override
    public Map<String, Object> getCurrentQuestion(Long userId, Long interviewId, Integer orderNum) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<InterviewQuestionDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewQuestionDetail::getInterviewId, interviewId)
               .eq(InterviewQuestionDetail::getOrderNum, orderNum);
        
        InterviewQuestionDetail detail = questionDetailMapper.selectOne(wrapper);
        
        if (detail == null) {
            result.put("found", false);
            return result;
        }
        
        result.put("id", detail.getId());
        result.put("questionId", detail.getQuestionId());
        result.put("content", detail.getQuestionContent());
        result.put("orderNum", detail.getOrderNum());
        result.put("found", true);
        
        return result;
    }
    
    private Map<String, Object> evaluateAnswer(String question, String referenceAnswer, String correctAnswer, String userAnswer) {
        Map<String, Object> result = new HashMap<>();
        
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            result.put("score", 0);
            result.put("comment", "未作答");
            result.put("isCorrect", 0);
            return result;
        }
        
        int score = 5;
        String comment = "";
        
        if (correctAnswer != null && !correctAnswer.trim().isEmpty()) {
            String normalizedUserAnswer = userAnswer.trim().toUpperCase();
            String normalizedCorrectAnswer = correctAnswer.trim().toUpperCase();
            
            if (normalizedUserAnswer.equals(normalizedCorrectAnswer)) {
                score = 10;
                comment = "回答正确！";
            } else {
                score = 0;
                comment = "回答错误，正确答案是：" + correctAnswer;
            }
        } else if (referenceAnswer != null && !referenceAnswer.isEmpty()) {
            String[] keywords = referenceAnswer.split("[，。、；：,.;:\\s]+");
            int matchedKeywords = 0;
            
            for (String keyword : keywords) {
                if (keyword.length() >= 2 && userAnswer.contains(keyword)) {
                    matchedKeywords++;
                }
            }
            
            if (keywords.length > 0) {
                double matchRate = (double) matchedKeywords / keywords.length;
                if (matchRate >= 0.7) {
                    score = 9;
                    comment = "回答准确，覆盖了大部分关键知识点。";
                } else if (matchRate >= 0.5) {
                    score = 7;
                    comment = "回答较好，但部分知识点未覆盖。";
                } else if (matchRate >= 0.3) {
                    score = 5;
                    comment = "回答一般，建议加强对该知识点的理解。";
                } else {
                    score = 3;
                    comment = "回答不够准确，需要重新学习相关内容。";
                }
            }
        } else {
            if (userAnswer.length() >= 50) {
                score = 7;
                comment = "回答较为完整，表述清晰。";
            } else if (userAnswer.length() >= 20) {
                score = 5;
                comment = "回答基本完整，可以更详细一些。";
            } else {
                score = 3;
                comment = "回答过于简短，建议补充更多细节。";
            }
        }
        
        result.put("score", score);
        result.put("comment", comment);
        result.put("isCorrect", score >= 6 ? 1 : 0);
        
        return result;
    }

    @Override
    public boolean deleteInterview(Long userId, Long interviewId) {
        LambdaQueryWrapper<InterviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewRecord::getId, interviewId)
               .eq(InterviewRecord::getUserId, userId);
        
        return interviewRecordMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean clearAllInterviews(Long userId) {
        LambdaQueryWrapper<InterviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewRecord::getUserId, userId)
               .eq(InterviewRecord::getStatus, 2);
        
        return interviewRecordMapper.delete(wrapper) > 0;
    }

    @Override
    public List<Map<String, Object>> getWrongQuestions(Long userId) {
        List<Map<String, Object>> wrongQuestions = new ArrayList<>();
        
        LambdaQueryWrapper<InterviewRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(InterviewRecord::getUserId, userId)
                    .eq(InterviewRecord::getStatus, 2)
                    .orderByDesc(InterviewRecord::getCreateTime);
        List<InterviewRecord> records = interviewRecordMapper.selectList(recordWrapper);
        
        for (InterviewRecord record : records) {
            LambdaQueryWrapper<InterviewQuestionDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(InterviewQuestionDetail::getInterviewId, record.getId())
                        .eq(InterviewQuestionDetail::getIsCorrect, 0);
            
            List<InterviewQuestionDetail> wrongDetails = questionDetailMapper.selectList(detailWrapper);
            
            for (InterviewQuestionDetail detail : wrongDetails) {
                Map<String, Object> wrongQuestion = new HashMap<>();
                wrongQuestion.put("id", detail.getId());
                wrongQuestion.put("questionId", detail.getQuestionId());
                wrongQuestion.put("title", detail.getQuestionContent());
                wrongQuestion.put("options", detail.getOptions());
                wrongQuestion.put("userAnswer", detail.getUserAnswer());
                wrongQuestion.put("correctAnswer", detail.getCorrectAnswer());
                wrongQuestion.put("referenceAnswer", detail.getReferenceAnswer());
                wrongQuestion.put("aiComment", detail.getAiComment());
                wrongQuestion.put("interviewId", record.getId());
                wrongQuestion.put("interviewTitle", record.getTitle());
                wrongQuestion.put("interviewType", record.getType());
                wrongQuestion.put("createTime", detail.getCreateTime());
                wrongQuestions.add(wrongQuestion);
            }
        }
        
        return wrongQuestions;
    }

    @Override
    public boolean removeWrongQuestion(Long userId, Long detailId) {
        LambdaQueryWrapper<InterviewQuestionDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(InterviewQuestionDetail::getId, detailId);
        
        InterviewQuestionDetail detail = questionDetailMapper.selectOne(detailWrapper);
        if (detail == null) {
            return false;
        }
        
        LambdaQueryWrapper<InterviewRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(InterviewRecord::getId, detail.getInterviewId())
                    .eq(InterviewRecord::getUserId, userId);
        InterviewRecord record = interviewRecordMapper.selectOne(recordWrapper);
        
        if (record == null) {
            return false;
        }
        
        return questionDetailMapper.deleteById(detailId) > 0;
    }

    @Override
    @Transactional
    public boolean clearAllWrongQuestions(Long userId) {
        LambdaQueryWrapper<InterviewRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(InterviewRecord::getUserId, userId)
                    .eq(InterviewRecord::getStatus, 2);
        List<InterviewRecord> records = interviewRecordMapper.selectList(recordWrapper);
        
        for (InterviewRecord record : records) {
            LambdaQueryWrapper<InterviewQuestionDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(InterviewQuestionDetail::getInterviewId, record.getId())
                        .eq(InterviewQuestionDetail::getIsCorrect, 0);
            questionDetailMapper.delete(detailWrapper);
        }
        
        return true;
    }
}
