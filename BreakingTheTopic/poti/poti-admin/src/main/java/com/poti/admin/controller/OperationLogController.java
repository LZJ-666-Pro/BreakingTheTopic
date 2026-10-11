package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.OperationLog;
import com.poti.admin.mapper.OperationLogMapper;
import com.poti.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/log")
public class OperationLogController {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @GetMapping("/list")
    public R<Page<OperationLog>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String adminName,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer result,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
            if (adminName != null && !adminName.trim().isEmpty()) {
                wrapper.like(OperationLog::getAdminName, adminName.trim());
            }
            if (module != null && !module.isEmpty()) {
                wrapper.eq(OperationLog::getModule, module);
            }
            if (result != null) {
                wrapper.eq(OperationLog::getResult, result);
            }
            if (startDate != null && !startDate.isEmpty()) {
                wrapper.ge(OperationLog::getCreateTime, LocalDate.parse(startDate).atStartOfDay());
            }
            if (endDate != null && !endDate.isEmpty()) {
                wrapper.le(OperationLog::getCreateTime, LocalDate.parse(endDate).atTime(LocalTime.MAX));
            }
            wrapper.orderByDesc(OperationLog::getCreateTime);
            Page<OperationLog> page = operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
            return R.success(page);
        } catch (Exception e) {
            log.error("获取操作日志失败", e);
            return R.error("获取操作日志失败");
        }
    }

    @GetMapping("/{id}")
    public R<OperationLog> detail(@PathVariable Long id) {
        try {
            OperationLog operationLog = operationLogMapper.selectById(id);
            if (operationLog == null) {
                return R.error("日志不存在");
            }
            return R.success(operationLog);
        } catch (Exception e) {
            log.error("获取日志详情失败", e);
            return R.error("获取日志详情失败");
        }
    }

    @PostMapping("/clear")
    public R<Void> clear() {
        try {
            operationLogMapper.delete(new LambdaQueryWrapper<>());
            log.info("操作日志已清空");
            return R.success(null);
        } catch (Exception e) {
            log.error("清空操作日志失败", e);
            return R.error("清空操作日志失败");
        }
    }

    /** 导出当前筛选结果的 CSV 文件 */
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String adminName,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer result,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (adminName != null && !adminName.trim().isEmpty()) {
            wrapper.like(OperationLog::getAdminName, adminName.trim());
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (result != null) {
            wrapper.eq(OperationLog::getResult, result);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(OperationLog::getCreateTime, LocalDate.parse(startDate).atStartOfDay());
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(OperationLog::getCreateTime, LocalDate.parse(endDate).atTime(LocalTime.MAX));
        }
        wrapper.orderByDesc(OperationLog::getCreateTime).last("LIMIT 10000");
        List<OperationLog> logs = operationLogMapper.selectList(wrapper);

        response.setContentType("text/csv;charset=UTF-8");
        String fileName = URLEncoder.encode("操作日志_" + LocalDate.now() + ".csv", StandardCharsets.UTF_8.displayName());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder("\uFEFF"); // BOM 保证 Excel 打开不乱码
        sb.append("操作人,模块,动作,操作对象,结果,IP,耗时(ms),操作时间,参数摘要\n");
        for (OperationLog l : logs) {
            sb.append(csv(l.getAdminName())).append(',')
              .append(csv(l.getModule())).append(',')
              .append(csv(l.getAction())).append(',')
              .append(csv(l.getTarget())).append(',')
              .append(l.getResult() != null && l.getResult() == 1 ? "成功" : "失败").append(',')
              .append(csv(l.getIp())).append(',')
              .append(l.getCostMs() == null ? "" : l.getCostMs()).append(',')
              .append(l.getCreateTime() == null ? "" : l.getCreateTime().format(fmt)).append(',')
              .append(csv(l.getDetail())).append('\n');
        }
        response.getWriter().write(sb.toString());
    }

    private String csv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
