package com.poti.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.favorite.entity.Favorite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface FavoriteMapper extends BaseMapper<Favorite> {
    
    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND question_id = #{questionId} LIMIT 1")
    Favorite selectByUserAndQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);
    
    @Update("UPDATE favorite SET deleted = 0, category_id = #{categoryId}, title = #{title}, type = #{type}, difficulty = #{difficulty} WHERE user_id = #{userId} AND question_id = #{questionId}")
    int restoreFavorite(@Param("userId") Long userId, @Param("questionId") Long questionId, 
                        @Param("categoryId") Long categoryId, @Param("title") String title, 
                        @Param("type") Integer type, @Param("difficulty") Integer difficulty);
}