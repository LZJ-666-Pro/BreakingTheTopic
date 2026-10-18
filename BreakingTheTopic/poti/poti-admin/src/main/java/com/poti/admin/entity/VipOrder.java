package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员订单实体（poti_user.vip_order）。
 * 当前为模拟支付（simulated=1，下单即成功），接入真实支付后替换回调逻辑。
 */
@Data
@TableName("vip_order")
public class VipOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 订单号 */
    private String orderNo;

    /** 支付金额（元） */
    private BigDecimal amount;

    /** 时长（月） */
    private Integer months;

    /** 支付状态 1成功 */
    private Integer payStatus;

    /** 是否模拟支付 1是 */
    private Integer simulated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
}
