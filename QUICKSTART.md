# 快速开始指南

## 一、准备工作

### 1. 安装要求
- JDK 8 或更高版本
- Maven 3.6+
- PostgreSQL 14 数据库
- SMTP 邮件服务（QQ邮箱、Gmail、163邮箱等）

### 2. 检查环境
```bash
# 检查 Java 版本
java -version

# 检查 Maven 版本
mvn -version
```

## 二、配置步骤

### 1. 获取邮箱授权码

**QQ邮箱：**
1. 登录 QQ 邮箱网页版
2. 设置 -> 账户 -> POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务
3. 开启 SMTP 服务
4. 生成授权码并保存

**163邮箱：**
1. 登录 163 邮箱网页版
2. 设置 -> POP3/SMTP/IMAP -> 开启 SMTP 服务
3. 生成客户端授权密码

**Gmail：**
1. 开启两步验证
2. 生成应用专用密码

### 2. 配置数据库和邮件

**首先从模板创建配置文件：**

```bash
cp src/main/resources/application.yml.template src/main/resources/application.yml
```

然后编辑 `src/main/resources/application.yml` 文件：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb  # 修改数据库地址
    username: postgres                          # 修改数据库用户名
    password: 123456                            # 修改数据库密码

  mail:
    host: smtp.qq.com                          # QQ邮箱SMTP服务器
    port: 587                                  # 端口
    username: your_qq@qq.com                   # 你的QQ邮箱
    password: abcd1234efgh5678                 # 你的授权码

app:
  notification:
    from: your_qq@qq.com                       # 发件人（与上面相同）
    to: recipient@example.com                  # 收件人
    startup-enabled: true                      # 启用启动通知（可选）
```

### 3. 自定义监控参数（可选）

如需修改监控频率和时间窗口，或禁用启动通知：

```yaml
app:
  monitor:
    cron: "0 */10 * * * ?"     # 改为每10分钟检查一次
    time-window-minutes: 10     # 改为检查最近10分钟
  
  notification:
    startup-enabled: false      # 禁用启动通知（如不需要）
```

## 三、运行应用

### 方式一：使用 Maven 运行（开发测试）

```bash
# 进入项目目录
cd postgres_check

# 运行应用
mvn spring-boot:run
```

### 方式二：打包后运行（生产环境）

```bash
# 1. 编译打包
mvn clean package

# 2. 运行 JAR 包
java -jar target/postgres-check-1.0.0.jar

# 3. 后台运行（Linux/Mac）
nohup java -jar target/postgres-check-1.0.0.jar > logs/app.log 2>&1 &
```

### 方式三：使用 systemd 服务（Linux生产环境）

创建服务文件 `/etc/systemd/system/postgres-check.service`：

```ini
[Unit]
Description=PostgreSQL Database Monitor
After=network.target

[Service]
Type=simple
User=your_user
WorkingDirectory=/path/to/postgres_check
ExecStart=/usr/bin/java -jar /path/to/postgres_check/target/postgres-check-1.0.0.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：
```bash
sudo systemctl daemon-reload
sudo systemctl start postgres-check
sudo systemctl enable postgres-check  # 开机自启
sudo systemctl status postgres-check  # 查看状态
```

## 四、验证运行

### 1. 查看日志

应用启动后，查看日志确认运行状态：

```bash
# 查看实时日志
tail -f logs/postgres-check.log

# 或查看控制台输出
```

### 2. 预期日志输出

**应用启动时：**
```
2024-01-01 09:55:00 - 应用启动完成，准备发送启动通知邮件
2024-01-01 09:55:00 - 启动通知邮件已发送至: recipient@example.com
```

**正常情况（有新数据）：**
```
2024-01-01 10:00:00 - ==================== 开始定时检查 ====================
2024-01-01 10:00:00 - 开始检查 sync_operation_log 表，时间窗口：最近 5 分钟
2024-01-01 10:00:00 - 正常：sync_operation_log 表在过去 5 分钟内有 10 条新增数据
2024-01-01 10:00:00 - ==================== 检查完成 ====================
```

**异常情况（无新数据）：**
```
2024-01-01 10:05:00 - ==================== 开始定时检查 ====================
2024-01-01 10:05:00 - 开始检查 sync_operation_log 表，时间窗口：最近 5 分钟
2024-01-01 10:05:00 - 警告：sync_operation_log 表在过去 5 分钟内没有新增数据
2024-01-01 10:05:00 - 最新记录时间: 2024-01-01 09:55:00
2024-01-01 10:05:00 - 数据缺失警告邮件已发送至: recipient@example.com
2024-01-01 10:05:00 - ==================== 检查完成 ====================
```

## 五、常见问题

### 1. 邮件发送失败

**错误信息：**
```
发送邮件失败: AuthenticationFailedException
```

**解决方法：**
- 检查邮箱用户名和授权码是否正确
- 确认已开启 SMTP 服务
- QQ邮箱必须使用授权码，不能使用登录密码

### 2. 数据库连接失败

**错误信息：**
```
Connection refused
```

**解决方法：**
- 检查 PostgreSQL 服务是否运行：`systemctl status postgresql`
- 检查 `pg_hba.conf` 是否允许连接
- 检查防火墙是否开放 5432 端口

### 3. 表不存在

**错误信息：**
```
relation "sync_operation_log" does not exist
```

**解决方法：**
- 确认表名拼写正确
- 检查是否在正确的 schema 中
- 如果表在非 public schema，需要修改配置：
  ```yaml
  spring:
    jpa:
      properties:
        hibernate:
          default_schema: your_schema
  ```

## 六、测试验证

### 1. 手动触发测试

可以暂时修改 cron 表达式为更频繁的检查，例如每分钟：

```yaml
app:
  monitor:
    cron: "0 * * * * ?"  # 每分钟执行一次
```

### 2. 插入测试数据

```sql
-- 插入测试数据
INSERT INTO sync_operation_log (id, table_name, operation, changed_at)
VALUES (1, 'test_table', 'INSERT', NOW());

-- 查询最新数据
SELECT * FROM sync_operation_log ORDER BY changed_at DESC LIMIT 5;
```

### 3. 验证告警

停止数据写入 5 分钟以上，应该会收到告警邮件。

## 七、停止应用

### 直接运行模式
按 `Ctrl + C` 停止

### 后台运行模式
```bash
# 查找进程ID
ps aux | grep postgres-check

# 停止进程
kill <PID>
```

### systemd 服务模式
```bash
sudo systemctl stop postgres-check
```

## 八、下一步

- 根据实际需求调整监控频率和时间窗口
- 配置多个收件人接收告警
- 自定义邮件模板内容
- 查看日志文件了解详细运行情况

## 需要帮助？

如有问题，请查看：
1. 日志文件：`logs/postgres-check.log`
2. README.md 完整文档
3. 项目 Issues 页面
