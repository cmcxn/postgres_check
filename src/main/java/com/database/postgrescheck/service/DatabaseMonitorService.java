package com.database.postgrescheck.service;

import com.database.postgrescheck.config.AppConfig;
import com.database.postgrescheck.repository.SyncOperationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 数据库监控服务
 */
@Service
public class DatabaseMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMonitorService.class);

    @Autowired
    private SyncOperationLogRepository repository;

    @Autowired
    private EmailNotificationService emailService;

    @Autowired
    private AppConfig appConfig;

    /**
     * 检查最近指定时间窗口内是否有新数据
     * 如果没有新数据，则发送警告邮件
     */
    public void checkRecentData() {
        int timeWindowMinutes = appConfig.getMonitor().getTimeWindowMinutes();
        LocalDateTime checkTime = LocalDateTime.now().minusMinutes(timeWindowMinutes);

        logger.info("开始检查 {} 表，时间窗口：最近 {} 分钟", 
                appConfig.getMonitor().getTableName(), timeWindowMinutes);

        try {
            // 统计指定时间之后的记录数
            long count = repository.countByChangedAtAfter(checkTime);

            if (count == 0) {
                logger.warn("警告：{} 表在过去 {} 分钟内没有新增数据", 
                        appConfig.getMonitor().getTableName(), timeWindowMinutes);
                
                // 获取最新记录时间
                LocalDateTime latestTime = repository.findLatestChangedAt();
                if (latestTime != null) {
                    logger.info("最新记录时间: {}", latestTime);
                } else {
                    logger.warn("表中没有任何数据");
                }

                // 发送警告邮件
                emailService.sendNoDataAlert(timeWindowMinutes);
            } else {
                logger.info("正常：{} 表在过去 {} 分钟内有 {} 条新增数据", 
                        appConfig.getMonitor().getTableName(), timeWindowMinutes, count);
            }
        } catch (Exception e) {
            logger.error("检查数据时发生错误", e);
        }
    }
}
