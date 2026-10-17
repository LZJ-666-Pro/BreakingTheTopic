package com.poti.agent.service.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poti.agent.entity.AgentToolLog;
import com.poti.agent.feign.FavoriteFeignClient;
import com.poti.agent.feign.InterviewFeignClient;
import com.poti.agent.feign.PracticeFeignClient;
import com.poti.agent.feign.QuestionFeignClient;
import com.poti.agent.feign.SearchFeignClient;
import com.poti.agent.feign.UserFeignClient;
import com.poti.agent.feign.WrongbookFeignClient;
import com.poti.agent.mapper.AgentToolLogMapper;
import com.poti.common.R;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Agent 工具集：LLM 可调用的业务能力（12 个只读工具，覆盖小程序全部读类业务）。
 *
 * 安全设计（对应 02 规范 4.3）：
 * - 工具方法不暴露 userId 形参，一律从 ThreadLocal 注入当前登录用户，LLM 无法伪造；
 * - 每次执行写 agent_tool_log（内联记录，等价 AOP，便于追溯）；
 * - get_question_detail/get_daily_question 剥离 answer 字段（防作弊），search_questions 剥离答案/解析字段，
 *   收藏/每日一题/专题等接口本身不含答案字段；
 * - 单次回答内调度步数超上限后，工具返回收敛提示，引导模型直接总结。
 */
@Slf4j
@Component
public class AgentTools {

    private final UserFeignClient userFeignClient;
    private final PracticeFeignClient practiceFeignClient;
    private final WrongbookFeignClient wrongbookFeignClient;
    private final QuestionFeignClient questionFeignClient;
    private final SearchFeignClient searchFeignClient;
    private final FavoriteFeignClient favoriteFeignClient;
    private final InterviewFeignClient interviewFeignClient;
    private final AgentToolLogMapper toolLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${agent.limits.max-rounds:5}")
    private int maxRounds;

    /** 当前请求的登录用户（AgentChatService 每次调用前设置，finally 清理） */
    private final ThreadLocal<Long> currentUser = new ThreadLocal<>();
    /** 当前请求内工具执行计数（软性轮数上限） */
    private final ThreadLocal<Integer> roundCounter = ThreadLocal.withInitial(() -> 0);
    private final ThreadLocal<Long> currentSession = new ThreadLocal<>();

    public AgentTools(UserFeignClient userFeignClient,
                      PracticeFeignClient practiceFeignClient,
                      WrongbookFeignClient wrongbookFeignClient,
                      QuestionFeignClient questionFeignClient,
                      SearchFeignClient searchFeignClient,
                      FavoriteFeignClient favoriteFeignClient,
                      InterviewFeignClient interviewFeignClient,
                      AgentToolLogMapper toolLogMapper) {
        this.userFeignClient = userFeignClient;
        this.practiceFeignClient = practiceFeignClient;
        this.wrongbookFeignClient = wrongbookFeignClient;
        this.questionFeignClient = questionFeignClient;
        this.searchFeignClient = searchFeignClient;
        this.favoriteFeignClient = favoriteFeignClient;
        this.interviewFeignClient = interviewFeignClient;
        this.toolLogMapper = toolLogMapper;
    }

    /** 每次助手调用前由 AgentChatService 重置上下文 */
    public void bindContext(Long userId, Long sessionId) {
        currentUser.set(userId);
        currentSession.set(sessionId);
        roundCounter.set(0);
    }

    public void clearContext() {
        currentUser.remove();
        currentSession.remove();
        roundCounter.remove();
    }

    // ==================== 工具 1：学习统计 ====================

    @Tool("查询当前用户的刷题学习统计，包括累计刷题数、答对答错数、正确率、今日刷题数等")
    public String get_study_statistics() {
        return execute("get_study_statistics", () -> {
            R<Map<String, Object>> r = practiceFeignClient.getStatistics(currentUserId());
            return r != null && r.getCode() == 200 && r.getData() != null
                    ? r.getData() : null;
        });
    }

    // ==================== 工具 2：签到状态 ====================

    @Tool("查询当前用户的连续签到天数和今日是否已签到")
    public String get_checkin_status() {
        return execute("get_checkin_status", () -> {
            R<Map<String, Object>> r = userFeignClient.getCheckinStatus(currentUserId());
            return r != null && r.getCode() == 200 && r.getData() != null
                    ? r.getData() : null;
        });
    }

    // ==================== 工具 3：错题概览 ====================

    @Tool("查询当前用户的错题本情况：错题总数、按分类的错题分布、按难度分布、最近错题样例")
    public String get_wrongbook_summary() {
        return execute("get_wrongbook_summary", () -> {
            Long userId = currentUserId();
            Map<String, Object> data = new LinkedHashMap<>();
            R<Integer> countR = wrongbookFeignClient.getWrongCount(userId);
            data.put("wrongCount", countR != null && countR.getCode() == 200 ? countR.getData() : 0);
            R<Map<String, Object>> distR = wrongbookFeignClient.getDistribution(userId);
            if (distR != null && distR.getCode() == 200 && distR.getData() != null) {
                data.putAll(distR.getData());
            }
            return data;
        });
    }

    // ==================== 工具 4：搜题 ====================

    @Tool("按关键字搜索题库中的题目，可按分类ID和难度过滤。返回题目列表（不含答案）。难度：1简单 2中等 3困难")
    public String search_questions(@P("搜索关键字") String keyword,
                                   @P(value = "分类ID，可为空", required = false) Long categoryId,
                                   @P(value = "难度：1简单 2中等 3困难，可为空", required = false) Integer difficulty) {
        return execute("search_questions", () -> {
            R<Map<String, Object>> r = searchFeignClient.searchQuestions(keyword, categoryId, difficulty, 1, 8);
            if (r == null || r.getCode() != 200 || r.getData() == null) {
                return Map.of("list", List.of(), "total", 0);
            }
            // 剥离答案与解析字段，只保留定位信息（防作弊：模型只看到题干与元数据）
            List<Map<String, Object>> safe = new ArrayList<>();
            Object rawList = r.getData().get("list");
            if (rawList instanceof List<?> list) {
                for (Object o : list) {
                    if (o instanceof Map<?, ?> m) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("id", m.get("id"));
                        item.put("title", m.get("title"));
                        item.put("categoryId", m.get("categoryId"));
                        item.put("difficulty", m.get("difficulty"));
                        item.put("type", m.get("type"));
                        safe.add(item);
                    }
                }
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("total", r.getData().get("total"));
            result.put("list", safe);
            return result;
        });
    }

    // ==================== 工具 5：题目详情 ====================

    @Tool("按题目ID查询题目详情：题干、题型、分类、难度、解题思路。不包含标准答案（作答前不可泄露答案）")
    public String get_question_detail(@P("题目ID") Long questionId) {
        return execute("get_question_detail", () -> {
            R<Map<String, Object>> r = questionFeignClient.getQuestion(questionId);
            if (r == null || r.getCode() != 200 || r.getData() == null) {
                return Map.of("error", "题目不存在");
            }
            Map<String, Object> data = new LinkedHashMap<>(r.getData());
            data.remove("answer");
            return data;
        });
    }

    // ==================== 工具 6：分类列表 ====================

    @Tool("查询题库所有题库分组与分类（分类ID、分类名、所属分组、题目数量），用于回答有哪些可刷的分类或推荐刷题方向")
    public String get_category_list() {
        return execute("get_category_list", () -> {
            R<Map<String, Object>> r = questionFeignClient.getCategoryGroups();
            if (r == null || r.getCode() != 200 || r.getData() == null) {
                return Map.of("categories", List.of());
            }
            // 精简字段并按题量取前 60 个分类，控制注入上下文的长度
            List<Map<String, Object>> slim = new ArrayList<>();
            Object rawCategories = r.getData().get("categories");
            if (rawCategories instanceof List<?> list) {
                List<Map<?, ?>> maps = new ArrayList<>();
                for (Object o : list) {
                    if (o instanceof Map<?, ?> m && questionCount(m) > 0) {
                        maps.add(m);
                    }
                }
                maps.sort((a, b) -> Long.compare(questionCount(b), questionCount(a)));
                maps.stream().limit(60).forEach(m -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", m.get("id"));
                    item.put("name", m.get("name"));
                    item.put("groupKeys", m.get("groupKeys"));
                    item.put("questionCount", m.get("questionCount"));
                    slim.add(item);
                });
            }
            return Map.of("categories", slim, "note", "按题量降序展示前60个分类");
        });
    }

    private long questionCount(Map<?, ?> m) {
        Object v = m.get("questionCount");
        return v instanceof Number n ? n.longValue() : 0L;
    }

    // ==================== 工具 7：每日一题 ====================

    @Tool("查询今天的每日一题：日期、题干、题型分类、难度、出题理由、提示。不包含标准答案（作答前不可泄露答案）")
    public String get_daily_question() {
        return execute("get_daily_question", () -> {
            R<Map<String, Object>> r = questionFeignClient.getDailyQuestion();
            if (r == null || r.getCode() != 200 || r.getData() == null) {
                return Map.of("error", "今日暂无每日一题");
            }
            Map<String, Object> data = new LinkedHashMap<>(r.getData());
            data.remove("answer");
            return data;
        });
    }

    // ==================== 工具 8：刷题专题列表 ====================

    @Tool("查询刷题专题列表（专题ID、名称、简介、难度、预计天数、题目数量），用于回答有哪些专题可刷或推荐学习计划")
    public String get_special_list() {
        return execute("get_special_list", () -> {
            R<List<Map<String, Object>>> r = questionFeignClient.getSpecialList();
            if (r == null || r.getCode() != 200 || r.getData() == null) {
                return Map.of("specials", List.of());
            }
            List<Map<String, Object>> slim = new ArrayList<>();
            for (Map<String, Object> m : r.getData()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", m.get("id"));
                item.put("name", m.get("name"));
                item.put("description", m.get("description"));
                item.put("difficulty", m.get("difficulty"));
                item.put("estimatedDays", m.get("estimatedDays"));
                item.put("questionCount", m.get("questionCount"));
                slim.add(item);
            }
            return Map.of("specials", slim);
        });
    }

    // ==================== 工具 9：热搜词 ====================

    @Tool("查询当前热搜的搜索关键词列表，用于回答大家都在搜什么、帮助用户发现热门题目方向")
    public String get_hot_searches() {
        return execute("get_hot_searches", () -> {
            R<Set<Object>> r = searchFeignClient.getHotSearches(10);
            if (r == null || r.getCode() != 200 || r.getData() == null) {
                return Map.of("hotSearches", List.of());
            }
            return Map.of("hotSearches", new ArrayList<>(r.getData()));
        });
    }

    // ==================== 工具 10：收藏列表 ====================

    @Tool("查询当前用户收藏的题目列表（题目ID、题干、分类、难度、题型），用于回答用户收藏了哪些题")
    public String get_favorite_list() {
        return execute("get_favorite_list", () -> {
            R<List<Map<String, Object>>> r = favoriteFeignClient.getFavoriteList(currentUserId(), 1, 50);
            if (r == null || r.getCode() != 200 || r.getData() == null) {
                return Map.of("favorites", List.of(), "total", 0);
            }
            List<Map<String, Object>> slim = new ArrayList<>();
            for (Map<String, Object> m : r.getData()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("questionId", m.get("questionId"));
                item.put("title", m.get("title"));
                item.put("categoryId", m.get("categoryId"));
                item.put("type", m.get("type"));
                item.put("difficulty", m.get("difficulty"));
                slim.add(item);
            }
            return Map.of("total", slim.size(), "favorites", slim);
        });
    }

    // ==================== 工具 11：面试特训营列表 ====================

    @Tool("查询面试特训营列表（营名、方向、周期、人数、当前用户是否已加入），用于推荐特训营或回答用户打卡情况")
    public String get_interview_camps() {
        return execute("get_interview_camps", () -> {
            R<List<Map<String, Object>>> r = interviewFeignClient.getCampList(currentUserId());
            if (r == null || r.getCode() != 200 || r.getData() == null) {
                return Map.of("camps", List.of());
            }
            return Map.of("camps", r.getData());
        });
    }

    // ==================== 工具 12：好友刷题排行 ====================

    @Tool("查询当前用户与好友的刷题排行榜（名次、昵称、刷题量或连续天数），用于回答好友排行、自己排第几")
    public String get_friend_ranking(@P(value = "排行类型：total按累计刷题量（默认），streak按连续刷题天数", required = false) String type) {
        String rankType = (type == null || type.isBlank()) ? "total" : type.trim();
        return execute("get_friend_ranking", () -> {
            R<Map<String, Object>> r = userFeignClient.getFriendRanking(currentUserId(), rankType);
            if (r == null || r.getCode() != 200 || r.getData() == null) {
                return Map.of("rankList", List.of());
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("rankType", rankType);
            // 精简排行条目：去掉头像 URL（对 LLM 无用且占上下文）
            for (String key : List.of("topThree", "rankList", "myRank")) {
                Object v = r.getData().get(key);
                if (v instanceof List<?> list) {
                    data.put(key, list.stream().map(this::slimRankItem).collect(java.util.stream.Collectors.toList()));
                } else if (v instanceof Map<?, ?>) {
                    data.put(key, slimRankItem(v));
                }
            }
            return data;
        });
    }

    private Map<String, Object> slimRankItem(Object o) {
        Map<?, ?> m = (Map<?, ?>) o;
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("rank", m.get("rank"));
        item.put("nickname", m.get("nickname"));
        item.put("score", m.get("score"));
        return item;
    }

    // ==================== 执行骨架 ====================

    private interface ToolCall {
        Object run() throws Exception;
    }

    private String execute(String toolName, ToolCall call) {
        long start = System.currentTimeMillis();
        int round = roundCounter.get() + 1;
        roundCounter.set(round);
        boolean success = true;
        String result;
        try {
            if (round > maxRounds) {
                success = false;
                result = "{\"ok\":false,\"error\":\"ROUND_LIMIT\",\"hint\":\"已达到单次回答的调度步数上限，请基于已获取的信息直接总结回答\"}";
            } else {
                Object data = call.run();
                result = objectMapper.writeValueAsString(Map.of("ok", true, "data", data == null ? Map.of() : data));
            }
        } catch (Exception e) {
            success = false;
            log.error("[agent] 工具执行失败: {}", toolName, e);
            try {
                result = objectMapper.writeValueAsString(Map.of("ok", false, "error", "TOOL_ERROR", "message", "业务服务暂不可用，请如实告知用户稍后再试"));
            } catch (Exception ignored) {
                result = "{\"ok\":false,\"error\":\"TOOL_ERROR\"}";
            }
        }
        long cost = System.currentTimeMillis() - start;
        try {
            AgentToolLog toolLog = new AgentToolLog();
            toolLog.setUserId(currentUser.get());
            toolLog.setSessionId(currentSession.get());
            toolLog.setToolName(toolName);
            toolLog.setCostMs((int) cost);
            toolLog.setRoundNo(round);
            toolLog.setSuccess(success ? 1 : 0);
            toolLog.setCreateTime(LocalDateTime.now());
            toolLogMapper.insert(toolLog);
        } catch (Exception e) {
            log.warn("[agent] 工具日志写入失败", e);
        }
        return result;
    }

    private Long currentUserId() {
        Long userId = currentUser.get();
        if (userId == null) {
            throw new IllegalStateException("工具上下文未绑定用户");
        }
        return userId;
    }
}
