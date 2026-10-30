package com.clj.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clj.ai.domain.AiKnowledgeBaseType;
import com.clj.ai.dto.AiKnowledgeBaseTypeDto;
import com.clj.ai.service.AiKnowledgeBaseTypeService;
import com.clj.ai.mapper.AiKnowledgeBaseTypeMapper;
import com.clj.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
* @author ajie
* @description 针对表【ai_knowledge_base_type(AI知识库类型表)】的数据库操作Service实现
* @createDate 2026-09-18 19:48:30
*/
@Service
public class AiKnowledgeBaseTypeServiceImpl extends ServiceImpl<AiKnowledgeBaseTypeMapper, AiKnowledgeBaseType>
    implements AiKnowledgeBaseTypeService{

    @Override
    public List<AiKnowledgeBaseType> listEnabled() {
        return lambdaQuery()
                .eq(AiKnowledgeBaseType::getDeleted, false)
                .orderByAsc(AiKnowledgeBaseType::getSortNo)
                .orderByAsc(AiKnowledgeBaseType::getId)
                .list();
    }

    @Override
    public void saveType(AiKnowledgeBaseTypeDto dto) {
        normalizeAndValidate(dto, false);

        boolean duplicated = lambdaQuery()
                .eq(AiKnowledgeBaseType::getTypeCode, dto.getTypeCode())
                .exists();
        if (duplicated) {
            throw new BusinessException("类型编码已存在");
        }

        AiKnowledgeBaseType type = new AiKnowledgeBaseType();
        BeanUtils.copyProperties(dto, type);
        Date now = new Date();
        type.setDeleted(false);
        type.setCreateTime(now);
        type.setUpdateTime(now);
        if (type.getSortNo() == null) {
            type.setSortNo(0);
        }
        if (type.getStatus() == null) {
            type.setStatus(1);
        }

        if (!save(type)) {
            throw new BusinessException("新增知识库类型失败");
        }
    }

    @Override
    public void updateType(AiKnowledgeBaseTypeDto dto) {
        normalizeAndValidate(dto, true);

        AiKnowledgeBaseType existing = getById(dto.getId());
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new BusinessException("知识库类型不存在");
        }

        boolean duplicated = lambdaQuery()
                .eq(AiKnowledgeBaseType::getTypeCode, dto.getTypeCode())
                .ne(AiKnowledgeBaseType::getId, dto.getId())
                .exists();
        if (duplicated) {
            throw new BusinessException("类型编码已存在");
        }

        BeanUtils.copyProperties(dto, existing);
        existing.setDeleted(false);
        existing.setCreateTime(existing.getCreateTime() == null ? new Date() : existing.getCreateTime());
        existing.setUpdateTime(new Date());
        if (existing.getSortNo() == null) {
            existing.setSortNo(0);
        }
        if (existing.getStatus() == null) {
            existing.setStatus(1);
        }

        if (!updateById(existing)) {
            throw new BusinessException("更新知识库类型失败");
        }
    }

    @Override
    public void deleteType(Long id) {
        if (id == null) {
            throw new BusinessException("知识库类型ID不能为空");
        }

        AiKnowledgeBaseType existing = getById(id);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new BusinessException("知识库类型不存在");
        }

        existing.setDeleted(true);
        existing.setUpdateTime(new Date());
        if (!updateById(existing)) {
            throw new BusinessException("删除知识库类型失败");
        }
    }

    private void normalizeAndValidate(AiKnowledgeBaseTypeDto dto, boolean requireId) {
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (requireId && dto.getId() == null) {
            throw new BusinessException("知识库类型ID不能为空");
        }
        if (!StringUtils.hasText(dto.getTypeName())) {
            throw new BusinessException("类型名称不能为空");
        }
        if (!StringUtils.hasText(dto.getTypeCode())) {
            throw new BusinessException("类型编码不能为空");
        }

        dto.setTypeName(dto.getTypeName().trim());
        dto.setTypeCode(dto.getTypeCode().trim().toUpperCase());
        if (dto.getTypeName().length() > 100) {
            throw new BusinessException("类型名称不能超过100个字符");
        }
        if (dto.getTypeCode().length() > 50) {
            throw new BusinessException("类型编码不能超过50个字符");
        }
        if (dto.getDescription() != null && dto.getDescription().length() > 500) {
            throw new BusinessException("类型描述不能超过500个字符");
        }
        if (dto.getStatus() != null && dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new BusinessException("类型状态只能是0或1");
        }
        if (dto.getSortNo() != null && dto.getSortNo() < 0) {
            throw new BusinessException("排序号不能小于0");
        }
    }
}




