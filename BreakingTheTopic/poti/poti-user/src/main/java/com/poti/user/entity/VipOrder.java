package com.poti.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员订单（模拟支付 simulated=1 点击即成功；
 * 真实微信支付时 pay_status 由回调/查单置 1，simulated=0 并记录微信 transaction_id）
 */
@Data
@TableName("vip_order")
public class VipOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;            // 用户ID
    private String orderNo;         // 订单号（VIP+yyyyMMddHHmmss+6位随机）
    private BigDecimal amount;      // 支付金额（元）
    private Integer months;         // 时长（月）
    private Integer payStatus;      // 支付状态：0待支付 1成功
    private Integer simulated;      // 是否模拟支付：1是

    @TableField(fill = FieldFill.INSERT)   // 自动填充创建时间
    private LocalDateTime createTime;

    private LocalDateTime payTime;  // 支付时间

    private String transactionId;   // 微信支付订单号（真实支付回调/查单回填）
}
