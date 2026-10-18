package com.poti.admin.service;

import com.poti.admin.entity.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 邮件发送服务：SMTP 参数实时读取 sys_config（邮箱设置 tab），后台改配置即时生效
 *
 * SMTP 服务器/端口/用户名/密码 四项均必填；端口 465 走 SSL，其余端口明文/STARTTLS
 */
@Slf4j
@Service
public class MailService {

    @Autowired
    private ConfigService configService;

    /** SMTP 参数是否已配置完整 */
    public boolean isConfigured() {
        return get("email_smtp_host") != null && get("email_smtp_username") != null
                && get("email_smtp_password") != null;
    }

    /** 未配置时的统一提示，前端可直接展示 */
    public String configTip() {
        return "邮箱服务未配置，请在后台「系统设置-邮件设置」中填写 SMTP 参数（QQ/163邮箱需开启SMTP服务并使用授权码）";
    }

    /** 发送验证码邮件。配置缺失或发送失败抛 IllegalStateException，message 为可直接展示的友好提示 */
    public void sendCaptcha(String to, String code) {
        String host = get("email_smtp_host");
        String username = get("email_smtp_username");
        String password = get("email_smtp_password");
        int port = 465;
        String portStr = get("email_smtp_port");
        if (portStr != null && portStr.matches("\\d+")) {
            port = Integer.parseInt(portStr);
        }
        if (host == null || username == null || password == null) {
            throw new IllegalStateException(configTip());
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        // 465 为 QQ/163 的 SSL 端口；587/25 走普通连接
        props.put("mail.smtp.ssl.enable", String.valueOf(port == 465));
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(username);
            helper.setTo(to);
            helper.setSubject("破题管理系统 - 密码重置验证码");
            helper.setText(buildHtml(code), true);
            sender.send(message);
            log.info("密码重置验证码邮件已发送: to={}", mask(to));
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("验证码邮件发送失败: to={}", mask(to), e);
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("550") || msg.toLowerCase().contains("auth fail") || msg.toLowerCase().contains("authentication")) {
                throw new IllegalStateException("SMTP 认证失败，请检查用户名与授权码（注意是授权码不是登录密码）");
            }
            throw new IllegalStateException("邮件发送失败，请检查 SMTP 服务器地址与端口配置");
        }
    }

    private String buildHtml(String code) {
        return "<div style=\"font-family:'Microsoft YaHei',sans-serif;max-width:480px;margin:0 auto;padding:24px;"
                + "border:1px solid #eee;border-radius:8px;\">"
                + "<h2 style=\"margin:0 0 16px;font-size:18px;color:#333;\">密码重置验证码</h2>"
                + "<p style=\"color:#666;font-size:14px;margin:0 0 12px;\">您正在重置破题管理系统的管理员密码，本次验证码为：</p>"
                + "<p style=\"font-size:28px;font-weight:bold;letter-spacing:6px;color:#6C47E0;margin:0 0 16px;\">" + code + "</p>"
                + "<p style=\"color:#999;font-size:12px;margin:0;\">验证码 10 分钟内有效。如非本人操作，请忽略本邮件。</p>"
                + "</div>";
    }

    private String get(String key) {
        Config config = configService.getByKey(key);
        String v = config != null ? config.getConfigValue() : null;
        return v == null || v.trim().isEmpty() ? null : v.trim();
    }

    /** 邮箱脱敏：a1b2c3@qq.com → a***@qq.com */
    private String mask(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
