package com.database.postgrescheck.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private Monitor monitor = new Monitor();
    private Notification notification = new Notification();

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    /**
     * 监控配置
     */
    public static class Monitor {
        private String cron = "0 */5 * * * ?";
        private int timeWindowMinutes = 5;
        private String tableName = "sync_operation_log";

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public int getTimeWindowMinutes() {
            return timeWindowMinutes;
        }

        public void setTimeWindowMinutes(int timeWindowMinutes) {
            this.timeWindowMinutes = timeWindowMinutes;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
    }

    /**
     * 通知配置
     */
    public static class Notification {
        private boolean enabled = true;
        private String from;
        private String to;
        private String cc;  // 抄送人邮箱，多个用逗号分隔
        private String subject = "[警告] 数据库监控：sync_operation_log 表无新数据";
        private String bodyTemplate;
        
        // 启动通知配置
        private boolean startupEnabled = true;
        private String startupSubject = "[通知] 数据库监控系统已启动";
        private String startupBodyTemplate;
        
        // 恢复通知配置
        private boolean recoveryEnabled = true;
        private String recoverySubject = "[恢复] 数据库监控：数据恢复正常";
        private String recoveryBodyTemplate;
        
        // 每日健康检查配置
        private boolean dailyHealthCheckEnabled = true;
        private String dailyHealthCheckCron = "0 0 8 * * ?";  // 每天早上8:00
        private String dailyHealthCheckSubject = "[正常] 数据库监控系统运行正常";
        private String dailyHealthCheckBodyTemplate;
        
        // 关闭通知配置
        private boolean shutdownEnabled = true;
        private String shutdownSubject = "[通知] 数据库监控系统已关闭";
        private String shutdownBodyTemplate;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getCc() {
            return cc;
        }

        public void setCc(String cc) {
            this.cc = cc;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getBodyTemplate() {
            return bodyTemplate;
        }

        public void setBodyTemplate(String bodyTemplate) {
            this.bodyTemplate = bodyTemplate;
        }

        public boolean isStartupEnabled() {
            return startupEnabled;
        }

        public void setStartupEnabled(boolean startupEnabled) {
            this.startupEnabled = startupEnabled;
        }

        public String getStartupSubject() {
            return startupSubject;
        }

        public void setStartupSubject(String startupSubject) {
            this.startupSubject = startupSubject;
        }

        public String getStartupBodyTemplate() {
            return startupBodyTemplate;
        }

        public void setStartupBodyTemplate(String startupBodyTemplate) {
            this.startupBodyTemplate = startupBodyTemplate;
        }

        public boolean isRecoveryEnabled() {
            return recoveryEnabled;
        }

        public void setRecoveryEnabled(boolean recoveryEnabled) {
            this.recoveryEnabled = recoveryEnabled;
        }

        public String getRecoverySubject() {
            return recoverySubject;
        }

        public void setRecoverySubject(String recoverySubject) {
            this.recoverySubject = recoverySubject;
        }

        public String getRecoveryBodyTemplate() {
            return recoveryBodyTemplate;
        }

        public void setRecoveryBodyTemplate(String recoveryBodyTemplate) {
            this.recoveryBodyTemplate = recoveryBodyTemplate;
        }

        public boolean isDailyHealthCheckEnabled() {
            return dailyHealthCheckEnabled;
        }

        public void setDailyHealthCheckEnabled(boolean dailyHealthCheckEnabled) {
            this.dailyHealthCheckEnabled = dailyHealthCheckEnabled;
        }

        public String getDailyHealthCheckCron() {
            return dailyHealthCheckCron;
        }

        public void setDailyHealthCheckCron(String dailyHealthCheckCron) {
            this.dailyHealthCheckCron = dailyHealthCheckCron;
        }

        public String getDailyHealthCheckSubject() {
            return dailyHealthCheckSubject;
        }

        public void setDailyHealthCheckSubject(String dailyHealthCheckSubject) {
            this.dailyHealthCheckSubject = dailyHealthCheckSubject;
        }

        public String getDailyHealthCheckBodyTemplate() {
            return dailyHealthCheckBodyTemplate;
        }

        public void setDailyHealthCheckBodyTemplate(String dailyHealthCheckBodyTemplate) {
            this.dailyHealthCheckBodyTemplate = dailyHealthCheckBodyTemplate;
        }

        public boolean isShutdownEnabled() {
            return shutdownEnabled;
        }

        public void setShutdownEnabled(boolean shutdownEnabled) {
            this.shutdownEnabled = shutdownEnabled;
        }

        public String getShutdownSubject() {
            return shutdownSubject;
        }

        public void setShutdownSubject(String shutdownSubject) {
            this.shutdownSubject = shutdownSubject;
        }

        public String getShutdownBodyTemplate() {
            return shutdownBodyTemplate;
        }

        public void setShutdownBodyTemplate(String shutdownBodyTemplate) {
            this.shutdownBodyTemplate = shutdownBodyTemplate;
        }
    }
}
