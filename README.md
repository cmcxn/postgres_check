# PostgreSQL 数据库监控工具

一个基于 Spring Boot 2.7 和 JDK 8 的 PostgreSQL 14 数据库监控工具，用于定时检测 `sync_operation_log` 表中的数据变化，并在数据长时间未更新时发送邮件告警。

## 功能特性

- ✅ 定时检测指定表的数据更新情况
- ✅ 检查最近 N 分钟内是否有新增数据（默认 5 分钟）
- ✅ 无新数据时自动发送邮件告警
- ✅ 系统启动时发送通知邮件
- ✅ 支持 SMTP 邮件服务
- ✅ 灵活的配置文件，所有参数可自定义
- ✅ 详细的日志记录

## 技术栈

- JDK 8
- Spring Boot 2.7.18
- Spring Data JPA
- PostgreSQL 14
- Spring Mail (SMTP)
- Maven

## 快速开始

### 1. 环境要求

- JDK 8 或更高版本
- Maven 3.6+
- PostgreSQL 14 数据库
- SMTP 邮件服务（如 Gmail, QQ 邮箱, 企业邮箱等）

### 2. 数据库准备

确保你的 PostgreSQL 数据库中存在 `sync_operation_log` 表：

```sql
CREATE TABLE "public"."sync_operation_log" (
  "id" int4 NOT NULL,
  "table_name" text,
  "primary_key_value" text,
  "operation" text,
  "changed_columns" text,
  "old_values" jsonb,
  "new_values" jsonb,
  "changed_at" timestamp(6) NOT NULL DEFAULT now(),
  PRIMARY KEY ("id")
);
```

### 3. 配置应用

**重要：首先需要从模板创建配置文件**

```bash
# 复制配置模板
cp src/main/resources/application.yml.template src/main/resources/application.yml
```

然后编辑 `src/main/resources/application.yml` 文件，配置以下参数：

#### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database  # 数据库地址
    username: your_username                              # 数据库用户名
    password: your_password                              # 数据库密码
```

#### 邮件服务配置

```yaml
spring:
  mail:
    host: smtp.example.com                  # SMTP 服务器地址
    port: 587                               # SMTP 端口
    username: your_email@example.com        # 发件邮箱
    password: your_email_password           # 邮箱密码或授权码
```

**常用 SMTP 服务器配置示例：**

- **QQ 邮箱**：`smtp.qq.com`，端口 `587` 或 `465`，需要使用授权码
- **Gmail**：`smtp.gmail.com`，端口 `587` 或 `465`，需要开启"不太安全的应用"访问
- **163 邮箱**：`smtp.163.com`，端口 `465` 或 `994`
- **企业邮箱**：根据你的邮件服务商提供的信息配置

#### 监控配置

```yaml
app:
  monitor:
    cron: "0 */5 * * * ?"           # 检查间隔（每5分钟）
    time-window-minutes: 5          # 时间窗口（分钟）
    table-name: sync_operation_log  # 监控的表名
  
  notification:
    enabled: true                              # 是否启用邮件通知
    from: your_email@example.com               # 发件人
    to: recipient@example.com                  # 收件人（多个用逗号分隔）
    subject: "[警告] 数据库监控：sync_operation_log 表无新数据"
    
    # 启动通知配置
    startup-enabled: true                      # 是否启用启动通知
    startup-subject: "[通知] 数据库监控系统已启动"
```

### 4. 构建和运行

#### 使用 Maven 构建

```bash
mvn clean package
```

#### 运行应用

```bash
java -jar target/postgres-check-1.0.0.jar
```

或者使用 Maven 直接运行：

```bash
mvn spring-boot:run
```

## 配置说明

### Cron 表达式说明

`app.monitor.cron` 使用标准的 Cron 表达式，格式为：`秒 分 时 日 月 星期`

示例：
- `0 */5 * * * ?`：每 5 分钟执行一次
- `0 */10 * * * ?`：每 10 分钟执行一次
- `0 0 * * * ?`：每小时执行一次
- `0 0 0 * * ?`：每天 0 点执行一次

### 邮件模板自定义

可以在 `application.yml` 中自定义邮件内容模板：

```yaml
app:
  notification:
    body-template: |
      您好，
      
      数据库监控系统检测到 sync_operation_log 表在过去 %d 分钟内没有新增数据。
      
      检测时间：%s
      表名：%s
      时间窗口：最近 %d 分钟
      
      请及时检查数据同步服务是否正常运行。
      
      此邮件由系统自动发送，请勿回复。
```

模板中的 `%d` 和 `%s` 将被自动替换为实际值。

## 工作原理

1. **启动通知**：应用启动完成后，自动发送启动通知邮件（可配置是否启用）
2. **定时任务**：根据配置的 Cron 表达式定时执行检查任务（默认每 5 分钟）
3. **数据检查**：查询 `sync_operation_log` 表，统计最近 N 分钟内 `changed_at` 字段的记录数
4. **告警判断**：如果记录数为 0，说明数据未更新
5. **邮件通知**：发送告警邮件到配置的收件人邮箱

## 日志

应用运行日志保存在 `logs/postgres-check.log` 文件中，包含：
- 应用启动通知
- 定时任务执行记录
- 数据检查结果
- 邮件发送状态
- 错误信息

## 常见问题

### 1. 邮件发送失败

当遇到邮件发送失败的问题时，应用会自动进行配置验证并输出诊断信息。请查看：

- **详细排查指南**：[EMAIL_TROUBLESHOOTING.md](EMAIL_TROUBLESHOOTING.md)
- 应用启动时会自动检查 SMTP 配置
- 错误日志会包含详细的诊断信息和修复建议

**快速检查清单**：
- 检查 SMTP 配置是否正确
- 确认邮箱密码或授权码是否正确（QQ/163 邮箱需要授权码）
- 某些邮箱需要开启 SMTP 服务
- 检查防火墙是否阻止了 SMTP 端口
- 使用 `telnet smtp.qq.com 587` 测试连接

### 2. 数据库连接失败

- 检查数据库地址、端口、用户名、密码是否正确
- 确认 PostgreSQL 服务是否正在运行
- 检查防火墙和网络连接

### 3. 定时任务不执行

- 检查 Cron 表达式是否正确
- 查看日志文件确认是否有错误信息

## 许可证

本项目采用 MIT 许可证。
