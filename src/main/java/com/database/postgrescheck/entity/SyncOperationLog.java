package com.database.postgrescheck.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * sync_operation_log 表实体类
 */
@Entity
@Table(name = "sync_operation_log")
public class SyncOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "primary_key_value")
    private String primaryKeyValue;

    @Column(name = "operation")
    private String operation;

    @Column(name = "changed_columns")
    private String changedColumns;

    @Column(name = "old_values", columnDefinition = "jsonb")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "jsonb")
    private String newValues;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    // Constructors
    public SyncOperationLog() {
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPrimaryKeyValue() {
        return primaryKeyValue;
    }

    public void setPrimaryKeyValue(String primaryKeyValue) {
        this.primaryKeyValue = primaryKeyValue;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getChangedColumns() {
        return changedColumns;
    }

    public void setChangedColumns(String changedColumns) {
        this.changedColumns = changedColumns;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    @Override
    public String toString() {
        return "SyncOperationLog{" +
                "id=" + id +
                ", tableName='" + tableName + '\'' +
                ", primaryKeyValue='" + primaryKeyValue + '\'' +
                ", operation='" + operation + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
