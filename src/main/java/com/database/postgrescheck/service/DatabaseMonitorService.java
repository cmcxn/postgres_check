package com.database.postgrescheck.service;

import com.database.postgrescheck.config.AppConfig;
import com.database.postgrescheck.repository.SyncOperationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
    
    // 告警状态：false = 正常，true = 已告警
    private volatile boolean inAlertState = false;
    
    // 告警开始时间
    private volatile LocalDateTime alertStartTime = null;

    /**
     * 检查最近指定时间窗口内是否有新数据
     * 如果没有新数据，则发送警告邮件
     * 如果从告警状态恢复，则发送恢复邮件
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
                // 没有新数据
                handleNoData(timeWindowMinutes);
            } else {
                // 有新数据
                handleDataPresent(count, timeWindowMinutes);
            }
        } catch (Exception e) {
            logger.error("检查数据时发生错误", e);
        }
    }
    
    /**
     * 处理无数据情况
     */
    private void handleNoData(int timeWindowMinutes) {
        logger.warn("警告：{} 表在过去 {} 分钟内没有新增数据", 
                appConfig.getMonitor().getTableName(), timeWindowMinutes);
        
        // 获取最新记录时间
        LocalDateTime latestTime = repository.findLatestChangedAt();
        if (latestTime != null) {
            logger.info("最新记录时间: {}", latestTime);
        } else {
            logger.warn("表中没有任何数据");
        }

        // 如果之前不在告警状态，则发送告警邮件并进入告警状态
        if (!inAlertState) {
            emailService.sendNoDataAlert(timeWindowMinutes);
            inAlertState = true;
            alertStartTime = LocalDateTime.now();
            logger.info("系统进入告警状态，告警开始时间: {}", alertStartTime);
        } else {
            logger.info("系统已在告警状态，跳过重复告警邮件");
        }
    }
    
    /**
     * 处理有数据情况
     */
    private void handleDataPresent(long count, int timeWindowMinutes) {
        logger.info("正常：{} 表在过去 {} 分钟内有 {} 条新增数据", 
                appConfig.getMonitor().getTableName(), timeWindowMinutes, count);
        
        // 如果之前在告警状态，则发送恢复邮件并退出告警状态
        if (inAlertState) {
            LocalDateTime recoveryTime = LocalDateTime.now();
            long downtimeMinutes = 0;
            
            if (alertStartTime != null) {
                Duration downtime = Duration.between(alertStartTime, recoveryTime);
                downtimeMinutes = downtime.toMinutes();
                logger.info("系统从告警状态恢复，故障持续时间: {} 分钟", downtimeMinutes);
            }
            
            emailService.sendRecoveryAlert(downtimeMinutes);
            inAlertState = false;
            alertStartTime = null;
            logger.info("系统恢复正常状态");
        }
    }
    
    /**
     * 获取当前告警状态
     */
    public boolean isInAlertState() {
        return inAlertState;
    }
    
    /**
     * 获取告警开始时间
     */
    public LocalDateTime getAlertStartTime() {
        return alertStartTime;
    }
}
