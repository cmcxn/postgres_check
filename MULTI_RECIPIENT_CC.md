# 多人接收和抄送功能说明

## 功能概述

系统现在支持为所有邮件通知配置多个收件人（TO）和抄送人（CC），让更多人可以收到监控通知。

## 配置方式

### 1. 多个收件人（TO）

在 `application.yml` 中配置多个收件人，用英文逗号分隔：

```yaml
app:
  notification:
    to: admin@example.com,manager@example.com,developer@example.com
```

### 2. 多个抄送人（CC）

配置抄送人，用英文逗号分隔：

```yaml
app:
  notification:
    cc: supervisor@example.com,ceo@example.com
```

### 3. 完整配置示例

```yaml
app:
  notification:
    enabled: true
    from: monitor@example.com
    
    # 主要收件人（必填）
    to: admin1@example.com,admin2@example.com
    
    # 抄送人（可选）
    cc: supervisor@example.com,manager@example.com
    
    # 其他配置...
    subject: "[警告] 数据库监控：sync_operation_log 表无新数据"
```

## 收件人类型说明

### TO（收件人）
- **必填**：必须配置至少一个收件人
- **作用**：邮件的主要接收者，通常是负责处理告警的人员
- **多个收件人**：用英文逗号分隔，支持任意数量
- **示例**：`admin@example.com,ops@example.com`

### CC（抄送人）
- **可选**：可以不配置，留空或不填写
- **作用**：邮件的抄送接收者，通常是需要知情但不需要直接处理的人员
- **多个抄送人**：用英文逗号分隔，支持任意数量
- **示例**：`manager@example.com,supervisor@example.com`

## 适用范围

CC 功能适用于所有类型的邮件通知：

1. ✅ **启动通知** - 系统启动时
2. ✅ **告警通知** - 检测到无数据时
3. ✅ **恢复通知** - 数据恢复正常时
4. ✅ **每日健康检查** - 每天定时发送
5. ✅ **关闭通知** - 系统关闭时

## 配置规则

### 1. 空格处理
系统会自动处理邮箱地址前后的空格：

```yaml
# 以下两种写法都是正确的
to: admin@example.com,manager@example.com
to: admin@example.com, manager@example.com
```

### 2. 留空 CC
如果不需要抄送，可以：

**方法 1**：留空
```yaml
cc:
```

**方法 2**：删除该行
```yaml
# cc 行可以完全删除
```

**方法 3**：注释掉
```yaml
# cc: supervisor@example.com
```

### 3. 邮箱格式
- 必须是有效的邮箱地址格式
- 支持各种域名：`@qq.com`, `@gmail.com`, `@163.com` 等
- 不支持中文邮箱地址

## 使用场景

### 场景 1：团队协作
```yaml
# 多个运维人员都需要收到告警
to: ops1@example.com,ops2@example.com,ops3@example.com
# 管理层需要知情
cc: manager@example.com,director@example.com
```

### 场景 2：主备方案
```yaml
# 主要负责人
to: primary-admin@example.com
# 备用负责人
cc: backup-admin@example.com,standby-admin@example.com
```

### 场景 3：跨部门协作
```yaml
# 开发团队
to: dev-lead@example.com,dev-ops@example.com
# 产品和运营团队知情
cc: product@example.com,operations@example.com
```

### 场景 4：仅主要收件人
```yaml
# 只有一个人负责，不需要抄送
to: solo-admin@example.com
# cc 不配置
```

## 邮件示例

### 发送前配置
```yaml
to: alice@example.com,bob@example.com
cc: charlie@example.com
```

### 邮件头信息
```
From: monitor@example.com
To: alice@example.com, bob@example.com
Cc: charlie@example.com
Subject: [警告] 数据库监控：sync_operation_log 表无新数据
```

### 收件人视角
- **Alice** 和 **Bob**：会在 "收件人" 字段看到彼此
- **Charlie**：会在 "抄送" 字段显示
- **所有人**：都能看到完整的收件人和抄送列表

## 日志输出

### 没有配置 CC
```
数据缺失警告邮件已发送至: admin@example.com,manager@example.com
```

### 配置了 CC（DEBUG 级别）
```
已添加抄送人: supervisor@example.com,ceo@example.com
数据缺失警告邮件已发送至: admin@example.com,manager@example.com
```

## 常见问题

### Q1: CC 是必须配置的吗？
**答**：不是，CC 是完全可选的。如果不需要抄送功能，可以不配置。

### Q2: 可以配置多少个收件人和抄送人？
**答**：理论上没有数量限制，但建议：
- 收件人（TO）：3-5 人
- 抄送人（CC）：2-3 人
- 总数不超过 10 人，避免邮件服务器限制

### Q3: TO 和 CC 有什么区别？
**答**：
- **TO**：主要收件人，通常是需要采取行动的人
- **CC**：抄送人，仅供知情，通常不需要采取行动
- 所有人都会收到相同的邮件内容

### Q4: 如何测试配置是否正确？
**答**：
1. 配置完成后重启应用
2. 查看启动通知邮件
3. 检查所有配置的邮箱是否都收到邮件
4. 检查邮件头的 TO 和 CC 字段是否正确

### Q5: 配置错误会影响邮件发送吗？
**答**：
- 如果 TO 字段为空或格式错误，邮件不会发送
- 如果 CC 字段格式错误，会忽略 CC，仅发送给 TO
- 建议测试后再正式使用

### Q6: 抄送人会知道有哪些主要收件人吗？
**答**：会的。所有收件人（TO 和 CC）都能在邮件头中看到完整的收件人列表。

### Q7: 可以在运行时修改配置吗？
**答**：需要修改 `application.yml` 文件并重启应用。配置更改在重启后生效。

## 最佳实践

### 1. 明确责任
```yaml
# 明确负责处理的人员
to: on-duty-admin@example.com
# 相关方知情
cc: team-lead@example.com,manager@example.com
```

### 2. 使用邮件组
```yaml
# 可以使用邮件组地址
to: ops-team@example.com
cc: management@example.com
```

### 3. 避免过多收件人
```yaml
# ❌ 不推荐：太多人
to: person1@example.com,person2@example.com,person3@example.com,person4@example.com,person5@example.com

# ✅ 推荐：使用邮件组
to: ops-team@example.com
```

### 4. 定期审查
- 定期检查配置的邮箱是否仍然有效
- 移除已离职人员的邮箱
- 根据组织变化调整收件人列表

## 技术细节

### 实现方式
- 使用 Spring 的 `SimpleMailMessage.setTo()` 设置主要收件人
- 使用 `SimpleMailMessage.setCc()` 设置抄送人
- 邮箱地址会自动去除首尾空格
- 通过 SMTP 协议发送，符合标准邮件规范

### 向后兼容
- 现有不包含 CC 配置的配置文件完全兼容
- CC 字段为空或不存在时，系统正常工作
- 不会影响现有功能

## 配置文件位置

- **模板文件**：`src/main/resources/application.yml.template`
- **实际配置**：`src/main/resources/application.yml`（需从模板复制）

## 相关文档

- [README.md](README.md) - 项目总体说明
- [QUICKSTART.md](QUICKSTART.md) - 快速开始指南
- [EMAIL_TROUBLESHOOTING.md](EMAIL_TROUBLESHOOTING.md) - 邮件故障排查

---

**更新时间**：2026-02-12
**版本**：1.0.0
