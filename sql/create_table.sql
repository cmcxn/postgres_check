-- ================================================
-- sync_operation_log 表创建脚本
-- ================================================

-- 如果表已存在，先删除（谨慎使用）
-- DROP TABLE IF EXISTS sync_operation_log;

-- 创建表
CREATE TABLE IF NOT EXISTS "public"."sync_operation_log" (
  "id" SERIAL PRIMARY KEY,
  "table_name" TEXT,
  "primary_key_value" TEXT,
  "operation" TEXT,
  "changed_columns" TEXT,
  "old_values" JSONB,
  "new_values" JSONB,
  "changed_at" TIMESTAMP(6) NOT NULL DEFAULT NOW()
);

-- 添加注释
COMMENT ON TABLE "public"."sync_operation_log" IS '数据同步操作日志表';
COMMENT ON COLUMN "public"."sync_operation_log"."id" IS '主键ID';
COMMENT ON COLUMN "public"."sync_operation_log"."table_name" IS '表名';
COMMENT ON COLUMN "public"."sync_operation_log"."primary_key_value" IS '主键值';
COMMENT ON COLUMN "public"."sync_operation_log"."operation" IS '操作类型（INSERT/UPDATE/DELETE）';
COMMENT ON COLUMN "public"."sync_operation_log"."changed_columns" IS '变更的字段列表';
COMMENT ON COLUMN "public"."sync_operation_log"."old_values" IS '变更前的值（JSON格式）';
COMMENT ON COLUMN "public"."sync_operation_log"."new_values" IS '变更后的值（JSON格式）';
COMMENT ON COLUMN "public"."sync_operation_log"."changed_at" IS '变更时间';

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_sync_operation_log_changed_at 
  ON "public"."sync_operation_log" ("changed_at" DESC);

-- 创建索引以支持按表名查询
CREATE INDEX IF NOT EXISTS idx_sync_operation_log_table_name 
  ON "public"."sync_operation_log" ("table_name");

-- 插入测试数据（可选）
INSERT INTO "public"."sync_operation_log" 
  ("table_name", "primary_key_value", "operation", "changed_columns", "old_values", "new_values", "changed_at")
VALUES
  ('users', '1', 'INSERT', 'id,name,email', NULL, 
   '{"id": 1, "name": "张三", "email": "zhangsan@example.com"}'::jsonb, 
   NOW()),
  ('users', '1', 'UPDATE', 'email', 
   '{"email": "zhangsan@example.com"}'::jsonb,
   '{"email": "zhangsan@newmail.com"}'::jsonb, 
   NOW()),
  ('products', '100', 'INSERT', 'id,name,price', NULL,
   '{"id": 100, "name": "商品A", "price": 99.99}'::jsonb,
   NOW() - INTERVAL '10 minutes');

-- 查询测试
SELECT * FROM "public"."sync_operation_log" ORDER BY "changed_at" DESC;

-- 查询最近5分钟的记录数
SELECT COUNT(*) as recent_count 
FROM "public"."sync_operation_log" 
WHERE "changed_at" >= NOW() - INTERVAL '5 minutes';
