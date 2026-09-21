-- RAG schema indexes and foreign keys.
-- This file is intended to run after the existing RAG dump files.

-- Compatibility columns used by the application. The existing dump predates
-- these fields, so make this script safe to run independently after the dump.
ALTER TABLE public.ai_knowledge_base
  ADD COLUMN IF NOT EXISTS type_id bigint,
  ADD COLUMN IF NOT EXISTS deleted boolean,
  ADD COLUMN IF NOT EXISTS update_time timestamp(6);

UPDATE public.ai_knowledge_base
SET deleted = COALESCE(deleted, is_deleted, false),
    update_time = COALESCE(update_time, uptim, crtim, CURRENT_TIMESTAMP);

ALTER TABLE public.ai_knowledge_base
  ALTER COLUMN deleted SET DEFAULT false,
  ALTER COLUMN deleted SET NOT NULL,
  ALTER COLUMN update_time SET DEFAULT CURRENT_TIMESTAMP,
  ALTER COLUMN update_time SET NOT NULL;

-- Knowledge-base filters and compatibility columns.
CREATE INDEX IF NOT EXISTS idx_ai_knowledge_base_type_id
  ON public.ai_knowledge_base(type_id);
CREATE INDEX IF NOT EXISTS idx_ai_knowledge_base_is_deleted
  ON public.ai_knowledge_base(is_deleted);
CREATE INDEX IF NOT EXISTS idx_ai_knowledge_base_deleted
  ON public.ai_knowledge_base(deleted);
CREATE INDEX IF NOT EXISTS idx_ai_knowledge_base_update_time
  ON public.ai_knowledge_base(update_time);

-- Document filters.
CREATE INDEX IF NOT EXISTS idx_rag_document_knowledge_base_id
  ON public.ai_rag_document(knowledge_base_id);
CREATE INDEX IF NOT EXISTS idx_rag_document_process_status
  ON public.ai_rag_document(process_status);
CREATE INDEX IF NOT EXISTS idx_rag_document_is_deleted
  ON public.ai_rag_document(is_deleted);

-- Chunk filters and vector-search support.
CREATE INDEX IF NOT EXISTS idx_rag_chunk_document_id
  ON public.ai_rag_chunk(document_id);
CREATE INDEX IF NOT EXISTS idx_rag_chunk_is_active
  ON public.ai_rag_chunk(is_active);
CREATE INDEX IF NOT EXISTS idx_rag_chunk_is_deleted
  ON public.ai_rag_chunk(is_deleted);

-- Retrieval trace lookups.
CREATE INDEX IF NOT EXISTS idx_rag_retrieval_message_id
  ON public.ai_rag_retrieval(message_id);
CREATE INDEX IF NOT EXISTS idx_rag_retrieval_document_id
  ON public.ai_rag_retrieval(document_id);
CREATE INDEX IF NOT EXISTS idx_rag_retrieval_chunk_id
  ON public.ai_rag_retrieval(chunk_id);

-- Keep the existing vector index optional because HNSW can be expensive to build.
-- CREATE INDEX IF NOT EXISTS idx_rag_chunk_embedding
--   ON public.ai_rag_chunk USING hnsw (embedding vector_cosine_ops);

-- Add relationship constraints only when they are absent, so reruns are harmless.
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'fk_ai_rag_document_knowledge_base'
      AND conrelid = 'public.ai_rag_document'::regclass
  ) THEN
    ALTER TABLE public.ai_rag_document
      ADD CONSTRAINT fk_ai_rag_document_knowledge_base
      FOREIGN KEY (knowledge_base_id)
      REFERENCES public.ai_knowledge_base (id)
      NOT VALID;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'fk_ai_rag_chunk_document'
      AND conrelid = 'public.ai_rag_chunk'::regclass
  ) THEN
    ALTER TABLE public.ai_rag_chunk
      ADD CONSTRAINT fk_ai_rag_chunk_document
      FOREIGN KEY (document_id)
      REFERENCES public.ai_rag_document (id)
      NOT VALID;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'fk_ai_rag_retrieval_document'
      AND conrelid = 'public.ai_rag_retrieval'::regclass
  ) THEN
    ALTER TABLE public.ai_rag_retrieval
      ADD CONSTRAINT fk_ai_rag_retrieval_document
      FOREIGN KEY (document_id)
      REFERENCES public.ai_rag_document (id)
      NOT VALID;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'fk_ai_rag_retrieval_chunk'
      AND conrelid = 'public.ai_rag_retrieval'::regclass
  ) THEN
    ALTER TABLE public.ai_rag_retrieval
      ADD CONSTRAINT fk_ai_rag_retrieval_chunk
      FOREIGN KEY (chunk_id)
      REFERENCES public.ai_rag_chunk (id)
      NOT VALID;
  END IF;
END
$$;
