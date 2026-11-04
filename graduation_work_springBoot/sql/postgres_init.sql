CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS biz_conversation (
  id BIGINT PRIMARY KEY,
  title VARCHAR(255),
  user_id BIGINT,
  model_name VARCHAR(128),
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_by BIGINT,
  created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT,
  updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS biz_message (
  id BIGINT PRIMARY KEY,
  conversation_id BIGINT,
  role VARCHAR(32),
  content_json JSONB,
  trace_json JSONB,
  attributes JSONB,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_by BIGINT,
  created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT,
  updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_knowledge_base (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  status INTEGER NOT NULL DEFAULT 1,
  document_count INTEGER NOT NULL DEFAULT 0,
  crtim TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cruid BIGINT,
  uptim TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  upuid BIGINT,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at TIMESTAMP,
  deleted_by BIGINT
);

CREATE TABLE IF NOT EXISTS ai_rag_document (
  id BIGINT PRIMARY KEY,
  knowledge_base_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  file_name VARCHAR(255),
  file_type VARCHAR(64),
  file_size BIGINT,
  object_name VARCHAR(512),
  file_url TEXT,
  content TEXT,
  status INTEGER NOT NULL DEFAULT 1,
  process_status INTEGER NOT NULL DEFAULT 0,
  process_message TEXT,
  chunk_count INTEGER NOT NULL DEFAULT 0,
  version INTEGER NOT NULL DEFAULT 1,
  file_hash VARCHAR(128),
  crtim TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cruid BIGINT,
  uptim TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  upuid BIGINT,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at TIMESTAMP,
  deleted_by BIGINT
);

CREATE TABLE IF NOT EXISTS ai_rag_chunk (
  id BIGINT PRIMARY KEY,
  document_id BIGINT NOT NULL,
  chunk_index INTEGER NOT NULL,
  text TEXT NOT NULL,
  token_count INTEGER,
  embedding VECTOR(1024),
  embedding_model VARCHAR(128),
  embedding_version VARCHAR(64),
  metadata JSONB,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  crtim TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cruid BIGINT,
  uptim TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  upuid BIGINT,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at TIMESTAMP,
  deleted_by BIGINT
);

CREATE TABLE IF NOT EXISTS ai_rag_retrieval (
  id BIGINT PRIMARY KEY,
  message_id BIGINT,
  document_id BIGINT,
  chunk_id BIGINT,
  score NUMERIC(10, 6),
  created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_biz_conversation_user_id ON biz_conversation(user_id);
CREATE INDEX IF NOT EXISTS idx_biz_conversation_is_deleted ON biz_conversation(is_deleted);
CREATE INDEX IF NOT EXISTS idx_biz_message_conversation_id ON biz_message(conversation_id);
CREATE INDEX IF NOT EXISTS idx_biz_message_is_deleted ON biz_message(is_deleted);
CREATE INDEX IF NOT EXISTS idx_knowledge_base_is_deleted ON ai_knowledge_base(is_deleted);
CREATE INDEX IF NOT EXISTS idx_rag_document_knowledge_base_id ON ai_rag_document(knowledge_base_id);
CREATE INDEX IF NOT EXISTS idx_rag_document_process_status ON ai_rag_document(process_status);
CREATE INDEX IF NOT EXISTS idx_rag_document_is_deleted ON ai_rag_document(is_deleted);
CREATE INDEX IF NOT EXISTS idx_rag_chunk_document_id ON ai_rag_chunk(document_id);
CREATE INDEX IF NOT EXISTS idx_rag_chunk_is_active ON ai_rag_chunk(is_active);
CREATE INDEX IF NOT EXISTS idx_rag_chunk_is_deleted ON ai_rag_chunk(is_deleted);
CREATE INDEX IF NOT EXISTS idx_rag_retrieval_message_id ON ai_rag_retrieval(message_id);
CREATE INDEX IF NOT EXISTS idx_rag_retrieval_document_id ON ai_rag_retrieval(document_id);
