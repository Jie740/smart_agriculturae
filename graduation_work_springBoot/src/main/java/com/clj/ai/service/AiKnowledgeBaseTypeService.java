package com.clj.ai.service;

import com.clj.ai.domain.AiKnowledgeBaseType;
import com.baomidou.mybatisplus.extension.service.IService;
import com.clj.ai.dto.AiKnowledgeBaseTypeDto;

import java.util.List;

/**
* @author ajie
* @description 针对表【ai_knowledge_base_type(AI知识库类型表)】的数据库操作Service
* @createDate 2026-09-18 19:48:30
*/
public interface AiKnowledgeBaseTypeService extends IService<AiKnowledgeBaseType> {

    /**
     * 查询未逻辑删除的知识库类型，供管理页面和知识库配置使用。
     */
    List<AiKnowledgeBaseType> listEnabled();

    /**
     * 新增知识库类型。
     */
    void saveType(AiKnowledgeBaseTypeDto dto);

    /**
     * 更新知识库类型。
     */
    void updateType(AiKnowledgeBaseTypeDto dto);

    /**
     * 逻辑删除知识库类型。
     */
    void deleteType(Long id);
}
