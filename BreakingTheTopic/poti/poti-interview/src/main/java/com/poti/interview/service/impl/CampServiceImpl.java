package com.poti.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.interview.entity.CampCheckin;
import com.poti.interview.entity.CampMember;
import com.poti.interview.mapper.CampCheckinMapper;
import com.poti.interview.mapper.CampMemberMapper;
import com.poti.interview.service.CampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 面试特训营服务。
 * 训练营定义（营信息 + 每日大纲）为静态配置；报名与打卡记录落库。
 */
@Slf4j
@Service
public class CampServiceImpl implements CampService {

    @Autowired
    private CampMemberMapper campMemberMapper;

    @Autowired
    private CampCheckinMapper campCheckinMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");

    // ==================== 静态营配置 ====================

    private static class CampDef {
        String id;
        String title;
        String subtitle;
        String emoji;
        String tint;      // 图标块底色
        String accent;    // 主题色
        int totalDays;
        String tag;       // 适合人群标签
        String subject;   // 刷题/面试科目名
        String intro;
        String[] audience;
        String[] highlights;
        String[] topics;  // 每日主题（即大纲）

        CampDef(String id, String title, String subtitle, String emoji, String tint, String accent,
                int totalDays, String tag, String subject, String intro,
                String[] audience, String[] highlights, String[] topics) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.emoji = emoji;
            this.tint = tint;
            this.accent = accent;
            this.totalDays = totalDays;
            this.tag = tag;
            this.subject = subject;
            this.intro = intro;
            this.audience = audience;
            this.highlights = highlights;
            this.topics = topics;
        }
    }

    private static final List<CampDef> CAMPS = new ArrayList<>();

    static {
        CAMPS.add(new CampDef(
                "java-backend", "Java 后端面试特训营", "从基础到进阶，直通大厂后端岗",
                "🏆", "#FFF3E6", "#FF9500", 21, "适合1-3年经验", "Java",
                "以 Java 后端校招/社招面试大纲为主线，21 天系统过完 Java 基础、并发、JVM、Spring、MySQL、Redis 与分布式高频考点，每天学习 + 刷题 + 面试三件套，循序渐进拿下 Offer。",
                new String[]{"准备校招/实习的后端方向同学", "工作 1-3 年想跳槽进阶的后端工程师", "基础不牢、知识零散需要体系化梳理的开发者"},
                new String[]{"21 天完整面试知识路线图", "每天 5 道高频真题配套练习", "阶段复盘 + 结营综合模拟面试"},
                new String[]{
                        "Java基础：面向对象与四大特性", "Java基础：集合框架精讲", "Java基础：String与不可变设计",
                        "并发编程：线程与线程池", "并发编程：锁与AQS", "并发编程：并发容器与CAS",
                        "JVM：内存结构与类加载", "JVM：垃圾回收与调优入门", "阶段复盘 + 错题清零",
                        "Spring：IOC与AOP原理", "Spring：事务与常见坑", "Spring Boot：自动配置原理",
                        "MySQL：索引与B+树", "MySQL：事务与MVCC", "MySQL：锁机制与SQL优化",
                        "Redis：数据结构与缓存", "Redis：持久化与高可用", "消息队列：MQ基础与应用",
                        "分布式：CAP与常见方案", "系统设计：秒杀/短链/Feed流", "结营综合模拟面试"
                }));

        CAMPS.add(new CampDef(
                "algo-sprint", "算法与数据结构冲刺营", "14 天刷穿高频算法题",
                "🧮", "#F3EEFF", "#7B61FF", 14, "适合校招/全体", "算法",
                "按「数组→链表→树→动态规划」的路线 14 天刷穿高频算法题，每天一个专题 + 配套刷题 + 阶段复盘，帮你建立解题模板，告别拿到题没思路。",
                new String[]{"备战秋招/春招笔试面试的同学", "算法基础薄弱、刷题没方向的同学", "想系统整理解题模板的求职者"},
                new String[]{"14 天高频算法专题路线", "每天 5 道经典题型配套练习", "递归/DP 等解题模板总结"},
                new String[]{
                        "复杂度分析与数组双指针", "滑动窗口与前缀和", "链表：反转与合并",
                        "栈与队列经典题", "哈希表实战", "二分查找专题", "阶段复盘 + 错题清零",
                        "递归与回溯", "二叉树：遍历与性质", "二叉树：路径与LCA",
                        "动态规划：入门与背包", "动态规划：子序列问题", "图与贪心算法", "结营综合模拟面试"
                }));

        CAMPS.add(new CampDef(
                "cs-basic", "计算机基础综合营", "网络 + 操作系统 + 设计模式一站式补齐",
                "📖", "#E8F4FF", "#10AEFF", 14, "适合校招/转行", "计算机网络",
                "计算机基础是面试的「必修课」。本营 14 天串讲计算机网络、操作系统与设计模式三大板块高频考点，配合真题练习，快速补齐基础短板。",
                new String[]{"计算机基础薄弱的校招同学", "非科班转行想补基础的开发者", "简历项目多但基础题总丢分的同学"},
                new String[]{"网络/OS/设计模式三大板块全覆盖", "高频面试题逐日串讲", "真题练习 + 错题复盘闭环"},
                new String[]{
                        "计算机网络：TCP三次握手与四次挥手", "计算机网络：HTTP/HTTPS", "计算机网络：从输入URL到页面展示",
                        "操作系统：进程与线程", "操作系统：内存管理与虚拟内存", "操作系统：死锁与调度", "阶段复盘 + 错题清零",
                        "设计模式：单例/工厂/策略", "设计模式：观察者/代理/模板方法", "设计模式：六大设计原则",
                        "网络高频面试题串讲", "操作系统高频面试题串讲", "综合刷题冲刺", "结营综合模拟面试"
                }));

        CAMPS.add(new CampDef(
                "spring-mysql", "Spring+MySQL+Redis 实战营", "框架与中间件面试一网打尽",
                "🐬", "#EAF9F0", "#07C160", 14, "适合1-3年经验", "Spring",
                "聚焦面试中被问最多的三大件：Spring、MySQL、Redis。14 天从原理到实战，覆盖事务、索引调优、缓存三大问题、分布式锁等高频考点，让八股文变成真本事。",
                new String[]{"天天被 Spring/Redis 八股文问倒的同学", "想深入理解框架原理的后端工程师", "准备社招面试的 1-3 年经验开发者"},
                new String[]{"Spring 事务/IOC/AOP 原理精讲", "MySQL 索引与 SQL 调优实战", "Redis 缓存三大问题与分布式锁"},
                new String[]{
                        "Spring IOC 容器与Bean生命周期", "Spring AOP 与动态代理", "Spring 事务传播与失效场景",
                        "Spring Boot 自动装配", "Spring MVC 请求流程", "MySQL：索引设计实战", "阶段复盘 + 错题清零",
                        "MySQL：SQL调优与执行计划", "Redis：缓存穿透/击穿/雪崩", "Redis：分布式锁实战",
                        "Redis：缓存与数据库一致性", "综合架构：接口幂等与限流", "综合刷题冲刺", "结营综合模拟面试"
                }));
    }

    private CampDef findCamp(String campId) {
        return CAMPS.stream().filter(c -> c.id.equals(campId)).findFirst().orElse(null);
    }

    // ==================== 对外接口实现 ====================

    @Override
    public List<Map<String, Object>> getCampList(Long userId) {
        // 每个营的真实报名数（叠加静态基数做展示）
        Map<String, Long> realCounts = new HashMap<>();
        for (CampDef camp : CAMPS) {
            realCounts.put(camp.id, campMemberMapper.selectCount(
                    new LambdaQueryWrapper<CampMember>().eq(CampMember::getCampId, camp.id)));
        }
        // 当前用户已加入的营
        Map<String, CampMember> myMembers = findMembers(userId);
        Map<String, Long> myProgress = findProgress(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (CampDef camp : CAMPS) {
            result.add(toCampMap(camp,
                    realCounts.getOrDefault(camp.id, 0L),
                    myMembers.get(camp.id), myProgress.getOrDefault(camp.id, 0L), false));
        }
        return result;
    }

    @Override
    public Map<String, Object> getCampDetail(Long userId, String campId) {
        CampDef camp = findCamp(campId);
        if (camp == null) {
            return null;
        }
        long realCount = countCampMembers(campId);
        CampMember member = findMembers(userId).get(campId);
        long progress = member != null ? findProgress(userId).getOrDefault(campId, 0L) : 0L;

        Map<String, Object> result = toCampMap(camp, realCount, member, progress, false);

        // 训练大纲
        List<Map<String, Object>> outline = new ArrayList<>();
        for (int day = 1; day <= camp.totalDays; day++) {
            Map<String, Object> item = new HashMap<>();
            item.put("day", day);
            item.put("topic", camp.topics[day - 1]);
            item.put("done", progress >= day);
            outline.add(item);
        }
        result.put("outline", outline);

        // 适合人群 / 内容亮点
        List<String> audience = Arrays.asList(camp.audience);
        List<String> highlights = Arrays.asList(camp.highlights);
        result.put("audience", audience);
        result.put("highlights", highlights);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> joinCamp(Long userId, String campId) {
        CampDef camp = findCamp(campId);
        if (camp == null) {
            throw new IllegalArgumentException("训练营不存在");
        }
        CampMember existing = findMembers(userId).get(campId);
        if (existing == null) {
            CampMember member = new CampMember();
            member.setUserId(userId);
            member.setCampId(campId);
            member.setStatus(1);
            campMemberMapper.insert(member);
            existing = member;
            log.info("用户 {} 加入训练营 {}", userId, campId);
        }
        long progress = findProgress(userId).getOrDefault(campId, 0L);
        Map<String, Object> result = new HashMap<>();
        result.put("memberId", existing.getId());
        result.put("totalDays", camp.totalDays);
        result.put("progressDays", progress);
        result.put("finished", existing.getStatus() != null && existing.getStatus() == 2);
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyCamps(Long userId) {
        Map<String, CampMember> members = findMembers(userId);
        if (members.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, Long> progress = findProgress(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (CampDef camp : CAMPS) {
            CampMember member = members.get(camp.id);
            if (member == null) {
                continue;
            }
            result.add(toCampMap(camp, countCampMembers(camp.id), member, progress.getOrDefault(camp.id, 0L), true));
        }
        return result;
    }

    @Override
    public Map<String, Object> getCampHome(Long userId, String campId) {
        CampDef camp = findCamp(campId);
        if (camp == null) {
            return null;
        }
        CampMember member = findMembers(userId).get(campId);
        if (member == null) {
            throw new IllegalArgumentException("尚未加入该训练营");
        }
        List<CampCheckin> checkins = listCheckins(userId, campId);
        long progress = checkins.size();
        boolean todayChecked = checkins.stream()
                .anyMatch(c -> LocalDate.now().equals(c.getCheckinDate()));
        boolean finished = progress >= camp.totalDays;

        // 今日任务（结营后无任务）
        int currentDay = (int) Math.min(progress + 1, camp.totalDays);
        List<Map<String, Object>> tasks = finished
                ? new ArrayList<>()
                : buildDayTasks(camp, currentDay);

        // 打卡记录（预格式化日期，供前端直接渲染）
        List<Map<String, Object>> checkinList = new ArrayList<>();
        for (CampCheckin c : checkins) {
            Map<String, Object> item = new HashMap<>();
            item.put("dayNum", c.getDayNum());
            item.put("dateText", c.getCheckinDate().format(DATE_FMT));
            checkinList.add(item);
        }
        Collections.reverse(checkinList);

        Map<String, Object> result = toCampMap(camp, countCampMembers(campId), member, progress, true);
        result.put("finished", finished);
        result.put("todayChecked", todayChecked);
        result.put("currentDay", currentDay);
        result.put("todayTopic", camp.topics[currentDay - 1]);
        result.put("tasks", tasks);
        result.put("checkins", checkinList);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> checkin(Long userId, String campId) {
        CampDef camp = findCamp(campId);
        if (camp == null) {
            throw new IllegalArgumentException("训练营不存在");
        }
        CampMember member = findMembers(userId).get(campId);
        if (member == null) {
            throw new IllegalArgumentException("尚未加入该训练营");
        }

        List<CampCheckin> checkins = listCheckins(userId, campId);
        long progress = checkins.size();
        if (progress >= camp.totalDays) {
            throw new IllegalStateException("该训练营已结营，无需打卡");
        }
        LocalDate today = LocalDate.now();
        boolean todayChecked = checkins.stream()
                .anyMatch(c -> today.equals(c.getCheckinDate()));
        if (todayChecked) {
            throw new IllegalStateException("今天已经打卡过啦，明天继续加油");
        }

        // 顺序推进：第 N 次打卡即第 N 天任务
        CampCheckin checkin = new CampCheckin();
        checkin.setUserId(userId);
        checkin.setCampId(campId);
        checkin.setDayNum((int) progress + 1);
        checkin.setCheckinDate(today);
        campCheckinMapper.insert(checkin);

        long newProgress = progress + 1;
        if (newProgress >= camp.totalDays && (member.getStatus() == null || member.getStatus() != 2)) {
            member.setStatus(2);
            campMemberMapper.updateById(member);
            log.info("用户 {} 完成训练营 {} 全部任务，结营", userId, campId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dayNum", checkin.getDayNum());
        result.put("progressDays", newProgress);
        result.put("totalDays", camp.totalDays);
        result.put("finished", newProgress >= camp.totalDays);
        return result;
    }

    // ==================== 私有工具 ====================

    /** 营真实报名人数（camp_member 表计数，无静态基数） */
    private long countCampMembers(String campId) {
        return campMemberMapper.selectCount(
                new LambdaQueryWrapper<CampMember>().eq(CampMember::getCampId, campId));
    }

    private Map<String, CampMember> findMembers(Long userId) {
        List<CampMember> members = campMemberMapper.selectList(
                new LambdaQueryWrapper<CampMember>().eq(CampMember::getUserId, userId));
        Map<String, CampMember> map = new HashMap<>();
        for (CampMember m : members) {
            map.put(m.getCampId(), m);
        }
        return map;
    }

    /** 各营打卡进度：campId -> 已打卡天数 */
    private Map<String, Long> findProgress(Long userId) {
        List<CampCheckin> checkins = campCheckinMapper.selectList(
                new LambdaQueryWrapper<CampCheckin>().eq(CampCheckin::getUserId, userId));
        Map<String, Long> map = new HashMap<>();
        for (CampCheckin c : checkins) {
            map.merge(c.getCampId(), 1L, Long::sum);
        }
        return map;
    }

    private List<CampCheckin> listCheckins(Long userId, String campId) {
        return campCheckinMapper.selectList(
                new LambdaQueryWrapper<CampCheckin>()
                        .eq(CampCheckin::getUserId, userId)
                        .eq(CampCheckin::getCampId, campId)
                        .orderByAsc(CampCheckin::getDayNum));
    }

    private Map<String, Object> toCampMap(CampDef camp, long joinCount, CampMember member, long progress, boolean mine) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", camp.id);
        map.put("title", camp.title);
        map.put("subtitle", camp.subtitle);
        map.put("emoji", camp.emoji);
        map.put("tint", camp.tint);
        map.put("accent", camp.accent);
        map.put("totalDays", camp.totalDays);
        map.put("tag", camp.tag);
        map.put("joinCount", joinCount);
        map.put("intro", camp.intro);
        map.put("joined", member != null);
        map.put("progressDays", progress);
        if (member != null) {
            map.put("finished", member.getStatus() != null && member.getStatus() == 2);
        } else {
            map.put("finished", false);
        }
        return map;
    }

    /** 生成某一天的每日任务（静态模板 + 当日主题） */
    private List<Map<String, Object>> buildDayTasks(CampDef camp, int day) {
        String topic = camp.topics[day - 1];
        List<Map<String, Object>> tasks = new ArrayList<>();

        Map<String, Object> study = new HashMap<>();
        study.put("type", "study");
        study.put("icon", "📖");
        study.put("name", "知识点学习");
        study.put("desc", "「" + topic + "」要点梳理与精讲");
        study.put("linkType", "question");
        tasks.add(study);

        Map<String, Object> practice = new HashMap<>();
        practice.put("type", "practice");
        practice.put("icon", "✍️");
        practice.put("name", "刷题实战");
        practice.put("desc", "完成 5 道「" + camp.subject + "」相关题目");
        practice.put("linkType", "practice");
        tasks.add(practice);

        if (day % 3 == 0) {
            Map<String, Object> review = new HashMap<>();
            review.put("type", "review");
            review.put("icon", "🧯");
            review.put("name", "错题复盘");
            review.put("desc", "清理错题本中本阶段的错题");
            review.put("linkType", "wrongbook");
            tasks.add(review);
        } else {
            Map<String, Object> interview = new HashMap<>();
            interview.put("type", "interview");
            interview.put("icon", "🎤");
            interview.put("name", "模拟面试");
            interview.put("desc", "进行 1 场「" + camp.subject + "」模拟面试");
            interview.put("linkType", "interview");
            tasks.add(interview);
        }
        return tasks;
    }
}
