package com.database.postgrescheck.service;

import com.database.postgrescheck.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 邮件通知服务
 */
@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AppConfig appConfig;

    /**
     * 发送数据缺失警告邮件
     *
     * @param timeWindowMinutes 时间窗口（分钟）
     */
    public void sendNoDataAlert(int timeWindowMinutes) {
        if (!appConfig.getNotification().isEnabled()) {
            logger.info("邮件通知已禁用，跳过发送");
            return;
        }

        // 验证收件人配置
        String toAddresses = appConfig.getNotification().getTo();
        if (toAddresses == null || toAddresses.trim().isEmpty()) {
            logger.error("邮件通知失败：收件人配置为空，请在配置文件中设置 app.notification.to");
            return;
        }

        // 验证邮件模板
        String bodyTemplate = appConfig.getNotification().getBodyTemplate();
        if (bodyTemplate == null || bodyTemplate.trim().isEmpty()) {
            logger.error("邮件通知失败：邮件内容模板为空，请在配置文件中设置 app.notification.body-template");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appConfig.getNotification().getFrom());
            
            // 支持多个收件人，用逗号分隔
            String[] recipients = toAddresses.split(",");
            message.setTo(recipients);
            
            message.setSubject(appConfig.getNotification().getSubject());

            // 格式化邮件内容
            String body = String.format(
                    bodyTemplate,
                    timeWindowMinutes,
                    LocalDateTime.now().format(FORMATTER),
                    appConfig.getMonitor().getTableName(),
                    timeWindowMinutes
            );
            message.setText(body);

            mailSender.send(message);
            logger.info("数据缺失警告邮件已发送至: {}", appConfig.getNotification().getTo());
        } catch (Exception e) {
            logger.error("发送邮件失败", e);
        }
    }
}
