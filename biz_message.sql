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

 Date: 02/09/2026 23:23:59
*/


-- ----------------------------
-- Table structure for biz_message
-- ----------------------------
DROP TABLE IF EXISTS "public"."biz_message";
CREATE TABLE "public"."biz_message" (
  "id" int8 NOT NULL DEFAULT nextval('biz_message_id_seq'::regclass),
  "conversation_id" int8 NOT NULL,
  "role" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "content_json" jsonb NOT NULL,
  "trace_json" jsonb,
  "attributes" jsonb,
  "is_deleted" bool NOT NULL DEFAULT false,
  "created_by" int8,
  "created_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_by" int8,
  "updated_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."biz_message"."id" IS '主键ID';
COMMENT ON COLUMN "public"."biz_message"."conversation_id" IS '所属会话ID';
COMMENT ON COLUMN "public"."biz_message"."role" IS '消息角色(SYSTEM/USER/AI/TOOL/CUSTOM)';
COMMENT ON COLUMN "public"."biz_message"."content_json" IS '消息内容JSON';
COMMENT ON COLUMN "public"."biz_message"."trace_json" IS '推理过程及工具调用轨迹JSON';
COMMENT ON COLUMN "public"."biz_message"."attributes" IS '扩展属性JSON';
COMMENT ON COLUMN "public"."biz_message"."is_deleted" IS '逻辑删除标识';
COMMENT ON COLUMN "public"."biz_message"."created_by" IS '创建人';
COMMENT ON COLUMN "public"."biz_message"."created_time" IS '创建时间';
COMMENT ON COLUMN "public"."biz_message"."updated_by" IS '更新人';
COMMENT ON COLUMN "public"."biz_message"."updated_time" IS '更新时间';
COMMENT ON TABLE "public"."biz_message" IS 'AI聊天消息表';

-- ----------------------------
-- Records of biz_message
-- ----------------------------

-- ----------------------------
-- Primary Key structure for table biz_message
-- ----------------------------
ALTER TABLE "public"."biz_message" ADD CONSTRAINT "biz_message_pkey" PRIMARY KEY ("id");
