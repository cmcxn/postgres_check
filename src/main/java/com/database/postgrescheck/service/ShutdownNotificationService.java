package com.database.postgrescheck.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

/**
 * 应用关闭通知服务
 * 在应用关闭时发送通知邮件
 */
@Service
public class ShutdownNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownNotificationService.class);

    @Autowired
    private EmailNotificationService emailService;

    /**
     * 应用关闭时的回调方法
     * 发送关闭通知邮件
     */
    @PreDestroy
    public void onShutdown() {
        logger.info("应用正在关闭，准备发送关闭通知邮件");
        try {
            emailService.sendShutdownNotification();
            // 等待邮件发送完成
            Thread.sleep(2000);
        } catch (Exception e) {
            logger.error("发送关闭通知邮件失败", e);
        }
    }
}
