# 项目总结

## 项目概述

本项目实现了一个 PostgreSQL 14 数据库监控工具，用于定时检测 `sync_operation_log` 表中的数据变化，并在数据长时间未更新时发送邮件告警。

## 技术栈

- **JDK**: 8 (兼容 1.8)
- **Spring Boot**: 2.7.18
- **构建工具**: Maven 3.6+
- **数据库**: PostgreSQL 14
- **ORM**: Spring Data JPA (Hibernate)
- **邮件服务**: Spring Mail (SMTP)

## 核心功能

### 1. 定时监控
- 基于 Spring `@Scheduled` 注解实现定时任务
- 默认每 5 分钟执行一次检查
- 可通过 Cron 表达式灵活配置检查频率

### 2. 数据检测
- 查询 `sync_operation_log` 表
- 检查最近 N 分钟内（默认 5 分钟）是否有新数据
- 基于 `changed_at` 字段时间戳判断

### 3. 邮件告警
- 当检测到无新数据时自动发送邮件
- 支持 SMTP 协议
- 支持多收件人（逗号分隔）
- 可自定义邮件主题和内容模板

### 4. 配置管理
- 所有配置参数集中在 `application.yml` 文件
- 支持数据库连接配置
- 支持 SMTP 邮件服务配置
- 支持监控参数配置（时间窗口、检查频率等）

## 项目结构

```
postgres_check/
├── pom.xml                                           # Maven 配置文件
├── .gitignore                                        # Git 忽略文件
├── README.md                                         # 项目说明文档
├── QUICKSTART.md                                     # 快速开始指南
├── DEPLOYMENT.md                                     # 部署指南
├── sql/
│   └── create_table.sql                             # 数据库表创建脚本
└── src/
    └── main/
        ├── java/com/database/postgrescheck/
        │   ├── PostgresCheckApplication.java        # Spring Boot 主类
        │   ├── config/
        │   │   └── AppConfig.java                   # 应用配置类
        │   ├── entity/
        │   │   └── SyncOperationLog.java            # 实体类
        │   ├── repository/
        │   │   └── SyncOperationLogRepository.java  # 数据访问层
        │   ├── service/
        │   │   ├── DatabaseMonitorService.java      # 监控服务
        │   │   └── EmailNotificationService.java    # 邮件服务
        │   └── scheduler/
        │       └── MonitorScheduler.java            # 定时任务
        └── resources/
            └── application.yml.template              # 配置文件模板
```

## 关键类说明

### 1. PostgresCheckApplication
- Spring Boot 应用主入口
- 启用 `@EnableScheduling` 注解以支持定时任务

### 2. SyncOperationLog (实体类)
- 映射 `sync_operation_log` 表
- 包含所有表字段的 Java 属性
- 使用 JPA 注解进行 ORM 映射
- ID 字段使用 `@GeneratedValue` 策略，匹配 PostgreSQL 的 SERIAL 类型

### 3. SyncOperationLogRepository (数据访问层)
- 继承 `JpaRepository` 接口
- 提供自定义查询方法：
  - `countByChangedAtAfter`: 统计指定时间后的记录数
  - `findLatestChangedAt`: 查找最新记录的时间

### 4. DatabaseMonitorService (监控服务)
- 核心业务逻辑
- 执行数据检查
- 调用邮件服务发送告警

### 5. EmailNotificationService (邮件服务)
- 使用 Spring Mail 发送邮件
- 支持 SMTP 认证
- 包含配置验证（空值检查）
- 格式化邮件内容

### 6. MonitorScheduler (定时任务)
- 使用 `@Scheduled` 注解配置定时执行
- 从配置文件读取 Cron 表达式
- 调用监控服务执行检查

### 7. AppConfig (配置类)
- 使用 `@ConfigurationProperties` 绑定配置
- 包含监控配置和通知配置两个内部类

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database
    username: your_username
    password: your_password
```

### 邮件配置
```yaml
spring:
  mail:
    host: smtp.example.com      # SMTP 服务器
    port: 587                   # 端口
    username: your_email@example.com
    password: your_password
```

### 监控配置
```yaml
app:
  monitor:
    cron: "0 */5 * * * ?"       # 每 5 分钟执行
    time-window-minutes: 5       # 检查最近 5 分钟
    table-name: sync_operation_log
```

### 通知配置
```yaml
app:
  notification:
    enabled: true
    from: sender@example.com
    to: recipient1@example.com,recipient2@example.com
    subject: "[警告] 数据库监控告警"
    body-template: |
      邮件内容模板...
```

## 使用流程

### 1. 环境准备
- 安装 JDK 8
- 安装 Maven
- 准备 PostgreSQL 14 数据库
- 准备 SMTP 邮件服务

### 2. 数据库初始化
```bash
psql -U postgres -d your_database -f sql/create_table.sql
```

### 3. 配置应用
```bash
cp src/main/resources/application.yml.template src/main/resources/application.yml
vi src/main/resources/application.yml
# 修改数据库和邮件配置
```

### 4. 编译打包
```bash
mvn clean package
```

### 5. 运行应用
```bash
# 开发模式
mvn spring-boot:run

# 生产模式
java -jar target/postgres-check-1.0.0.jar
```

## 部署方式

### 1. 本地运行
- Maven 直接运行
- JAR 文件运行

### 2. Linux 服务器
- systemd 服务（推荐）
- nohup 后台运行
- Supervisor 进程管理

### 3. Windows 服务器
- 批处理脚本
- NSSM Windows 服务

### 4. 容器化部署
- Docker
- Docker Compose

## 日志管理

### 日志位置
- 控制台输出：实时显示
- 文件日志：`logs/postgres-check.log`

### 日志级别
- 应用日志：INFO
- Spring 框架：WARN
- Hibernate：WARN

### 日志轮转
- 单文件最大：10MB
- 保留天数：30 天

## 监控指标

### 关键日志输出

**正常情况：**
```
开始检查 sync_operation_log 表，时间窗口：最近 5 分钟
正常：sync_operation_log 表在过去 5 分钟内有 10 条新增数据
```

**异常情况：**
```
警告：sync_operation_log 表在过去 5 分钟内没有新增数据
最新记录时间: 2024-01-01 09:55:00
数据缺失警告邮件已发送至: recipient@example.com
```

## 安全考虑

### 1. 代码安全
- ✅ 通过 CodeQL 安全扫描，无漏洞
- ✅ 配置文件不包含在版本控制中
- ✅ 添加了空值和异常处理

### 2. 配置安全
- 使用模板文件避免敏感信息泄露
- 支持环境变量覆盖配置
- 建议使用加密工具管理敏感配置

### 3. 运行安全
- 建议使用受限权限的用户运行
- 配置文件权限设置为 600
- 定期更新依赖包

## 性能考虑

### 1. 数据库优化
- 在 `changed_at` 字段上创建索引
- 使用简单的 COUNT 查询，性能高效
- 数据库连接池配置合理

### 2. 应用优化
- 定时任务异步执行，不阻塞主线程
- 邮件发送包含异常捕获，失败不影响后续执行
- JVM 参数可根据实际情况调优

### 3. 资源使用
- 内存占用：约 100-200MB
- CPU 使用：检查时短暂上升，其他时间很低
- 网络：仅在发送邮件时使用

## 扩展建议

### 1. 功能扩展
- 支持监控多个表
- 支持自定义告警规则
- 添加 Web 管理界面
- 支持更多通知方式（短信、钉钉、微信等）
- 添加健康检查端点

### 2. 监控扩展
- 集成 Prometheus 和 Grafana
- 添加应用性能监控（APM）
- 记录监控历史数据
- 生成监控报表

### 3. 部署扩展
- 支持集群部署
- 添加配置中心（Spring Cloud Config）
- 容器编排（Kubernetes）

## 常见问题

### 1. 邮件发送失败
- 检查 SMTP 配置
- 验证邮箱授权码
- 查看防火墙设置

### 2. 数据库连接失败
- 检查数据库地址和端口
- 验证用户名和密码
- 确认 PostgreSQL 服务运行

### 3. 定时任务不执行
- 检查 Cron 表达式语法
- 查看应用日志
- 确认 `@EnableScheduling` 注解存在

## 测试验证

### 1. 单元测试
- 可添加 JUnit 测试用例
- 测试关键业务逻辑

### 2. 集成测试
- 测试数据库连接
- 测试邮件发送
- 测试定时任务执行

### 3. 手动测试
- 插入测试数据验证正常流程
- 停止数据写入验证告警流程

## 文档资源

1. **README.md** - 项目总体说明和使用指南
2. **QUICKSTART.md** - 快速开始指南，5分钟上手
3. **DEPLOYMENT.md** - 详细部署指南，多种环境
4. **sql/create_table.sql** - 数据库表创建脚本
5. **src/main/resources/application.yml.template** - 配置文件模板

## 版本信息

- **当前版本**: 1.0.0
- **发布日期**: 2024
- **维护状态**: 活跃维护

## 许可证

MIT License

## 贡献指南

欢迎提交 Issue 和 Pull Request 改进项目。

## 联系方式

如有问题或建议，请通过 GitHub Issues 联系。

---

**项目成功指标：**
- ✅ 编译成功
- ✅ 代码审查通过
- ✅ 安全扫描通过（CodeQL 0 漏洞）
- ✅ 文档完善
- ✅ 可直接部署使用
