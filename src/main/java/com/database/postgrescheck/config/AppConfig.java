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
        private String subject = "[警告] 数据库监控：sync_operation_log 表无新数据";
        private String bodyTemplate;
        
        // 启动通知配置
        private boolean startupEnabled = true;
        private String startupSubject = "[通知] 数据库监控系统已启动";
        private String startupBodyTemplate;

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
    }
}
