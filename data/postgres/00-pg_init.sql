-- PostgreSQL 初始化前置脚本（在 Navicat 导出的建表 SQL 之前执行）
-- 1. 启用 pgvector 扩展（ai_rag_chunk.embedding 使用 vector 类型）
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. 创建序列（Navicat 单表导出未包含 CREATE SEQUENCE，建表时 nextval 会报错）
CREATE SEQUENCE IF NOT EXISTS ai_knowledge_base_id_seq;
CREATE SEQUENCE IF NOT EXISTS ai_knowledge_base_type_id_seq;
CREATE SEQUENCE IF NOT EXISTS ai_rag_document_id_seq;
CREATE SEQUENCE IF NOT EXISTS ai_rag_chunk_id_seq;
CREATE SEQUENCE IF NOT EXISTS ai_rag_retrieval_id_seq;
CREATE SEQUENCE IF NOT EXISTS biz_conversation_id_seq;
CREATE SEQUENCE IF NOT EXISTS biz_message_id_seq;
