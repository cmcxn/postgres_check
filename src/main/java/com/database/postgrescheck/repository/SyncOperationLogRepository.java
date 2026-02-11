package com.database.postgrescheck.repository;

import com.database.postgrescheck.entity.SyncOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * sync_operation_log 表数据访问层
 */
@Repository
public interface SyncOperationLogRepository extends JpaRepository<SyncOperationLog, Integer> {

    /**
     * 统计指定时间之后的记录数
     *
     * @param afterTime 开始时间
     * @return 记录数
     */
    @Query("SELECT COUNT(s) FROM SyncOperationLog s WHERE s.changedAt >= :afterTime")
    long countByChangedAtAfter(@Param("afterTime") LocalDateTime afterTime);

    /**
     * 查找最新的一条记录
     *
     * @return 最新记录的changed_at时间
     */
    @Query("SELECT MAX(s.changedAt) FROM SyncOperationLog s")
    LocalDateTime findLatestChangedAt();
}
