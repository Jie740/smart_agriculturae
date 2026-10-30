/*
 Navicat Premium Dump SQL

 Source Server         : 192.168.127.128_pgvector
 Source Server Type    : PostgreSQL
 Source Server Version : 160015 (160015)
 Source Host           : 192.168.127.128:5432
 Source Catalog        : agriculture_ai
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 160015 (160015)
 File Encoding         : 65001

 Date: 02/09/2026 23:23:15
*/


-- ----------------------------
-- Table structure for ai_knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_knowledge_base";
CREATE TABLE "public"."ai_knowledge_base" (
  "id" int8 NOT NULL DEFAULT nextval('ai_knowledge_base_id_seq'::regclass),
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(500) COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 1,
  "document_count" int4 NOT NULL DEFAULT 0,
  "crtim" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "cruid" int8,
  "uptim" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "upuid" int8,
  "is_deleted" bool NOT NULL DEFAULT false,
  "deleted_at" timestamp(6),
  "deleted_by" int8
)
;
COMMENT ON COLUMN "public"."ai_knowledge_base"."id" IS '知识库ID';
COMMENT ON COLUMN "public"."ai_knowledge_base"."name" IS '知识库名称';
COMMENT ON COLUMN "public"."ai_knowledge_base"."description" IS '知识库描述';
COMMENT ON COLUMN "public"."ai_knowledge_base"."status" IS '状态：1启用 0禁用';
COMMENT ON COLUMN "public"."ai_knowledge_base"."document_count" IS '知识库文档数量';
COMMENT ON COLUMN "public"."ai_knowledge_base"."crtim" IS '创建时间';
COMMENT ON COLUMN "public"."ai_knowledge_base"."cruid" IS '创建人ID';
COMMENT ON COLUMN "public"."ai_knowledge_base"."uptim" IS '更新时间';
COMMENT ON COLUMN "public"."ai_knowledge_base"."upuid" IS '更新人ID';
COMMENT ON COLUMN "public"."ai_knowledge_base"."is_deleted" IS '是否删除：false否 true是';
COMMENT ON COLUMN "public"."ai_knowledge_base"."deleted_at" IS '删除时间';
COMMENT ON COLUMN "public"."ai_knowledge_base"."deleted_by" IS '删除人ID';
COMMENT ON TABLE "public"."ai_knowledge_base" IS 'AI知识库表';

-- ----------------------------
-- Records of ai_knowledge_base
-- ----------------------------
INSERT INTO "public"."ai_knowledge_base" VALUES (2090049979451801601, '农业知识库', '种植规范', 1, 4, '2026-08-19 20:15:20.958', 1, '2026-08-19 20:15:20.958', 1, 'f', NULL, NULL);

-- ----------------------------
-- Primary Key structure for table ai_knowledge_base
-- ----------------------------
ALTER TABLE "public"."ai_knowledge_base" ADD CONSTRAINT "ai_knowledge_base_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Incremental RAG knowledge-base type compatibility migration
-- ----------------------------
CREATE SEQUENCE IF NOT EXISTS "public"."ai_knowledge_base_type_id_seq";

CREATE TABLE IF NOT EXISTS "public"."ai_knowledge_base_type" (
  "id" int8 NOT NULL DEFAULT nextval('"public"."ai_knowledge_base_type_id_seq"'::regclass),
  "type_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "type_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(500) COLLATE "pg_catalog"."default",
  "sort_no" int4 NOT NULL DEFAULT 0,
  "status" int2 NOT NULL DEFAULT 1,
  "deleted" bool NOT NULL DEFAULT false,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'ai_knowledge_base_type_pkey'
      AND conrelid = 'public.ai_knowledge_base_type'::regclass
  ) THEN
    ALTER TABLE "public"."ai_knowledge_base_type"
      ADD CONSTRAINT "ai_knowledge_base_type_pkey" PRIMARY KEY ("id");
  END IF;
END
$$;

ALTER TABLE "public"."ai_knowledge_base_type"
  ADD COLUMN IF NOT EXISTS "type_name" varchar(100),
  ADD COLUMN IF NOT EXISTS "type_code" varchar(50),
  ADD COLUMN IF NOT EXISTS "description" varchar(500),
  ADD COLUMN IF NOT EXISTS "sort_no" int4 DEFAULT 0,
  ADD COLUMN IF NOT EXISTS "status" int2 DEFAULT 1,
  ADD COLUMN IF NOT EXISTS "deleted" bool DEFAULT false,
  ADD COLUMN IF NOT EXISTS "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN IF NOT EXISTS "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE "public"."ai_knowledge_base_type"
  ALTER COLUMN "id" SET DEFAULT nextval('"public"."ai_knowledge_base_type_id_seq"'::regclass),
  ALTER COLUMN "sort_no" SET DEFAULT 0,
  ALTER COLUMN "status" SET DEFAULT 1,
  ALTER COLUMN "deleted" SET DEFAULT false,
  ALTER COLUMN "create_time" SET DEFAULT CURRENT_TIMESTAMP,
  ALTER COLUMN "update_time" SET DEFAULT CURRENT_TIMESTAMP;

UPDATE "public"."ai_knowledge_base_type"
SET "type_name" = COALESCE(NULLIF("type_name", ''), '未分类'),
    "type_code" = COALESCE(NULLIF("type_code", ''), 'TYPE_' || "id"::text),
    "sort_no" = COALESCE("sort_no", 0),
    "status" = COALESCE("status", 1),
    "deleted" = COALESCE("deleted", false),
    "create_time" = COALESCE("create_time", CURRENT_TIMESTAMP),
    "update_time" = COALESCE("update_time", CURRENT_TIMESTAMP);

ALTER TABLE "public"."ai_knowledge_base_type"
  ALTER COLUMN "type_name" SET NOT NULL,
  ALTER COLUMN "type_code" SET NOT NULL,
  ALTER COLUMN "sort_no" SET NOT NULL,
  ALTER COLUMN "status" SET NOT NULL,
  ALTER COLUMN "deleted" SET NOT NULL,
  ALTER COLUMN "create_time" SET NOT NULL,
  ALTER COLUMN "update_time" SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS "uk_ai_knowledge_base_type_code"
  ON "public"."ai_knowledge_base_type" ("type_code");
CREATE INDEX IF NOT EXISTS "idx_ai_knowledge_base_type_status_deleted_sort"
  ON "public"."ai_knowledge_base_type" ("status", "deleted", "sort_no");

COMMENT ON TABLE "public"."ai_knowledge_base_type" IS 'AI知识库类型表';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."id" IS '知识库类型ID';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."type_name" IS '知识库类型名称';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."type_code" IS '知识库类型编码';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."description" IS '知识库类型描述';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."sort_no" IS '排序号';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."status" IS '状态：1启用 0禁用';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."deleted" IS '逻辑删除标识：false未删除 true已删除';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."update_time" IS '更新时间';

ALTER TABLE "public"."ai_knowledge_base"
  ADD COLUMN IF NOT EXISTS "type_id" int8,
  ADD COLUMN IF NOT EXISTS "deleted" bool,
  ADD COLUMN IF NOT EXISTS "update_time" timestamp(6);

UPDATE "public"."ai_knowledge_base"
SET "deleted" = COALESCE("deleted", "is_deleted", false),
    "update_time" = COALESCE("update_time", "uptim", "crtim", CURRENT_TIMESTAMP);

ALTER TABLE "public"."ai_knowledge_base"
  ALTER COLUMN "deleted" SET DEFAULT false,
  ALTER COLUMN "deleted" SET NOT NULL,
  ALTER COLUMN "update_time" SET DEFAULT CURRENT_TIMESTAMP,
  ALTER COLUMN "update_time" SET NOT NULL;

COMMENT ON COLUMN "public"."ai_knowledge_base"."type_id" IS '知识库类型ID，关联ai_knowledge_base_type.id';
COMMENT ON COLUMN "public"."ai_knowledge_base"."deleted" IS '兼容字段：逻辑删除标识，false未删除 true已删除';
COMMENT ON COLUMN "public"."ai_knowledge_base"."update_time" IS '兼容字段：更新时间';

INSERT INTO "public"."ai_knowledge_base_type"
  ("type_name", "type_code", "description", "sort_no", "status", "deleted")
VALUES
  ('农业', 'AGRICULTURE', '农业知识库', 1, 1, false)
ON CONFLICT ("type_code") DO NOTHING;

UPDATE "public"."ai_knowledge_base" AS kb
SET "type_id" = t."id",
    "update_time" = COALESCE(kb."update_time", kb."uptim", CURRENT_TIMESTAMP)
FROM "public"."ai_knowledge_base_type" AS t
WHERE t."type_code" = 'AGRICULTURE'
  AND kb."name" = '农业知识库'
  AND kb."type_id" IS DISTINCT FROM t."id";

SELECT setval(
  '"public"."ai_knowledge_base_type_id_seq"',
  GREATEST((SELECT COALESCE(MAX("id"), 1) FROM "public"."ai_knowledge_base_type"), 1),
  true
);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'fk_ai_knowledge_base_type'
      AND conrelid = 'public.ai_knowledge_base'::regclass
  ) THEN
    ALTER TABLE "public"."ai_knowledge_base"
      ADD CONSTRAINT "fk_ai_knowledge_base_type"
      FOREIGN KEY ("type_id")
      REFERENCES "public"."ai_knowledge_base_type" ("id")
      NOT VALID;
  END IF;
END
$$;
