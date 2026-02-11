package com.database.postgrescheck.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 启动通知服务
 * 在应用启动完成后发送通知邮件
 */
@Service
public class StartupNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(StartupNotificationService.class);

    @Autowired
    private EmailNotificationService emailService;

    /**
     * 监听应用启动完成事件
     * 在应用完全启动后发送启动通知邮件
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("应用启动完成，准备发送启动通知邮件");
        emailService.sendStartupNotification();
    }
}
