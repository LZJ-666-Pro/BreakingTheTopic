-- 42: 签到奖励发放记录表（连签里程碑送会员时长）
-- 业务：连签 3/7/15/30 天自动送会员时长（档位天数后台 sys_config 可配：checkin_reward_days_3/7/15/30，
--       总开关 checkin_reward_enabled）；断签一天归零后重新累计开启新一轮，可循环再得；
--       幂等键 uk_user_streak_cycle（user_id + streak_days + cycle_start_date）保证同轮连签同一档位仅发放一次
USE poti_user;

CREATE TABLE IF NOT EXISTS `checkin_reward_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `streak_days` int NOT NULL COMMENT '达成里程碑的连签天数（3/7/15/30）',
  `reward_days` int NOT NULL COMMENT '发放的会员天数',
  `cycle_start_date` date NOT NULL COMMENT '本轮连签起始日期（断签归零后开启新一轮）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发放时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_streak_cycle` (`user_id`, `streak_days`, `cycle_start_date`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到奖励发放记录表';
