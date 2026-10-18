package com.poti.practice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.practice.entity.Competition;

import java.util.List;
import java.util.Map;

public interface CompetitionService extends IService<Competition> {

    /** 竞赛分页列表（tab：signup报名中/ongoing进行中/ended已结束，缺省全部） */
    Map<String, Object> pageList(Long userId, Integer pageNum, Integer pageSize, String tab);

    /** 竞赛详情（含我的报名状态与我的成绩） */
    Map<String, Object> detail(Long userId, Long id);

    /** 报名竞赛 */
    void signup(Long userId, Long id);

    /** 开赛：抽题生成题目快照并返回题目列表（不含答案） */
    List<Map<String, Object>> start(Long userId, Long id);

    /** 交卷判分 */
    Map<String, Object> submit(Long userId, Long id, List<Map<String, Object>> answers);

    /** 排行榜前50（成绩脱敏） */
    List<Map<String, Object>> rank(Long id);

    /** 我的竞赛记录（已交卷，含竞赛标题/得分/排名） */
    Map<String, Object> myList(Long userId, Integer pageNum, Integer pageSize);

    /** 试卷回顾：每题的我的作答、参考答案与解析（交卷后可用） */
    List<Map<String, Object>> review(Long userId, Long id);
}
