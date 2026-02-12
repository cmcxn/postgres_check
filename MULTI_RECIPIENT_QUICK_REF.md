# 多人接收和抄送 - 快速参考卡

## 快速配置

### 基础配置（单个收件人）
```yaml
app:
  notification:
    to: admin@example.com
```

### 多个收件人
```yaml
app:
  notification:
    to: admin1@example.com,admin2@example.com,admin3@example.com
```

### 多个收件人 + 抄送人
```yaml
app:
  notification:
    to: admin1@example.com,admin2@example.com
    cc: supervisor@example.com,manager@example.com
```

## 字段说明

| 字段 | 必填 | 说明 | 示例 |
|-----|------|------|------|
| `to` | ✅ 是 | 主要收件人，需要处理告警的人 | `admin@example.com` |
| `cc` | ❌ 否 | 抄送人，仅供知情的人 | `manager@example.com` |

## 支持的邮件类型

所有 5 种邮件都支持 TO 和 CC：

| 邮件类型 | 说明 | 支持 TO | 支持 CC |
|---------|------|---------|---------|
| 启动通知 | 系统启动时发送 | ✅ | ✅ |
| 告警通知 | 检测到无数据 | ✅ | ✅ |
| 恢复通知 | 数据恢复正常 | ✅ | ✅ |
| 健康检查 | 每天定时发送 | ✅ | ✅ |
| 关闭通知 | 系统关闭时发送 | ✅ | ✅ |

## 使用场景

### 场景 1：小团队
```yaml
to: admin@example.com,backup@example.com
```

### 场景 2：团队 + 管理层
```yaml
to: ops-team@example.com
cc: manager@example.com,director@example.com
```

### 场景 3：主备架构
```yaml
to: primary-ops@example.com
cc: backup-ops@example.com,standby-ops@example.com
```

### 场景 4：跨部门
```yaml
to: dev-ops@example.com,platform-ops@example.com
cc: product-manager@example.com,business-ops@example.com
```

## 格式规则

### ✅ 正确格式
```yaml
# 逗号分隔
to: a@example.com,b@example.com

# 带空格也可以（会自动处理）
to: a@example.com, b@example.com

# 留空 CC（可选）
cc:

# 注释掉 CC
# cc: someone@example.com
```

### ❌ 错误格式
```yaml
# 使用分号
to: a@example.com;b@example.com

# 使用中文逗号
to: a@example.com，b@example.com

# 不是有效邮箱
to: admin
```

## 邮件头示例

配置：
```yaml
to: alice@example.com,bob@example.com
cc: charlie@example.com
```

实际邮件头：
```
From: monitor@example.com
To: alice@example.com, bob@example.com
Cc: charlie@example.com
Subject: [警告] 数据库监控：sync_operation_log 表无新数据
```

## 常见问题

**Q: CC 必须配置吗？**
A: 不是，CC 完全可选。

**Q: 可以配置多少个？**
A: 理论无限制，建议总数不超过 10 个。

**Q: TO 和 CC 有什么区别？**
A: TO 是主要收件人（需采取行动），CC 是抄送人（仅供知情）。

**Q: 如何测试？**
A: 配置后重启应用，检查启动通知邮件。

## 日志输出

### 成功发送
```
数据缺失警告邮件已发送至: admin@example.com,manager@example.com
```

### DEBUG 级别（显示 CC）
```
已添加抄送人: supervisor@example.com,ceo@example.com
数据缺失警告邮件已发送至: admin@example.com,manager@example.com
```

## 技术实现

- **TO 设置**：`message.setTo(String[] addresses)`
- **CC 设置**：`message.setCc(String[] addresses)`
- **自动处理**：去除空格、分割逗号
- **向后兼容**：CC 为空时正常工作

## 相关文档

- [MULTI_RECIPIENT_CC.md](MULTI_RECIPIENT_CC.md) - 完整功能说明
- [README.md](README.md) - 项目总览
- [EMAIL_TROUBLESHOOTING.md](EMAIL_TROUBLESHOOTING.md) - 邮件故障排查

---

**更新时间**：2026-02-12
