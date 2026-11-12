-- 种子数据(06/07)使用显式 id 导入,自增序列不会自动前进,
-- 必须在导入完成后把序列同步到当前最大 id,否则新插入会报主键冲突。
SELECT setval('biz_conversation_id_seq', (SELECT COALESCE(MAX(id), 1) FROM biz_conversation));
SELECT setval('biz_message_id_seq', (SELECT COALESCE(MAX(id), 1) FROM biz_message));
