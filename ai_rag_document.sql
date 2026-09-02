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

 Date: 02/09/2026 23:23:33
*/


-- ----------------------------
-- Table structure for ai_rag_document
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_rag_document";
CREATE TABLE "public"."ai_rag_document" (
  "id" int8 NOT NULL DEFAULT nextval('ai_rag_document_id_seq'::regclass),
  "knowledge_base_id" int8 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "file_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "file_type" varchar(50) COLLATE "pg_catalog"."default",
  "file_size" int8,
  "object_name" varchar(500) COLLATE "pg_catalog"."default",
  "file_url" varchar(1000) COLLATE "pg_catalog"."default",
  "content" text COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 1,
  "process_status" int2 NOT NULL DEFAULT 0,
  "process_message" text COLLATE "pg_catalog"."default",
  "chunk_count" int4 NOT NULL DEFAULT 0,
  "version" int4 NOT NULL DEFAULT 1,
  "file_hash" varchar(128) COLLATE "pg_catalog"."default",
  "crtim" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "cruid" int8,
  "uptim" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "upuid" int8,
  "is_deleted" bool NOT NULL DEFAULT false,
  "deleted_at" timestamp(6),
  "deleted_by" int8
)
;
COMMENT ON COLUMN "public"."ai_rag_document"."id" IS '文档ID';
COMMENT ON COLUMN "public"."ai_rag_document"."knowledge_base_id" IS '知识库ID';
COMMENT ON COLUMN "public"."ai_rag_document"."name" IS '文档名称';
COMMENT ON COLUMN "public"."ai_rag_document"."file_name" IS '原始文件名';
COMMENT ON COLUMN "public"."ai_rag_document"."file_type" IS '文件类型';
COMMENT ON COLUMN "public"."ai_rag_document"."file_size" IS '文件大小，单位字节';
COMMENT ON COLUMN "public"."ai_rag_document"."object_name" IS 'MinIO对象名称';
COMMENT ON COLUMN "public"."ai_rag_document"."file_url" IS '文件访问地址';
COMMENT ON COLUMN "public"."ai_rag_document"."content" IS '文档原始文本内容';
COMMENT ON COLUMN "public"."ai_rag_document"."status" IS '状态：1正常 0禁用';
COMMENT ON COLUMN "public"."ai_rag_document"."process_status" IS '处理状态：0待处理 1解析中 2向量化中 3完成 4失败';
COMMENT ON COLUMN "public"."ai_rag_document"."process_message" IS '处理失败信息';
COMMENT ON COLUMN "public"."ai_rag_document"."chunk_count" IS '文档Chunk数量';
COMMENT ON COLUMN "public"."ai_rag_document"."version" IS '文档版本';
COMMENT ON COLUMN "public"."ai_rag_document"."file_hash" IS '文件SHA-256哈希值';
COMMENT ON COLUMN "public"."ai_rag_document"."crtim" IS '创建时间';
COMMENT ON COLUMN "public"."ai_rag_document"."cruid" IS '创建人ID';
COMMENT ON COLUMN "public"."ai_rag_document"."uptim" IS '更新时间';
COMMENT ON COLUMN "public"."ai_rag_document"."upuid" IS '更新人ID';
COMMENT ON COLUMN "public"."ai_rag_document"."is_deleted" IS '是否删除：false否 true是';
COMMENT ON COLUMN "public"."ai_rag_document"."deleted_at" IS '删除时间';
COMMENT ON COLUMN "public"."ai_rag_document"."deleted_by" IS '删除人ID';
COMMENT ON TABLE "public"."ai_rag_document" IS 'AI知识库文档表';

-- ----------------------------
-- Records of ai_rag_document
-- ----------------------------
INSERT INTO "public"."ai_rag_document" VALUES (2093619021043236866, 2090049979451801601, '现代农业种植技术知识库.pdf', '现代农业种植技术知识库.pdf', 'pdf', 3937370, 'rag/2090049979451801601/2093619021043236866/现代农业种植技术知识库.pdf', 'http://192.168.127.128:9000/agriculture/rag/2090049979451801601/2093619021043236866/现代农业种植技术知识库.pdf', NULL, 1, 3, NULL, 33, 1, NULL, '2026-08-29 16:37:26.829', 1, '2026-08-29 16:37:31.603', 1, 'f', NULL, NULL);
INSERT INTO "public"."ai_rag_document" VALUES (2093612468533731329, 2090049979451801601, '现代农业种植技术知识库.pdf', '现代农业种植技术知识库.pdf', 'pdf', 3937370, 'rag/2090049979451801601/2093612468533731329/现代农业种植技术知识库.pdf', 'http://192.168.127.128:9000/agriculture/rag/2090049979451801601/2093612468533731329/现代农业种植技术知识库.pdf', NULL, 1, 3, NULL, 153, 1, NULL, '2026-08-29 16:11:24.59', 1, '2026-08-29 16:11:35.695', 1, 'f', NULL, NULL);
INSERT INTO "public"."ai_rag_document" VALUES (2093612698549362689, 2090049979451801601, '玉米标准化种植技术规范.pdf', '玉米标准化种植技术规范.pdf', 'pdf', 342495, 'rag/2090049979451801601/2093612698549362689/玉米标准化种植技术规范.pdf', 'http://192.168.127.128:9000/agriculture/rag/2090049979451801601/2093612698549362689/玉米标准化种植技术规范.pdf', NULL, 1, 3, NULL, 6, 1, NULL, '2026-08-29 16:12:19.441', 1, '2026-08-29 16:12:20.027', 1, 'f', NULL, NULL);
INSERT INTO "public"."ai_rag_document" VALUES (2093613557987442689, 2090049979451801601, '玉米标准化种植技术规范.pdf', '玉米标准化种植技术规范.pdf', 'pdf', 342495, 'rag/2090049979451801601/2093613557987442689/玉米标准化种植技术规范.pdf', 'http://192.168.127.128:9000/agriculture/rag/2090049979451801601/2093613557987442689/玉米标准化种植技术规范.pdf', NULL, 1, 3, NULL, 6, 1, NULL, '2026-08-29 16:15:44.336', 1, '2026-08-29 16:15:45.913', 1, 'f', NULL, NULL);

-- ----------------------------
-- Indexes structure for table ai_rag_document
-- ----------------------------
CREATE INDEX "idx_rag_document_is_deleted" ON "public"."ai_rag_document" USING btree (
  "is_deleted" "pg_catalog"."bool_ops" ASC NULLS LAST
);
CREATE INDEX "idx_rag_document_knowledge_base_id" ON "public"."ai_rag_document" USING btree (
  "knowledge_base_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_rag_document_process_status" ON "public"."ai_rag_document" USING btree (
  "process_status" "pg_catalog"."int2_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table ai_rag_document
-- ----------------------------
ALTER TABLE "public"."ai_rag_document" ADD CONSTRAINT "ai_rag_document_pkey" PRIMARY KEY ("id");
