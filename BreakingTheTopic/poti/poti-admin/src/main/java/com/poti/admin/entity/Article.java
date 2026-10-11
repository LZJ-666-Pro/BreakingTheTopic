package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体（poti_admin.article）。
 */
@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** 分类：学习路线/面试经验/刷题技巧/面经/技术科普 */
    private String category;

    /** 封面图URL */
    private String cover;

    /** 标签，逗号分隔 */
    private String tags;

    /** 摘要 */
    private String summary;

    /** 正文（Markdown） */
    private String content;

    private String authorName;

    /** 1置顶 */
    private Integer pinned;

    /** 1推荐 */
    private Integer recommended;

    /** 发布状态：0草稿 1已发布 2下架 */
    private Integer status;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    /** 发布时间 */
    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
