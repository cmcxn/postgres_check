package com.database.postgrescheck.service;

import com.database.postgrescheck.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Transport;

/**
 * 邮件配置验证服务
 * 在应用启动时验证 SMTP 配置
 */
@Service
public class EmailConfigValidator {

    private static final Logger logger = LoggerFactory.getLogger(EmailConfigValidator.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AppConfig appConfig;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    /**
     * 在应用启动时验证邮件配置
     * 优先级高于启动通知，确保先检查配置
     */
    @Order(1)
    @EventListener(ApplicationReadyEvent.class)
    public void validateEmailConfiguration() {
        logger.info("========================================");
        logger.info("开始验证邮件配置...");
        logger.info("========================================");

        boolean hasIssues = false;

        // 检查 SMTP 基本配置
        if (mailHost == null || mailHost.trim().isEmpty() || mailHost.equals("smtp.example.com")) {
            logger.warn("⚠️  SMTP 服务器未配置或使用默认值: {}", mailHost);
            logger.warn("   请在 application.yml 中设置 spring.mail.host");
            hasIssues = true;
        } else {
            logger.info("✓ SMTP 服务器: {}", mailHost);
        }

        if (mailPort == 0) {
            logger.warn("⚠️  SMTP 端口未配置");
            logger.warn("   请在 application.yml 中设置 spring.mail.port (通常为 587 或 465)");
            hasIssues = true;
        } else {
            logger.info("✓ SMTP 端口: {}", mailPort);
        }

        if (mailUsername == null || mailUsername.trim().isEmpty() || mailUsername.contains("your_email")) {
            logger.warn("⚠️  SMTP 用户名未配置或使用默认值: {}", mailUsername);
            logger.warn("   请在 application.yml 中设置 spring.mail.username");
            hasIssues = true;
        } else {
            logger.info("✓ SMTP 用户名: {}***", maskEmail(mailUsername));
        }

        // 检查通知配置
        String from = appConfig.getNotification().getFrom();
        if (from == null || from.trim().isEmpty() || from.contains("your_email")) {
            logger.warn("⚠️  发件人地址未配置或使用默认值: {}", from);
            logger.warn("   请在 application.yml 中设置 app.notification.from");
            hasIssues = true;
        } else {
            logger.info("✓ 发件人: {}", from);
        }

        String to = appConfig.getNotification().getTo();
        if (to == null || to.trim().isEmpty() || to.contains("recipient@example.com")) {
            logger.warn("⚠️  收件人地址未配置或使用默认值: {}", to);
            logger.warn("   请在 application.yml 中设置 app.notification.to");
            hasIssues = true;
        } else {
            logger.info("✓ 收件人: {}", to);
        }

        // 测试 SMTP 连接
        if (!hasIssues) {
            testSmtpConnection();
        } else {
            logger.warn("");
            logger.warn("❌ 邮件配置存在问题，请修复后重启应用");
            logger.warn("   配置文件位置: src/main/resources/application.yml");
            logger.warn("   参考模板: src/main/resources/application.yml.template");
        }

        logger.info("========================================");
    }

    /**
     * 测试 SMTP 服务器连接
     */
    private void testSmtpConnection() {
        logger.info("正在测试 SMTP 服务器连接...");
        try {
            if (mailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl sender = (JavaMailSenderImpl) mailSender;
                // 尝试建立连接
                Transport transport = sender.getSession().getTransport("smtp");
                try {
                    transport.connect(mailHost, mailPort, mailUsername, sender.getPassword());
                    logger.info("✓ SMTP 服务器连接成功");
                    transport.close();
                } catch (MessagingException e) {
                    logger.error("❌ SMTP 服务器连接失败: {}", e.getMessage());
                    logger.error("   请检查:");
                    logger.error("   1. SMTP 服务器地址和端口是否正确");
                    logger.error("   2. 用户名和密码/授权码是否正确");
                    logger.error("   3. 网络是否可以访问 SMTP 服务器");
                    logger.error("   4. 防火墙是否允许 SMTP 端口");
                    if (e.getMessage().contains("AuthenticationFailedException")) {
                        logger.error("   5. 如使用 QQ/163 邮箱，确认使用的是授权码而非登录密码");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("❌ SMTP 连接测试失败: {}", e.getMessage());
        }
    }

    /**
     * 掩码显示邮箱地址（保护隐私）
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String prefix = email.substring(0, Math.min(3, atIndex));
            return prefix + "***@" + email.substring(atIndex + 1);
        }
        return email.substring(0, 3) + "***";
    }
}
