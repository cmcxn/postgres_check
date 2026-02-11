package com.database.postgrescheck.scheduler;

import com.database.postgrescheck.config.AppConfig;
import com.database.postgrescheck.service.DatabaseMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务调度器
 */
@Component
public class MonitorScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MonitorScheduler.class);

    @Autowired
    private DatabaseMonitorService monitorService;

    @Autowired
    private AppConfig appConfig;

    /**
     * 定时检查数据库数据
     * 默认每5分钟执行一次，可通过配置文件修改
     */
    @Scheduled(cron = "${app.monitor.cron:0 */5 * * * ?}")
    public void scheduleMonitoring() {
        logger.info("==================== 开始定时检查 ====================");
        monitorService.checkRecentData();
        logger.info("==================== 检查完成 ====================");
    }
}
