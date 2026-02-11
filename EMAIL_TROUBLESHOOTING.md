# 邮件配置故障排查指南

## 问题现象

应用启动时出现以下错误：

```
发送启动通知邮件失败
org.springframework.mail.MailSendException: Mail server connection failed
Caused by: java.net.SocketTimeoutException: Read timed out
```

## 问题原因

1. **SMTP 服务器配置错误**：服务器地址、端口不正确
2. **认证信息错误**：用户名、密码或授权码不正确
3. **网络问题**：无法连接到 SMTP 服务器
4. **防火墙阻止**：防火墙阻止了 SMTP 端口

## 新增功能

### 1. 应用启动时自动验证邮件配置

应用启动时会自动检查邮件配置并输出详细的诊断信息：

```
========================================
开始验证邮件配置...
========================================
✓ SMTP 服务器: smtp.qq.com
✓ SMTP 端口: 587
✓ SMTP 用户名: abc***@qq.com
✓ 发件人: test@example.com
✓ 收件人: admin@example.com
正在测试 SMTP 服务器连接...
✓ SMTP 服务器连接成功
========================================
```

如果配置有问题，会显示警告：

```
⚠️  SMTP 服务器未配置或使用默认值: smtp.example.com
   请在 application.yml 中设置 spring.mail.host
```

### 2. 异步发送启动通知

启动通知现在采用异步方式发送，不会阻塞应用启动。即使邮件发送失败，应用也能正常运行。

### 3. 详细的错误诊断

当邮件发送失败时，会输出详细的诊断信息：

```
=== 邮件配置诊断信息 ===
请检查 application.yml 中的以下配置项：
1. spring.mail.host - SMTP 服务器地址
2. spring.mail.port - SMTP 端口（587/TLS 或 465/SSL）
3. spring.mail.username - 发件邮箱账号
4. spring.mail.password - 邮箱密码或授权码
5. app.notification.from - 发件人地址
6. app.notification.to - 收件人地址

常见问题：
- QQ/163邮箱需要使用授权码，不是登录密码
- 检查防火墙是否阻止 SMTP 端口
- 确认 SMTP 服务器地址和端口正确
- 某些邮箱需要开启 SMTP 服务
=========================
```

## 配置检查步骤

### 1. 检查 SMTP 基本配置

打开 `src/main/resources/application.yml`，检查以下配置：

```yaml
spring:
  mail:
    host: smtp.qq.com          # SMTP 服务器地址
    port: 587                  # SMTP 端口
    username: your@qq.com      # 邮箱账号
    password: abcd1234efgh     # 授权码（不是登录密码！）
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

### 2. 检查通知配置

```yaml
app:
  notification:
    enabled: true                    # 启用通知
    from: your@qq.com               # 发件人（通常与 username 相同）
    to: recipient@example.com        # 收件人
    startup-enabled: true            # 启用启动通知
```

### 3. 常见邮箱配置参考

#### QQ 邮箱
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587  # 或 465（SSL）
    username: your@qq.com
    password: abcdefghijklmnop  # 授权码，在 QQ 邮箱设置中生成
```

**获取 QQ 邮箱授权码：**
1. 登录 QQ 邮箱网页版
2. 设置 → 账户 → POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV 服务
3. 开启 IMAP/SMTP 服务
4. 生成授权码

#### 163 邮箱
```yaml
spring:
  mail:
    host: smtp.163.com
    port: 465
    username: your@163.com
    password: your_auth_code  # 客户端授权密码
```

#### Gmail
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your@gmail.com
    password: your_app_password  # 应用专用密码
```

### 4. 测试网络连接

使用命令行测试是否可以连接到 SMTP 服务器：

```bash
# 测试端口是否可达
telnet smtp.qq.com 587

# 或使用 nc
nc -zv smtp.qq.com 587
```

### 5. 检查防火墙

确保防火墙允许出站连接到 SMTP 端口（587 或 465）。

## 常见问题解答

### Q1: 提示认证失败 (AuthenticationFailedException)

**原因**：用户名或密码错误

**解决方法**：
1. 确认使用的是授权码，不是登录密码（QQ、163 邮箱）
2. 检查用户名是否为完整的邮箱地址
3. 重新生成授权码并更新配置

### Q2: 连接超时 (SocketTimeoutException)

**原因**：无法连接到 SMTP 服务器

**解决方法**：
1. 检查 SMTP 服务器地址和端口是否正确
2. 测试网络连接：`telnet smtp.qq.com 587`
3. 检查防火墙设置
4. 如果在内网环境，可能需要配置代理

### Q3: SSL/TLS 相关错误

**原因**：SSL/TLS 配置不匹配

**解决方法**：
- 端口 587 使用 STARTTLS：
  ```yaml
  port: 587
  properties:
    mail:
      smtp:
        starttls:
          enable: true
          required: true
  ```
- 端口 465 使用 SSL：
  ```yaml
  port: 465
  properties:
    mail:
      smtp:
        ssl:
          enable: true
  ```

### Q4: 如何临时禁用启动通知？

在 `application.yml` 中设置：

```yaml
app:
  notification:
    startup-enabled: false  # 禁用启动通知
```

或者禁用所有邮件通知：

```yaml
app:
  notification:
    enabled: false  # 禁用所有邮件通知
```

## 日志说明

### 成功的日志输出

```
应用启动完成，准备发送启动通知邮件
✓ SMTP 服务器连接成功
启动通知邮件已发送至: admin@example.com
```

### 失败的日志输出

```
应用启动完成，准备发送启动通知邮件
❌ SMTP 服务器连接失败: Authentication failed
   请检查:
   1. SMTP 服务器地址和端口是否正确
   2. 用户名和密码/授权码是否正确
   3. 网络是否可以访问 SMTP 服务器
   4. 防火墙是否允许 SMTP 端口
   5. 如使用 QQ/163 邮箱，确认使用的是授权码而非登录密码
发送启动通知邮件失败。请检查以下配置：
=== 邮件配置诊断信息 ===
...
```

## 进一步帮助

如果按照上述步骤仍无法解决问题，请：

1. 查看完整的错误日志：`logs/postgres-check.log`
2. 确认邮箱服务商的 SMTP 设置文档
3. 尝试使用其他邮件客户端（如 Thunderbird）测试 SMTP 连接
4. 联系网络管理员确认防火墙规则

## 相关文档

- [README.md](README.md) - 项目总体说明
- [QUICKSTART.md](QUICKSTART.md) - 快速开始指南
- [STARTUP_NOTIFICATION.md](STARTUP_NOTIFICATION.md) - 启动通知功能说明
