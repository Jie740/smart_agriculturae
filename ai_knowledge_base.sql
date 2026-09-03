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
