# PostgreSQL 监控系统 - 功能速查表

## 📧 邮件通知类型

| 通知类型 | 触发时机 | 配置键 | 默认状态 |
|---------|---------|--------|---------|
| 🚀 启动通知 | 应用启动完成 | `startup-enabled` | ✅ 启用 |
| ⚠️ 告警通知 | 首次检测到无数据 | `enabled` | ✅ 启用 |
| ✅ 恢复通知 | 数据从故障恢复 | `recovery-enabled` | ✅ 启用 |
| 💚 健康检查 | 每天定时（8:00） | `daily-health-check-enabled` | ✅ 启用 |
| 🛑 关闭通知 | 应用正常关闭 | `shutdown-enabled` | ✅ 启用 |

## 🔄 监控状态流转

```
正常状态 ────────> 告警状态 ────────> 正常状态
         (无数据)           (数据恢复)
         发送告警           发送恢复通知
         记录开始时间       计算故障时长
```

## ⏰ 定时任务

| 任务 | 默认频率 | 配置键 |
|-----|---------|--------|
| 数据检查 | 每 5 分钟 | `app.monitor.cron` |
| 健康检查 | 每天 8:00 | `app.notification.daily-health-check-cron` |

## 📝 配置示例

### 最小配置
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your@qq.com
    password: your_auth_code

app:
  notification:
    from: your@qq.com
    to: admin@example.com
```

### 自定义健康检查时间
```yaml
app:
  notification:
    daily-health-check-cron: "0 0 9 * * ?"  # 改为 9:00
```

### 禁用某些通知
```yaml
app:
  notification:
    startup-enabled: false       # 禁用启动通知
    recovery-enabled: false      # 禁用恢复通知
    daily-health-check-enabled: false  # 禁用健康检查
    shutdown-enabled: false      # 禁用关闭通知
```

## 📊 故障时长格式

| 时长 | 显示格式 | 示例 |
|-----|---------|------|
| < 60 分钟 | X 分钟 | "45 分钟" |
| < 24 小时 | X 小时 Y 分钟 | "2 小时 30 分钟" |
| ≥ 24 小时 | X 天 Y 小时 | "1 天 5 小时" |

## 🔍 日志关键词

监控日志中的关键输出：

**进入告警**：
```
系统进入告警状态，告警开始时间: 2024-01-15 09:00:00
```

**告警持续**：
```
系统已在告警状态，跳过重复告警邮件
```

**恢复正常**：
```
系统从告警状态恢复，故障持续时间: 85 分钟
恢复通知邮件已发送至: admin@example.com
```

**每日检查**：
```
==================== 开始每日健康检查 ====================
每日健康检查邮件已发送至: admin@example.com
```

**应用关闭**：
```
应用正在关闭，准备发送关闭通知邮件
关闭通知邮件已发送至: admin@example.com
```

## 🧪 测试方法

### 测试恢复通知
1. 停止数据同步 → 等待 5 分钟 → 收到告警
2. 启动数据同步 → 等待 5 分钟 → 收到恢复通知

### 测试健康检查
- 临时修改 cron: `"0 * * * * ?"` (每分钟执行)
- 或等待到配置的时间

### 测试关闭通知
- 使用 `Ctrl+C` 或 `kill PID` 正常关闭
- ⚠️ `kill -9` 可能无法发送通知

## 🚨 常见问题

**Q: 为什么告警期间没有重复邮件？**
A: 设计如此，避免邮件轰炸。只在首次告警和恢复时发送邮件。

**Q: 健康检查没执行？**
A: 检查 cron 表达式和时区设置。

**Q: 关闭通知没收到？**
A: 检查是否使用 `kill -9`，这会绕过关闭钩子。

**Q: 如何知道系统在告警状态？**
A: 查看日志，搜索 "系统进入告警状态" 或 "系统已在告警状态"。

## 📚 更多文档

- [NEW_FEATURES.md](NEW_FEATURES.md) - 详细功能说明
- [README.md](README.md) - 项目总览
- [QUICKSTART.md](QUICKSTART.md) - 快速开始
- [EMAIL_TROUBLESHOOTING.md](EMAIL_TROUBLESHOOTING.md) - 邮件故障排查

## 🎯 最佳实践

1. ✅ **保持所有通知启用** - 全面掌握系统状态
2. ✅ **配置多个收件人** - 防止单点失效
3. ✅ **定期检查健康邮件** - 确认监控系统运行
4. ✅ **保存关闭通知** - 用于问题追溯
5. ✅ **监控恢复时间** - 优化故障响应流程

---

**提示**: 所有邮件内容都可以自定义，修改 `application.yml` 中的模板即可。
