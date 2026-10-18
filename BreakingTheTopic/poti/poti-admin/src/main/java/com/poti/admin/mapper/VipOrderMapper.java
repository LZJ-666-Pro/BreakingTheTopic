package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.VipOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 会员订单 Mapper（跨库 poti_user.vip_order）。
 */
@Mapper
@DS("user")
public interface VipOrderMapper extends BaseMapper<VipOrder> {

    /** 批量统计用户累计消费（成功订单金额合计） */
    @Select("<script>SELECT user_id AS userId, IFNULL(SUM(amount), 0) AS totalAmount FROM vip_order " +
            "WHERE pay_status = 1 AND user_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "GROUP BY user_id</script>")
    List<Map<String, Object>> selectTotalAmountBatch(@Param("ids") List<Long> ids);
}
