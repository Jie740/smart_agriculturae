SELECT setval('public.ai_knowledge_base_id_seq', COALESCE((SELECT MAX(id) FROM public.ai_knowledge_base), 1), (SELECT MAX(id) IS NOT NULL FROM public.ai_knowledge_base));
SELECT setval('public.ai_rag_document_id_seq', COALESCE((SELECT MAX(id) FROM public.ai_rag_document), 1), (SELECT MAX(id) IS NOT NULL FROM public.ai_rag_document));
SELECT setval('public.ai_rag_chunk_id_seq', COALESCE((SELECT MAX(id) FROM public.ai_rag_chunk), 1), (SELECT MAX(id) IS NOT NULL FROM public.ai_rag_chunk));
SELECT setval('public.ai_rag_retrieval_id_seq', COALESCE((SELECT MAX(id) FROM public.ai_rag_retrieval), 1), (SELECT MAX(id) IS NOT NULL FROM public.ai_rag_retrieval));
SELECT setval('public.biz_conversation_id_seq', COALESCE((SELECT MAX(id) FROM public.biz_conversation), 1), (SELECT MAX(id) IS NOT NULL FROM public.biz_conversation));
SELECT setval('public.biz_message_id_seq', COALESCE((SELECT MAX(id) FROM public.biz_message), 1), (SELECT MAX(id) IS NOT NULL FROM public.biz_message));
