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

 Date: 30/10/2026 18:22:50
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
INSERT INTO "public"."biz_conversation" VALUES (8, '新建会话', 1, 'default', 'f', 1, '2026-09-21 16:42:29.865', 1, '2026-09-21 17:21:23.701');
INSERT INTO "public"."biz_conversation" VALUES (3, '新建会话', 1, 'default', 'f', 1, '2026-09-18 17:02:25.227', 1, '2026-09-18 17:03:47.679');
INSERT INTO "public"."biz_conversation" VALUES (2, '新建会话', 1, 'default', 'f', 1, '2026-09-18 17:02:25.012', 1, '2026-09-18 17:07:18.818');
INSERT INTO "public"."biz_conversation" VALUES (4, '新建会话', 1, 'default', 'f', 1, '2026-09-18 17:06:59.429', 1, '2026-09-18 17:08:02.648');
INSERT INTO "public"."biz_conversation" VALUES (14, '这个文件里写了什么？', 1, 'default', 'f', 1, '2026-09-21 17:36:00.266', 1, '2026-09-21 17:36:17.666');
INSERT INTO "public"."biz_conversation" VALUES (15, '这张图片是什么颜色？只回答颜色。', 1, 'default', 'f', 1, '2026-09-21 17:36:09.943', 1, '2026-09-21 17:36:17.742');
INSERT INTO "public"."biz_conversation" VALUES (6, '1', 1, 'default', 'f', 1, '2026-09-18 17:11:49.327', 1, '2026-09-18 20:38:08.242');
INSERT INTO "public"."biz_conversation" VALUES (5, '新建会话', 1, 'default', 'f', 1, '2026-09-18 17:11:21.925', 1, '2026-09-18 20:38:15.092');
INSERT INTO "public"."biz_conversation" VALUES (1, '新建会话', 1, 'default', 'f', 1, '2026-09-18 17:02:23.302', 1, '2026-09-21 14:49:24.239');
INSERT INTO "public"."biz_conversation" VALUES (13, '新建会话', 1, 'default', 'f', 1, '2026-09-21 17:21:26.744', 1, '2026-09-21 17:41:17.891');
INSERT INTO "public"."biz_conversation" VALUES (7, '新建会话', 1, 'default', 'f', 1, '2026-09-21 15:25:31.833', 1, '2026-10-30 17:13:58.153');
INSERT INTO "public"."biz_conversation" VALUES (16, 'test', 1, 'default', 'f', 1, '2026-10-30 17:15:19.28', 1, '2026-10-30 18:09:15.064');
INSERT INTO "public"."biz_conversation" VALUES (9, '请描述这张图片', 1, 'default', 'f', 1, '2026-09-21 16:48:57.659', 1, '2026-09-21 17:13:54.669');
INSERT INTO "public"."biz_conversation" VALUES (10, '这张图片是什么颜色？只回答颜色。', 1, 'default', 'f', 1, '2026-09-21 16:50:08.211', 1, '2026-09-21 17:13:54.757');
INSERT INTO "public"."biz_conversation" VALUES (11, '这张图片是什么颜色？只回答颜色。', 1, 'default', 'f', 1, '2026-09-21 17:11:49.109', 1, '2026-09-21 17:13:54.855');
INSERT INTO "public"."biz_conversation" VALUES (12, '我上传的文档里，火龙果的成熟期是多少天？', 1, 'default', 'f', 1, '2026-09-21 17:12:28.073', 1, '2026-09-21 17:13:54.943');

-- ----------------------------
-- Primary Key structure for table biz_conversation
-- ----------------------------
ALTER TABLE "public"."biz_conversation" ADD CONSTRAINT "biz_conversation_pkey" PRIMARY KEY ("id");
