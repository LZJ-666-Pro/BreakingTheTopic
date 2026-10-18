package com.poti.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.user.entity.CheckIn;

import java.util.Map;

public interface CheckinService extends IService<CheckIn> {

    Map<String, Object> doCheckIn(Long userId);

    boolean hasCheckedInToday(Long userId);

    int getStreakDays(Long userId);

    /**
     * 签到成功后按连签里程碑发放会员时长奖励（幂等：同轮连签同档位仅发一次）。
     * 注意：方法内部读取后台跨库配置（@DS admin），不能放进 @Transactional 事务里调用。
     * @param streakDays 本次签到后的连签天数
     * @return 发放的会员天数；未达里程碑/开关关闭/已发放/发放失败返回 null
     */
    Integer grantMilestoneReward(Long userId, int streakDays);

    /**
     * 下一个连签奖励提示：{nextStreakDays 目标天数, remainDays 还需签几天, rewardDays 可得天数}
     * @return 开关关闭/无下一档/配置读取异常返回 null
     */
    Map<String, Object> getRewardHint(int streakDays);
}
