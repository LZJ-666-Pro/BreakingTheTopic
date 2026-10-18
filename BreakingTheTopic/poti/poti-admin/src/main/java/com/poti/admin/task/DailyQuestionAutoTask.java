package com.poti.admin.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.admin.entity.Config;
import com.poti.admin.entity.DailyQuestion;
import com.poti.admin.mapper.DailyMapper;
import com.poti.admin.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 每日一题自动发布定时任务。
 *
 * 规则（sys_config 控制，后台「系统设置-业务开关」可改）：
 * - daily_question_auto = true 且已过当日发布时间（daily_question_time，默认 00:00）时，
 *   若今日尚未配置每日一题，则从题库随机抽取一题自动发布（排除最近 30 天已用题目，避免重复）；
 * - 开关关闭后任务直接跳过，不再自动发布；手动配置的记录优先，自动任务不覆盖。
 * - 每 30 分钟轮询一次：服务重启、错过发布时间点都能自动补偿，且天然幂等（当日已有记录即跳过）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyQuestionAutoTask {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ConfigService configService;
    private final DailyMapper dailyMapper;

    /** 服务启动就绪后立即检查一次：错过发布时间点的当天任务（如重启/宕机）立刻补偿，无需等下一轮轮询 */
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        autoPublish();
    }

    @Scheduled(cron = "0 5/30 * * * ?")
    public void autoPublish() {
        try {
            if (!isEnabled()) {
                return;
            }
            LocalDate today = LocalDate.now();
            if (existsOn(today)) {
                return;
            }
            LocalTime publishTime = resolvePublishTime();
            if (LocalDateTime.now().toLocalTime().isBefore(publishTime)) {
                return;
            }

            Map<String, Object> picked = dailyMapper.selectRandomQuestion(today.minusDays(30));
            if (picked == null) {
                // 近 30 天题目全部用尽时兜底：不排除重复再抽一次
                picked = dailyMapper.selectRandomQuestionAny();
            }
            if (picked == null || picked.get("id") == null) {
                log.warn("每日一题自动发布失败：题库无可用题目");
                return;
            }

            DailyQuestion daily = new DailyQuestion();
            daily.setQuestionId(Long.valueOf(String.valueOf(picked.get("id"))));
            daily.setDate(today);
            daily.setReason("系统每日精选，温故知新");
            daily.setHint(null);
            dailyMapper.insert(daily);
            log.info("每日一题自动发布成功：date={}, questionId={}", today, daily.getQuestionId());
        } catch (Exception e) {
            log.error("每日一题自动发布任务异常", e);
        }
    }

    /** 开关：daily_question_auto = true 才启用自动化 */
    private boolean isEnabled() {
        Config config = configService.getByKey("daily_question_auto");
        return config != null && "true".equalsIgnoreCase(String.valueOf(config.getConfigValue()).trim());
    }

    /** 发布时间：daily_question_time（HH:mm），解析失败回退 00:00 */
    private LocalTime resolvePublishTime() {
        try {
            Config config = configService.getByKey("daily_question_time");
            if (config != null && config.getConfigValue() != null) {
                return LocalTime.parse(config.getConfigValue().trim(), TIME_FORMAT);
            }
        } catch (Exception ignored) {
        }
        return LocalTime.MIN;
    }

    private boolean existsOn(LocalDate date) {
        LambdaQueryWrapper<DailyQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyQuestion::getDate, date);
        return dailyMapper.selectCount(wrapper) > 0;
    }
}
