CREATE EXTENSION IF NOT EXISTS vector;

CREATE SEQUENCE IF NOT EXISTS public.ai_knowledge_base_id_seq AS bigint;
CREATE SEQUENCE IF NOT EXISTS public.ai_rag_document_id_seq AS bigint;
CREATE SEQUENCE IF NOT EXISTS public.ai_rag_chunk_id_seq AS bigint;
CREATE SEQUENCE IF NOT EXISTS public.ai_rag_retrieval_id_seq AS bigint;
CREATE SEQUENCE IF NOT EXISTS public.biz_conversation_id_seq AS bigint;
CREATE SEQUENCE IF NOT EXISTS public.biz_message_id_seq AS bigint;
