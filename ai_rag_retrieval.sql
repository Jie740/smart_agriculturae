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

 Date: 02/09/2026 23:23:40
*/


-- ----------------------------
-- Table structure for ai_rag_retrieval
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_rag_retrieval";
CREATE TABLE "public"."ai_rag_retrieval" (
  "id" int8 NOT NULL DEFAULT nextval('ai_rag_retrieval_id_seq'::regclass),
  "message_id" int8 NOT NULL,
  "document_id" int8 NOT NULL,
  "chunk_id" int8,
  "score" numeric(10,6),
  "created_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."ai_rag_retrieval"."id" IS '主键ID';
COMMENT ON COLUMN "public"."ai_rag_retrieval"."message_id" IS '关联AI消息ID';
COMMENT ON COLUMN "public"."ai_rag_retrieval"."document_id" IS '知识库文档ID';
COMMENT ON COLUMN "public"."ai_rag_retrieval"."chunk_id" IS '知识库分块ID';
COMMENT ON COLUMN "public"."ai_rag_retrieval"."score" IS '向量检索相似度得分';
COMMENT ON COLUMN "public"."ai_rag_retrieval"."created_time" IS '创建时间';
COMMENT ON TABLE "public"."ai_rag_retrieval" IS 'RAG检索溯源记录表';

-- ----------------------------
-- Records of ai_rag_retrieval
-- ----------------------------

-- ----------------------------
-- Indexes structure for table ai_rag_retrieval
-- ----------------------------
CREATE INDEX "idx_rag_retrieval_document_id" ON "public"."ai_rag_retrieval" USING btree (
  "document_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_rag_retrieval_message_id" ON "public"."ai_rag_retrieval" USING btree (
  "message_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table ai_rag_retrieval
-- ----------------------------
ALTER TABLE "public"."ai_rag_retrieval" ADD CONSTRAINT "ai_rag_retrieval_pkey" PRIMARY KEY ("id");
