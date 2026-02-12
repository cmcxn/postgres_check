# 启动通知功能说明

## 功能概述

系统启动时，会自动发送一封邮件通知，告知管理员监控系统已经成功启动。

## 配置示例

在 `application.yml` 文件中配置：

```yaml
app:
  notification:
    # 基本邮件配置
    enabled: true
    from: monitor@example.com
    to: admin@example.com
    
    # 启动通知配置
    startup-enabled: true                          # 是否启用启动通知
    startup-subject: "[通知] 数据库监控系统已启动"  # 启动邮件主题
    startup-body-template: |                       # 启动邮件内容模板
      您好，
      
      数据库监控系统已成功启动。
      
      启动时间：%s
      监控表名：%s
      检查频率：每 %d 分钟
      
      系统将定时检查数据库中是否有新增数据，如发现异常将及时通知您。
      
      此邮件由系统自动发送，请勿回复。
```

## 工作流程

```
1. Spring Boot 应用启动
   ↓
2. 应用初始化完成
   ↓
3. 触发 ApplicationReadyEvent 事件
   ↓
4. StartupNotificationService 监听到事件
   ↓
5. 调用 EmailNotificationService.sendStartupNotification()
   ↓
6. 检查配置（startup-enabled, to, startup-body-template）
   ↓
7. 格式化邮件内容（填充时间、表名、频率）
   ↓
8. 发送邮件
   ↓
9. 记录日志
```

## 邮件内容说明

启动通知邮件包含以下信息：
- **启动时间**：系统启动的具体时间
- **监控表名**：正在监控的数据库表（sync_operation_log）
- **检查频率**：定时检查的时间间隔（默认5分钟）

## 日志输出

应用启动时，会在日志中看到：

```
2024-01-01 09:55:00 - 应用启动完成，准备发送启动通知邮件
2024-01-01 09:55:00 - 启动通知邮件已发送至: admin@example.com
```

如果发送失败，会记录错误日志：

```
2024-01-01 09:55:00 - 应用启动完成，准备发送启动通知邮件
2024-01-01 09:55:00 - 启动通知失败：收件人配置为空，请在配置文件中设置 app.notification.to
```

或

```
2024-01-01 09:55:00 - 应用启动完成，准备发送启动通知邮件
2024-01-01 09:55:00 - 发送启动通知邮件失败: javax.mail.AuthenticationFailedException
```

## 如何禁用启动通知

如果不需要启动通知，可以在配置文件中禁用：

```yaml
app:
  notification:
    startup-enabled: false  # 禁用启动通知
```

## 常见问题

### Q1: 为什么收不到启动通知邮件？

**可能的原因：**
1. `startup-enabled` 设置为 `false`
2. `notification.to` 未配置或配置错误
3. `startup-body-template` 未配置或为空
4. SMTP 配置错误（服务器地址、端口、认证信息）
5. 网络问题或防火墙阻止

**解决方法：**
1. 检查配置文件中的所有邮件相关配置
2. 查看应用日志，确认错误信息
3. 测试 SMTP 服务器是否可达
4. 验证邮箱密码或授权码是否正确

### Q2: 启动通知会影响应用启动速度吗？

不会显著影响。发送邮件是在应用完全启动后执行的，不会阻塞应用的初始化过程。即使邮件发送失败，也不会影响应用的正常运行。

### Q3: 可以发送给多个收件人吗？

可以。在 `notification.to` 配置中使用逗号分隔多个邮箱地址：

```yaml
app:
  notification:
    to: admin1@example.com,admin2@example.com,admin3@example.com
```

### Q4: 如何自定义邮件内容？

修改 `startup-body-template` 配置。注意保持占位符 `%s`（字符串）和 `%d`（数字）的顺序和数量：

```yaml
startup-body-template: |
  自定义内容：
  系统启动于：%s
  监控表：%s  
  检查间隔：%d 分钟
```

占位符对应的值：
- 第1个 `%s`：启动时间（格式：yyyy-MM-dd HH:mm:ss）
- 第2个 `%s`：监控的表名
- 第3个 `%d`：时间窗口分钟数

## 代码实现位置

- **配置类**：`src/main/java/com/database/postgrescheck/config/AppConfig.java`
- **邮件服务**：`src/main/java/com/database/postgrescheck/service/EmailNotificationService.java`
- **启动服务**：`src/main/java/com/database/postgrescheck/service/StartupNotificationService.java`
- **配置模板**：`src/main/resources/application.yml.template`

## 测试方法

1. 确保已正确配置数据库和邮件参数
2. 启动应用：`mvn spring-boot:run` 或 `java -jar postgres-check-1.0.0.jar`
3. 查看控制台或日志文件，确认启动通知已发送
4. 检查收件人邮箱，确认收到启动通知邮件

---

**注意**：首次使用前，请确保已经从 `application.yml.template` 复制并配置好 `application.yml` 文件。
