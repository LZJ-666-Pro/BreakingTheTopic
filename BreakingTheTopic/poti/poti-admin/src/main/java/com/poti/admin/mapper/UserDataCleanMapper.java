package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户数据重置专用 Mapper。
 * <p>
 * 后台跨库物理删除用户学习数据：practice_record 在 poti_practice 库、
 * wrongbook 在 poti_wrongbook 库、favorite 在 poti_favorite 库，
 * 其余（user_stats / checkin_record / discussion / discussion_like）在 poti_user 库。
 * 重置属于清零场景，统一物理删除而非逻辑删除。
 * <p>
 * 注意：跨库方法必须逐个标注 {@code @DS}，否则会打到主数据源 poti_admin 库上。
 */
@Mapper
public interface UserDataCleanMapper {

    /** 清零用户学习统计（练习记录或签到记录被重置时调用，避免聚合数据与明细不一致） */
    @DS("user")
    @Update("UPDATE user_stats SET total_questions = 0, correct_questions = 0, wrong_questions = 0, " +
            "total_time = 0, today_questions = 0, today_correct = 0, today_time = 0, " +
            "consecutive_days = 0, last_practice_date = NULL, total_points = 0, achievement_points = 0 " +
            "WHERE user_id = #{userId}")
    int resetUserStats(@Param("userId") Long userId);

    /** 删除练习明细记录（poti_practice 库） */
    @DS("practice")
    @Delete("DELETE FROM practice_record WHERE user_id = #{userId}")
    int deletePracticeRecords(@Param("userId") Long userId);

    /** 删除错题本记录（poti_wrongbook 库） */
    @DS("wrongbook")
    @Delete("DELETE FROM wrongbook WHERE user_id = #{userId}")
    int deleteWrongbookRecords(@Param("userId") Long userId);

    /** 删除收藏记录（poti_favorite 库） */
    @DS("favorite")
    @Delete("DELETE FROM favorite WHERE user_id = #{userId}")
    int deleteFavoriteRecords(@Param("userId") Long userId);

    /** 删除签到记录（含连续打卡数据） */
    @DS("user")
    @Delete("DELETE FROM checkin_record WHERE user_id = #{userId}")
    int deleteCheckinRecords(@Param("userId") Long userId);

    /** 删除用户发起的讨论 */
    @DS("user")
    @Delete("DELETE FROM discussion WHERE user_id = #{userId}")
    int deleteDiscussions(@Param("userId") Long userId);

    /** 删除讨论点赞：用户发出的赞 + 用户被删评论收到的赞 */
    @DS("user")
    @Delete("DELETE FROM discussion_like WHERE user_id = #{userId} " +
            "OR discussion_id IN (SELECT id FROM discussion WHERE user_id = #{userId})")
    int deleteDiscussionLikes(@Param("userId") Long userId);
}
