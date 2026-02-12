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
            
            // 设置抄送人
            setCcIfConfigured(message);
            
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

    /**
     * 发送启动通知邮件
     */
    public void sendStartupNotification() {
        if (!appConfig.getNotification().isStartupEnabled()) {
            logger.info("启动通知已禁用，跳过发送");
            return;
        }

        // 验证收件人配置
        String toAddresses = appConfig.getNotification().getTo();
        if (toAddresses == null || toAddresses.trim().isEmpty()) {
            logger.error("启动通知失败：收件人配置为空，请在配置文件中设置 app.notification.to");
            return;
        }

        // 验证邮件模板
        String bodyTemplate = appConfig.getNotification().getStartupBodyTemplate();
        if (bodyTemplate == null || bodyTemplate.trim().isEmpty()) {
            logger.error("启动通知失败：邮件内容模板为空，请在配置文件中设置 app.notification.startup-body-template");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appConfig.getNotification().getFrom());
            
            // 支持多个收件人，用逗号分隔
            String[] recipients = toAddresses.split(",");
            message.setTo(recipients);
            
            // 设置抄送人
            setCcIfConfigured(message);
            
            message.setSubject(appConfig.getNotification().getStartupSubject());

            // 格式化邮件内容
            String body = String.format(
                    bodyTemplate,
                    LocalDateTime.now().format(FORMATTER),
                    appConfig.getMonitor().getTableName(),
                    appConfig.getMonitor().getTimeWindowMinutes()
            );
            message.setText(body);

            mailSender.send(message);
            logger.info("启动通知邮件已发送至: {}", appConfig.getNotification().getTo());
        } catch (Exception e) {
            logger.error("发送启动通知邮件失败。请检查以下配置：", e);
            logEmailConfigurationDiagnostics();
        }
    }

    /**
     * 记录邮件配置诊断信息
     * 帮助用户排查配置问题
     */
    private void logEmailConfigurationDiagnostics() {
        logger.error("=== 邮件配置诊断信息 ===");
        logger.error("请检查 application.yml 中的以下配置项：");
        logger.error("1. spring.mail.host - SMTP 服务器地址");
        logger.error("2. spring.mail.port - SMTP 端口（587/TLS 或 465/SSL）");
        logger.error("3. spring.mail.username - 发件邮箱账号");
        logger.error("4. spring.mail.password - 邮箱密码或授权码");
        logger.error("5. app.notification.from - 发件人地址");
        logger.error("6. app.notification.to - 收件人地址");
        logger.error("");
        logger.error("常见问题：");
        logger.error("- QQ/163邮箱需要使用授权码，不是登录密码");
        logger.error("- 检查防火墙是否阻止 SMTP 端口");
        logger.error("- 确认 SMTP 服务器地址和端口正确");
        logger.error("- 某些邮箱需要开启 SMTP 服务");
        logger.error("");
        logger.error("当前配置摘要：");
        logger.error("- 发件人: {}", appConfig.getNotification().getFrom());
        logger.error("- 收件人: {}", appConfig.getNotification().getTo());
        logger.error("- 启动通知: {}", appConfig.getNotification().isStartupEnabled() ? "已启用" : "已禁用");
        logger.error("=========================");
    }
    
    /**
     * 发送恢复通知邮件
     * 
     * @param downtimeMinutes 故障持续时间（分钟）
     */
    public void sendRecoveryAlert(long downtimeMinutes) {
        if (!appConfig.getNotification().isRecoveryEnabled()) {
            logger.info("恢复通知已禁用，跳过发送");
            return;
        }

        // 验证收件人配置
        String toAddresses = appConfig.getNotification().getTo();
        if (toAddresses == null || toAddresses.trim().isEmpty()) {
            logger.error("恢复通知失败：收件人配置为空，请在配置文件中设置 app.notification.to");
            return;
        }

        // 验证邮件模板
        String bodyTemplate = appConfig.getNotification().getRecoveryBodyTemplate();
        if (bodyTemplate == null || bodyTemplate.trim().isEmpty()) {
            logger.error("恢复通知失败：邮件内容模板为空，请在配置文件中设置 app.notification.recovery-body-template");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appConfig.getNotification().getFrom());
            
            // 支持多个收件人，用逗号分隔
            String[] recipients = toAddresses.split(",");
            message.setTo(recipients);
            
            // 设置抄送人
            setCcIfConfigured(message);
            
            message.setSubject(appConfig.getNotification().getRecoverySubject());

            // 格式化停机时长
            String downtimeDescription = formatDowntime(downtimeMinutes);
            
            // 格式化邮件内容
            String body = String.format(
                    bodyTemplate,
                    LocalDateTime.now().format(FORMATTER),
                    appConfig.getMonitor().getTableName(),
                    downtimeDescription,
                    downtimeMinutes
            );
            message.setText(body);

            mailSender.send(message);
            logger.info("恢复通知邮件已发送至: {}", appConfig.getNotification().getTo());
        } catch (Exception e) {
            logger.error("发送恢复通知邮件失败", e);
        }
    }
    
    /**
     * 发送每日健康检查邮件
     */
    public void sendDailyHealthCheck() {
        if (!appConfig.getNotification().isDailyHealthCheckEnabled()) {
            logger.info("每日健康检查通知已禁用，跳过发送");
            return;
        }

        // 验证收件人配置
        String toAddresses = appConfig.getNotification().getTo();
        if (toAddresses == null || toAddresses.trim().isEmpty()) {
            logger.error("每日健康检查失败：收件人配置为空，请在配置文件中设置 app.notification.to");
            return;
        }

        // 验证邮件模板
        String bodyTemplate = appConfig.getNotification().getDailyHealthCheckBodyTemplate();
        if (bodyTemplate == null || bodyTemplate.trim().isEmpty()) {
            logger.error("每日健康检查失败：邮件内容模板为空，请在配置文件中设置 app.notification.daily-health-check-body-template");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appConfig.getNotification().getFrom());
            
            // 支持多个收件人，用逗号分隔
            String[] recipients = toAddresses.split(",");
            message.setTo(recipients);
            
            // 设置抄送人
            setCcIfConfigured(message);
            
            message.setSubject(appConfig.getNotification().getDailyHealthCheckSubject());

            // 格式化邮件内容
            String body = String.format(
                    bodyTemplate,
                    LocalDateTime.now().format(FORMATTER),
                    appConfig.getMonitor().getTableName(),
                    appConfig.getMonitor().getTimeWindowMinutes()
            );
            message.setText(body);

            mailSender.send(message);
            logger.info("每日健康检查邮件已发送至: {}", appConfig.getNotification().getTo());
        } catch (Exception e) {
            logger.error("发送每日健康检查邮件失败", e);
        }
    }
    
    /**
     * 发送关闭通知邮件
     */
    public void sendShutdownNotification() {
        if (!appConfig.getNotification().isShutdownEnabled()) {
            logger.info("关闭通知已禁用，跳过发送");
            return;
        }

        // 验证收件人配置
        String toAddresses = appConfig.getNotification().getTo();
        if (toAddresses == null || toAddresses.trim().isEmpty()) {
            logger.error("关闭通知失败：收件人配置为空，请在配置文件中设置 app.notification.to");
            return;
        }

        // 验证邮件模板
        String bodyTemplate = appConfig.getNotification().getShutdownBodyTemplate();
        if (bodyTemplate == null || bodyTemplate.trim().isEmpty()) {
            logger.error("关闭通知失败：邮件内容模板为空，请在配置文件中设置 app.notification.shutdown-body-template");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appConfig.getNotification().getFrom());
            
            // 支持多个收件人，用逗号分隔
            String[] recipients = toAddresses.split(",");
            message.setTo(recipients);
            
            // 设置抄送人
            setCcIfConfigured(message);
            
            message.setSubject(appConfig.getNotification().getShutdownSubject());

            // 格式化邮件内容
            String body = String.format(
                    bodyTemplate,
                    LocalDateTime.now().format(FORMATTER),
                    appConfig.getMonitor().getTableName()
            );
            message.setText(body);

            mailSender.send(message);
            logger.info("关闭通知邮件已发送至: {}", appConfig.getNotification().getTo());
        } catch (Exception e) {
            logger.error("发送关闭通知邮件失败", e);
        }
    }
    
    /**
     * 格式化停机时长
     */
    private String formatDowntime(long minutes) {
        if (minutes < 60) {
            return minutes + " 分钟";
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            long mins = minutes % 60;
            return hours + " 小时 " + mins + " 分钟";
        } else {
            long days = minutes / 1440;
            long hours = (minutes % 1440) / 60;
            return days + " 天 " + hours + " 小时";
        }
    }
    
    /**
     * 设置抄送人（CC）
     * 如果配置了抄送人，则添加到邮件中
     * 
     * @param message 邮件消息对象
     */
    private void setCcIfConfigured(SimpleMailMessage message) {
        String ccAddresses = appConfig.getNotification().getCc();
        if (ccAddresses != null && !ccAddresses.trim().isEmpty()) {
            // 支持多个抄送人，用逗号分隔
            String[] ccRecipients = ccAddresses.split(",");
            // 去除每个地址的首尾空格
            for (int i = 0; i < ccRecipients.length; i++) {
                ccRecipients[i] = ccRecipients[i].trim();
            }
            message.setCc(ccRecipients);
            logger.debug("已添加抄送人: {}", ccAddresses);
        }
    }
}
