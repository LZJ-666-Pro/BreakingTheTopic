package com.poti.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专题题目关联（poti_question.special_question）。
 */
@Data
@TableName("special_question")
public class SpecialQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long topicId;

    private Long questionId;

    /** 题目顺序，越小越靠前 */
    private Integer sort;

    private LocalDateTime createTime;
}
