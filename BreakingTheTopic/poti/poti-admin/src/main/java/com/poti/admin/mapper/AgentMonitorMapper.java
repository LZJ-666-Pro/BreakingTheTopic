package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * AI 助手监控 Mapper（poti_agent 库）——Agent 使用情况统计与会话/工具日志查询。
 * 只读监控，不写任何 Agent 数据。
 */
@Mapper
@DS("agent")
public interface AgentMonitorMapper {

    // ==================== 概览统计 ====================

    @Select("SELECT COUNT(*) FROM agent_session WHERE create_time >= CURDATE()")
    Long countTodaySessions();

    @Select("SELECT COUNT(*) FROM agent_message WHERE role = 'user' AND create_time >= CURDATE()")
    Long countTodayQuestions();

    @Select("SELECT COUNT(*) FROM agent_tool_log WHERE create_time >= CURDATE()")
    Long countTodayToolCalls();

    @Select("SELECT COUNT(DISTINCT user_id) FROM agent_session WHERE update_time >= CURDATE()")
    Long countTodayActiveUsers();

    @Select("SELECT COUNT(*) FROM agent_tool_log WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    Long countToolCalls7d();

    @Select("SELECT COUNT(*) FROM agent_tool_log WHERE success = 1 AND create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    Long countToolSuccess7d();

    @Select("SELECT IFNULL(AVG(cost_ms), 0) FROM agent_tool_log WHERE success = 1 AND create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    Double avgToolCost7d();

    @Select("SELECT COUNT(*) FROM agent_session")
    Long countTotalSessions();

    @Select("SELECT COUNT(*) FROM agent_message")
    Long countTotalMessages();

    @Select("SELECT COUNT(DISTINCT user_id) FROM agent_session")
    Long countTotalUsers();

    // ==================== 会话列表 ====================

    @Select("<script>" +
            "SELECT s.id, s.user_id AS userId, s.title, " +
            "       s.create_time AS createTime, s.update_time AS updateTime, " +
            "       IFNULL(m.cnt, 0) AS messageCount " +
            "FROM agent_session s " +
            "LEFT JOIN (SELECT session_id, COUNT(*) cnt FROM agent_message GROUP BY session_id) m ON m.session_id = s.id " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'> s.title LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "</where>" +
            "ORDER BY s.update_time DESC" +
            "</script>")
    IPage<Map<String, Object>> selectSessions(Page<Map<String, Object>> page, @Param("keyword") String keyword);

    // ==================== 会话消息 ====================

    @Select("SELECT id, role, content, create_time AS createTime FROM agent_message " +
            "WHERE session_id = #{sessionId} ORDER BY id ASC")
    List<Map<String, Object>> selectMessages(@Param("sessionId") Long sessionId);

    // ==================== 工具调用日志 ====================

    @Select("<script>" +
            "SELECT id, user_id AS userId, session_id AS sessionId, tool_name AS toolName, " +
            "       cost_ms AS costMs, round_no AS roundNo, success, create_time AS createTime " +
            "FROM agent_tool_log " +
            "<where>" +
            "  <if test='toolName != null and toolName != \"\"'> tool_name LIKE CONCAT('%', #{toolName}, '%') </if>" +
            "  <if test='success != null'> AND success = #{success} </if>" +
            "</where>" +
            "ORDER BY id DESC" +
            "</script>")
    IPage<Map<String, Object>> selectToolLogs(Page<Map<String, Object>> page,
                                              @Param("toolName") String toolName,
                                              @Param("success") Integer success);
}
