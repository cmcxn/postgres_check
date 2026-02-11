# 部署指南

本文档提供在不同环境下部署 PostgreSQL 数据库监控工具的详细说明。

## 目录

1. [本地开发环境](#本地开发环境)
2. [Linux 服务器部署](#linux-服务器部署)
3. [Windows 服务器部署](#windows-服务器部署)
4. [Docker 容器部署](#docker-容器部署)
5. [配置文件管理](#配置文件管理)

---

## 本地开发环境

### 使用 Maven 运行

最简单的方式，适合开发和测试：

```bash
# 克隆项目
git clone https://github.com/cmcxn/postgres_check.git
cd postgres_check

# 配置 application.yml
vi src/main/resources/application.yml

# 运行应用
mvn spring-boot:run
```

### 使用 IDE 运行

1. 使用 IntelliJ IDEA 或 Eclipse 打开项目
2. 配置 `src/main/resources/application.yml`
3. 运行 `PostgresCheckApplication.java` 主类

---

## Linux 服务器部署

### 方式一：使用 systemd 服务（推荐）

#### 1. 打包应用

```bash
mvn clean package
```

#### 2. 创建部署目录

```bash
sudo mkdir -p /opt/postgres-check
sudo cp target/postgres-check-1.0.0.jar /opt/postgres-check/
sudo cp src/main/resources/application.yml /opt/postgres-check/
```

#### 3. 修改配置文件

```bash
sudo vi /opt/postgres-check/application.yml
# 根据实际环境修改数据库和邮件配置
```

#### 4. 创建 systemd 服务文件

```bash
sudo vi /etc/systemd/system/postgres-check.service
```

添加以下内容：

```ini
[Unit]
Description=PostgreSQL Database Monitor
After=network.target postgresql.service

[Service]
Type=simple
User=postgres
WorkingDirectory=/opt/postgres-check
ExecStart=/usr/bin/java -jar /opt/postgres-check/postgres-check-1.0.0.jar --spring.config.location=/opt/postgres-check/application.yml
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

# 可选：资源限制
MemoryLimit=512M
CPUQuota=50%

[Install]
WantedBy=multi-user.target
```

#### 5. 启动服务

```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start postgres-check

# 设置开机自启
sudo systemctl enable postgres-check

# 查看服务状态
sudo systemctl status postgres-check

# 查看日志
sudo journalctl -u postgres-check -f
```

#### 6. 服务管理命令

```bash
# 停止服务
sudo systemctl stop postgres-check

# 重启服务
sudo systemctl restart postgres-check

# 禁用开机自启
sudo systemctl disable postgres-check

# 查看最近的日志
sudo journalctl -u postgres-check -n 100
```

### 方式二：使用 nohup 后台运行

```bash
# 创建部署目录
mkdir -p ~/postgres-check
cd ~/postgres-check

# 复制文件
cp /path/to/target/postgres-check-1.0.0.jar .
cp /path/to/src/main/resources/application.yml .

# 修改配置
vi application.yml

# 创建日志目录
mkdir -p logs

# 后台运行
nohup java -jar postgres-check-1.0.0.jar --spring.config.location=./application.yml > logs/app.log 2>&1 &

# 查看进程
ps aux | grep postgres-check

# 停止应用
kill <PID>
```

---

## Windows 服务器部署

### 方式一：使用批处理脚本

#### 1. 创建启动脚本 `start.bat`

```batch
@echo off
title PostgreSQL Database Monitor
cd /d "%~dp0"

echo Starting PostgreSQL Database Monitor...
java -jar postgres-check-1.0.0.jar --spring.config.location=application.yml

pause
```

#### 2. 创建后台启动脚本 `start-background.bat`

```batch
@echo off
cd /d "%~dp0"

start /B java -jar postgres-check-1.0.0.jar --spring.config.location=application.yml > logs\app.log 2>&1

echo PostgreSQL Database Monitor started in background
pause
```

#### 3. 运行

双击 `start.bat` 或 `start-background.bat`

### 方式二：使用 Windows 服务（推荐）

使用 [NSSM (Non-Sucking Service Manager)](https://nssm.cc/) 将应用注册为 Windows 服务：

#### 1. 下载 NSSM

访问 https://nssm.cc/download 下载 NSSM

#### 2. 安装服务

```cmd
# 打开管理员命令行
nssm install PostgresCheck "C:\Program Files\Java\jdk1.8.0_xxx\bin\java.exe"
nssm set PostgresCheck AppParameters "-jar C:\postgres-check\postgres-check-1.0.0.jar --spring.config.location=C:\postgres-check\application.yml"
nssm set PostgresCheck AppDirectory "C:\postgres-check"
nssm set PostgresCheck DisplayName "PostgreSQL Database Monitor"
nssm set PostgresCheck Description "Monitor PostgreSQL sync_operation_log table"
nssm set PostgresCheck Start SERVICE_AUTO_START

# 启动服务
nssm start PostgresCheck

# 查看服务状态
nssm status PostgresCheck
```

#### 3. 服务管理

```cmd
# 停止服务
nssm stop PostgresCheck

# 重启服务
nssm restart PostgresCheck

# 删除服务
nssm remove PostgresCheck confirm
```

---

## Docker 容器部署

### 1. 创建 Dockerfile

在项目根目录创建 `Dockerfile`：

```dockerfile
FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 复制 JAR 文件和配置
COPY target/postgres-check-1.0.0.jar app.jar
COPY src/main/resources/application.yml application.yml

# 创建日志目录
RUN mkdir -p logs

# 暴露端口（如果需要健康检查）
# EXPOSE 8080

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=application.yml"]
```

### 2. 构建镜像

```bash
# 先打包应用
mvn clean package

# 构建 Docker 镜像
docker build -t postgres-check:1.0.0 .
```

### 3. 运行容器

```bash
# 运行容器
docker run -d \
  --name postgres-check \
  --restart unless-stopped \
  -v $(pwd)/application.yml:/app/application.yml \
  -v $(pwd)/logs:/app/logs \
  postgres-check:1.0.0

# 查看日志
docker logs -f postgres-check

# 停止容器
docker stop postgres-check

# 启动容器
docker start postgres-check
```

### 4. 使用 Docker Compose

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  postgres-check:
    image: postgres-check:1.0.0
    container_name: postgres-check
    restart: unless-stopped
    volumes:
      - ./application.yml:/app/application.yml
      - ./logs:/app/logs
    environment:
      - TZ=Asia/Shanghai
    networks:
      - monitor-network

networks:
  monitor-network:
    driver: bridge
```

运行：

```bash
docker-compose up -d
```

---

## 配置文件管理

### 外部配置文件

在生产环境中，建议将配置文件放在应用外部：

```bash
# 使用外部配置文件运行
java -jar postgres-check-1.0.0.jar --spring.config.location=/path/to/application.yml
```

### 环境变量覆盖

可以使用环境变量覆盖配置：

```bash
# 覆盖数据库配置
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/mydb
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=prod_pass

# 覆盖邮件配置
export SPRING_MAIL_HOST=smtp.company.com
export SPRING_MAIL_USERNAME=monitor@company.com
export SPRING_MAIL_PASSWORD=secret

# 运行应用
java -jar postgres-check-1.0.0.jar
```

### 配置文件加密

对于敏感信息，建议使用 Spring Cloud Config 或 Jasypt 加密：

```yaml
# 使用 Jasypt 加密密码
spring:
  datasource:
    password: ENC(encrypted_password_here)
  mail:
    password: ENC(encrypted_password_here)
```

---

## 健康检查

### 添加健康检查端点（可选）

如果需要监控应用状态，可以在 `pom.xml` 中添加 Actuator：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

在 `application.yml` 中配置：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

访问健康检查：

```bash
curl http://localhost:8080/actuator/health
```

---

## 日志管理

### 日志轮转

在 `application.yml` 中配置日志轮转：

```yaml
logging:
  file:
    name: logs/postgres-check.log
    max-size: 10MB      # 单个日志文件最大大小
    max-history: 30     # 保留最近30天的日志
```

### 集中日志管理

可以将日志发送到 ELK、Splunk 等日志平台：

```yaml
logging:
  config: classpath:logback-spring.xml
```

---

## 监控和告警

### 应用监控

1. 使用 Spring Boot Actuator + Prometheus + Grafana
2. 使用 APM 工具（如 New Relic, DataDog）
3. 使用自定义监控脚本

### 进程监控

使用 Supervisor、Monit 等工具监控应用进程：

```bash
# 安装 Supervisor
sudo apt-get install supervisor

# 配置文件
sudo vi /etc/supervisor/conf.d/postgres-check.conf
```

```ini
[program:postgres-check]
command=java -jar /opt/postgres-check/postgres-check-1.0.0.jar
directory=/opt/postgres-check
user=postgres
autostart=true
autorestart=true
stderr_logfile=/var/log/postgres-check.err.log
stdout_logfile=/var/log/postgres-check.out.log
```

---

## 性能优化

### JVM 参数调优

```bash
java -Xms256m -Xmx512m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -Dfile.encoding=UTF-8 \
     -Duser.timezone=Asia/Shanghai \
     -jar postgres-check-1.0.0.jar
```

### 数据库连接池优化

在 `application.yml` 中配置：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

## 故障排查

### 常见问题

1. **端口冲突**：修改 `server.port` 配置
2. **内存不足**：增加 JVM 堆内存 `-Xmx`
3. **数据库连接失败**：检查网络和防火墙
4. **邮件发送失败**：验证 SMTP 配置和授权码

### 调试模式

```bash
# 启用调试日志
java -jar postgres-check-1.0.0.jar --logging.level.root=DEBUG
```

---

## 安全建议

1. 不要在代码中硬编码密码
2. 使用环境变量或配置管理工具管理敏感信息
3. 限制应用运行用户的权限
4. 定期更新依赖包和 Java 版本
5. 使用 HTTPS 和加密存储

---

## 备份和恢复

### 备份配置文件

```bash
# 定期备份配置
cp application.yml application.yml.backup.$(date +%Y%m%d)
```

### 恢复步骤

1. 停止应用
2. 恢复配置文件
3. 重启应用
4. 验证功能

---

## 升级步骤

1. 备份当前版本和配置
2. 下载新版本 JAR
3. 停止旧版本
4. 替换 JAR 文件
5. 启动新版本
6. 验证功能
7. 监控日志

```bash
# 升级脚本示例
#!/bin/bash
systemctl stop postgres-check
cp postgres-check-1.0.0.jar postgres-check-1.0.0.jar.backup
cp postgres-check-1.1.0.jar postgres-check-1.0.0.jar
systemctl start postgres-check
systemctl status postgres-check
```

---

需要更多帮助？请查看 [README.md](README.md) 和 [QUICKSTART.md](QUICKSTART.md)。
