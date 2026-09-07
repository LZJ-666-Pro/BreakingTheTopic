package com.poti.interview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.interview.entity.InterviewRecord;

import java.util.List;
import java.util.Map;

public interface InterviewService extends IService<InterviewRecord> {

    List<Map<String, Object>> getInterviewTypes();

    Map<String, Object> startInterview(Long userId, String type);

    Map<String, Object> submitAnswer(Long userId, Long interviewId, Long questionId, 
                                      String userAnswer, String audioUrl, Integer answerTimeSeconds);

    Map<String, Object> finishInterview(Long userId, Long interviewId, Integer spendSeconds);

    Map<String, Object> submitInterview(Long userId, String type, List<Map<String, Object>> answers, Integer spendSeconds);

    Map<String, Object> getHistory(Long userId, Integer pageNum, Integer pageSize);

    Map<String, Object> getStatistics(Long userId);

    Map<String, Object> getInterviewDetail(Long userId, Long interviewId);

    Map<String, Object> getCurrentQuestion(Long userId, Long interviewId, Integer orderNum);

    boolean deleteInterview(Long userId, Long interviewId);

    boolean clearAllInterviews(Long userId);

    List<Map<String, Object>> getWrongQuestions(Long userId);

    boolean removeWrongQuestion(Long userId, Long detailId);

    boolean clearAllWrongQuestions(Long userId);
}
