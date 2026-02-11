# 邮件配置对比分析

## 你的配置（有问题）

```yaml
#支持邮件
mail:
    default :
        from : 'chenmins@88.com'
    host : 'smtp.88.com'
    port : '465'
    username : 'chenmins@88.com'
    password : 'xxxxx'
    props :
        mail.smtp.auth : 'true'
        mail.smtp.socketFactory.port : '465'
        mail.smtp.socketFactory.class : 'javax.net.ssl.SSLSocketFactory'
        mail.smtp.socketFactory.fallback : 'false'
```

## 问题分析

### ❌ 问题 1: 缺少 `spring:` 前缀
Spring Boot 的邮件配置必须在 `spring.mail` 下，不是 `mail`

### ❌ 问题 2: 错误的 `default` 嵌套
`default.from` 不是标准的 Spring Boot 配置，应该在应用配置中设置

### ❌ 问题 3: `props` 应该是 `properties`
Spring Boot 使用 `properties` 而不是 `props`

### ❌ 问题 4: 属性路径不完整
属性应该在 `properties.mail.smtp.*` 下，而不是直接使用点号作为键名

### ❌ 问题 5: 端口应该是数字而非字符串
`port: '465'` 应该是 `port: 465`（不带引号）

## 正确的配置（SSL 465 端口）

```yaml
spring:
  mail:
    host: smtp.88.com
    port: 465
    username: chenmins@88.com
    password: xxxxx
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
          socketFactory:
            port: 465
            class: javax.net.ssl.SSLSocketFactory
            fallback: false
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

app:
  notification:
    from: chenmins@88.com
    to: recipient@example.com  # 修改为实际收件人
    startup-enabled: true
    startup-subject: "[通知] 数据库监控系统已启动"
    startup-body-template: |
      您好，
      
      数据库监控系统已成功启动。
      
      启动时间：%s
      监控表名：%s
      检查频率：每 %d 分钟
      
      系统将定时检查数据库中是否有新增数据，如发现异常将及时通知您。
      
      此邮件由系统自动发送，请勿回复。
```

## 完整对比表

| 配置项 | 你的配置 ❌ | 正确配置 ✅ |
|--------|------------|------------|
| 根路径 | `mail:` | `spring.mail:` |
| 发件人设置 | `mail.default.from` | `app.notification.from` |
| 属性配置 | `props:` | `properties:` |
| 端口类型 | `'465'` (字符串) | `465` (数字) |
| SSL配置路径 | `props.mail.smtp.*` | `properties.mail.smtp.*` |
| 属性嵌套 | 使用点号作为键 | 正确的 YAML 嵌套 |

## 配置示例对比

### 错误示例（你的配置方式）
```yaml
props:
    mail.smtp.auth: 'true'  # ❌ 点号作为键名
```

### 正确示例
```yaml
properties:
  mail:
    smtp:
      auth: true  # ✅ YAML 嵌套结构
```

## 快速修复方案

### 方案 1：完整配置文件

创建或修改 `src/main/resources/application.yml`：

```yaml
spring:
  application:
    name: postgres-check
  
  # 数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false
    hibernate:
      ddl-auto: none
  
  # 邮件配置（SSL 465端口）
  mail:
    host: smtp.88.com
    port: 465
    username: chenmins@88.com
    password: xxxxx
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
          socketFactory:
            port: 465
            class: javax.net.ssl.SSLSocketFactory
            fallback: false
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

# 应用配置
app:
  monitor:
    cron: "0 */5 * * * ?"
    time-window-minutes: 5
    table-name: sync_operation_log
  
  notification:
    enabled: true
    from: chenmins@88.com
    to: admin@example.com  # 改为你的收件人
    subject: "[警告] 数据库监控：sync_operation_log 表无新数据"
    body-template: |
      您好，
      
      数据库监控系统检测到 sync_operation_log 表在过去 %d 分钟内没有新增数据。
      
      检测时间：%s
      表名：%s
      时间窗口：最近 %d 分钟
      
      请及时检查数据同步服务是否正常运行。
      
      此邮件由系统自动发送，请勿回复。
    
    # 启动通知配置
    startup-enabled: true
    startup-subject: "[通知] 数据库监控系统已启动"
    startup-body-template: |
      您好，
      
      数据库监控系统已成功启动。
      
      启动时间：%s
      监控表名：%s
      检查频率：每 %d 分钟
      
      系统将定时检查数据库中是否有新增数据，如发现异常将及时通知您。
      
      此邮件由系统自动发送，请勿回复。

logging:
  level:
    com.database.postgrescheck: INFO
    org.springframework: WARN
  file:
    name: logs/postgres-check.log
```

### 方案 2：如果使用 TLS (587端口) 更推荐

```yaml
spring:
  mail:
    host: smtp.88.com
    port: 587  # TLS 端口
    username: chenmins@88.com
    password: xxxxx
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
```

## 验证配置

启动应用后，你会看到配置验证信息：

### 成功示例
```
========================================
开始验证邮件配置...
========================================
✓ SMTP 服务器: smtp.88.com
✓ SMTP 端口: 465
✓ SMTP 用户名: che***@88.com
✓ 发件人: chenmins@88.com
✓ 收件人: admin@example.com
正在测试 SMTP 服务器连接...
✓ SMTP 服务器连接成功
========================================
```

## 常见错误对照

| 错误配置 | 结果 | 正确配置 |
|---------|------|---------|
| `mail:` | ❌ 不会被识别 | `spring.mail:` |
| `props:` | ❌ 不会被识别 | `properties:` |
| `port: '465'` | ⚠️ 可能工作但不规范 | `port: 465` |
| `mail.smtp.auth: 'true'` | ❌ 键名错误 | 使用嵌套结构 |

## 测试你的配置

1. **复制上面的完整配置**到 `application.yml`
2. **修改收件人**：将 `to: admin@example.com` 改为实际收件人
3. **启动应用**：`mvn spring-boot:run`
4. **查看日志**：观察配置验证结果

## SSL vs TLS 选择

- **端口 465 (SSL)**：直接使用 SSL 加密连接
  ```yaml
  port: 465
  properties.mail.smtp.ssl.enable: true
  ```

- **端口 587 (TLS)**：使用 STARTTLS 升级连接（推荐）
  ```yaml
  port: 587
  properties.mail.smtp.starttls.enable: true
  ```

## 需要帮助？

如果修改后仍有问题，应用会自动提供诊断信息。查看：
- 应用启动日志中的配置验证部分
- [EMAIL_TROUBLESHOOTING.md](EMAIL_TROUBLESHOOTING.md) 完整故障排查指南
