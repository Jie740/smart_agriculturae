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

 Date: 02/09/2026 23:23:50
*/


-- ----------------------------
-- Table structure for biz_conversation
-- ----------------------------
DROP TABLE IF EXISTS "public"."biz_conversation";
CREATE TABLE "public"."biz_conversation" (
  "id" int8 NOT NULL DEFAULT nextval('biz_conversation_id_seq'::regclass),
  "title" varchar(200) COLLATE "pg_catalog"."default",
  "user_id" int8 NOT NULL,
  "model_name" varchar(100) COLLATE "pg_catalog"."default",
  "is_deleted" bool NOT NULL DEFAULT false,
  "created_by" int8,
  "created_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_by" int8,
  "updated_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."biz_conversation"."id" IS '主键ID';
COMMENT ON COLUMN "public"."biz_conversation"."title" IS '会话标题';
COMMENT ON COLUMN "public"."biz_conversation"."user_id" IS '所属用户ID';
COMMENT ON COLUMN "public"."biz_conversation"."model_name" IS '使用模型名称';
COMMENT ON COLUMN "public"."biz_conversation"."is_deleted" IS '逻辑删除标识';
COMMENT ON COLUMN "public"."biz_conversation"."created_by" IS '创建人';
COMMENT ON COLUMN "public"."biz_conversation"."created_time" IS '创建时间';
COMMENT ON COLUMN "public"."biz_conversation"."updated_by" IS '更新人';
COMMENT ON COLUMN "public"."biz_conversation"."updated_time" IS '更新时间';
COMMENT ON TABLE "public"."biz_conversation" IS 'AI对话会话表';

-- ----------------------------
-- Records of biz_conversation
-- ----------------------------

-- ----------------------------
-- Primary Key structure for table biz_conversation
-- ----------------------------
ALTER TABLE "public"."biz_conversation" ADD CONSTRAINT "biz_conversation_pkey" PRIMARY KEY ("id");
