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

 Date: 30/10/2026 21:45:18
*/


-- ----------------------------
-- Table structure for ai_knowledge_base_type
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_knowledge_base_type";
CREATE TABLE "public"."ai_knowledge_base_type" (
  "id" int8 NOT NULL DEFAULT nextval('ai_knowledge_base_type_id_seq'::regclass),
  "type_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "type_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(500) COLLATE "pg_catalog"."default",
  "sort_no" int4 NOT NULL DEFAULT 0,
  "status" int2 NOT NULL DEFAULT 1,
  "deleted" bool NOT NULL DEFAULT false,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."id" IS '知识库类型ID';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."type_name" IS '知识库类型名称';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."type_code" IS '知识库类型编码';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."description" IS '知识库类型描述';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."sort_no" IS '排序号';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."status" IS '状态：1启用，0禁用';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."deleted" IS '逻辑删除标识：false未删除，true已删除';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ai_knowledge_base_type"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."ai_knowledge_base_type" IS 'AI知识库类型表';

-- ----------------------------
-- Records of ai_knowledge_base_type
-- ----------------------------
INSERT INTO "public"."ai_knowledge_base_type" VALUES (1, '农业', 'AGRICULTURE', '农业知识库', 1, 1, 'f', '2026-09-18 11:47:42.767605', '2026-09-18 11:47:42.767605');
INSERT INTO "public"."ai_knowledge_base_type" VALUES (2, '医疗', '223', '32', 0, 1, 'f', '2026-09-18 20:23:59.055', '2026-09-18 20:27:14.984');

-- ----------------------------
-- Uniques structure for table ai_knowledge_base_type
-- ----------------------------
ALTER TABLE "public"."ai_knowledge_base_type" ADD CONSTRAINT "uk_ai_knowledge_base_type_code" UNIQUE ("type_code");

-- ----------------------------
-- Primary Key structure for table ai_knowledge_base_type
-- ----------------------------
ALTER TABLE "public"."ai_knowledge_base_type" ADD CONSTRAINT "ai_knowledge_base_type_pkey" PRIMARY KEY ("id");
