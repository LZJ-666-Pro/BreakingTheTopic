package com.poti.admin.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poti.admin.entity.Admin;
import com.poti.admin.entity.OperationLog;
import com.poti.admin.mapper.AdminMapper;
import com.poti.admin.mapper.OperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 操作日志切面：自动拦截 admin 后台所有写操作（POST/PUT/DELETE），
 * 记录操作人、模块、动作、参数摘要、结果、IP 与耗时，无需逐个方法加注解。
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private AdminMapper adminMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("execution(* com.poti.admin.controller..*(..)) "
            + "&& ( @annotation(org.springframework.web.bind.annotation.PostMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PutMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping) )")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Throwable error = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            try {
                saveLog(joinPoint, System.currentTimeMillis() - start, error);
            } catch (Exception e) {
                log.warn("操作日志记录失败: {}", e.getMessage());
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long costMs, Throwable error) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return;
        }
        HttpServletRequest request = attrs.getRequest();
        String uri = request.getRequestURI();

        OperationLog logEntry = new OperationLog();
        logEntry.setModule(resolveModule(uri));
        logEntry.setAction(resolveAction(request.getMethod()));
        logEntry.setTarget(uri);
        logEntry.setDetail(buildDetail(joinPoint.getArgs()));
        logEntry.setResult(error == null ? 1 : 0);
        logEntry.setIp(resolveIp(request));
        logEntry.setCostMs((int) Math.min(costMs, Integer.MAX_VALUE));
        logEntry.setCreateTime(LocalDateTime.now());

        // 操作人：网关注入的 X-User-Id
        String userIdHeader = request.getHeader("X-User-Id");
        try {
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                Long adminId = Long.valueOf(userIdHeader);
                logEntry.setAdminId(adminId);
                Admin admin = adminMapper.selectById(adminId);
                if (admin != null) {
                    String name = admin.getNickname() != null && !admin.getNickname().isEmpty()
                            ? admin.getNickname() : admin.getUsername();
                    logEntry.setAdminName(name);
                }
            }
        } catch (NumberFormatException ignore) {
            // 非数字头，按未知处理
        }
        if (logEntry.getAdminName() == null || logEntry.getAdminName().isEmpty()) {
            logEntry.setAdminId(0L);
            logEntry.setAdminName("未知");
        }

        operationLogMapper.insert(logEntry);
    }

    /** URI 前缀 → 中文模块名 */
    private String resolveModule(String uri) {
        if (uri.contains("/admin/question")) return "题目管理";
        if (uri.contains("/admin/category")) return "分类管理";
        if (uri.contains("/admin/tag")) return "标签管理";
        if (uri.contains("/admin/special")) return "题库专题";
        if (uri.contains("/admin/daily")) return "每日一题";
        if (uri.contains("/admin/solution")) return "题解审核";
        if (uri.contains("/admin/article")) return "文章管理";
        if (uri.contains("/admin/notice")) return "公告管理";
        if (uri.contains("/admin/role")) return "角色管理";
        if (uri.contains("/admin/account")) return "管理员账号";
        if (uri.contains("/admin/user")) return "用户管理";
        if (uri.contains("/admin/feedback")) return "反馈与举报";
        if (uri.contains("/admin/config")) return "系统配置";
        if (uri.contains("/admin/log")) return "操作日志";
        if (uri.contains("/admin/login") || uri.contains("/admin/register")) return "登录注册";
        return "其他";
    }

    private String resolveAction(String method) {
        switch (method) {
            case "POST": return "新增";
            case "PUT": return "更新";
            case "DELETE": return "删除";
            default: return method;
        }
    }

    /** 参数摘要：取第一个非空参数序列化为 JSON，截断 500 字符 */
    private String buildDetail(Object[] args) {
        try {
            if (args == null) {
                return "";
            }
            for (Object arg : args) {
                if (arg == null) continue;
                // 跳过 HttpServletRequest 等容器对象
                if (arg instanceof javax.servlet.ServletRequest || arg instanceof javax.servlet.ServletResponse) continue;
                String json;
                if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                    json = String.valueOf(arg);
                } else {
                    json = objectMapper.writeValueAsString(arg);
                }
                if (json != null && !json.isEmpty() && !"null".equals(json)) {
                    return json.length() > 500 ? json.substring(0, 500) + "..." : json;
                }
            }
        } catch (Exception ignore) {
            // 序列化失败不影响主流程
        }
        return "";
    }

    private String resolveIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
