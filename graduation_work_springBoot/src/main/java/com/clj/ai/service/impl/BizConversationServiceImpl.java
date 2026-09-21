package com.clj.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clj.domain.BizConversation;
import com.clj.ai.service.BizConversationService;
import com.clj.ai.mapper.BizConversationMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @author ajie
* @description 针对表【biz_conversation(AI对话会话表)】的数据库操作Service实现
* @createDate 2026-08-18 21:00:10
*/
@Service
public class BizConversationServiceImpl extends ServiceImpl<BizConversationMapper, BizConversation>
    implements BizConversationService{

    @Override
    public List<BizConversation> listByUserId(Long userId) {
        return lambdaQuery()
                .eq(BizConversation::getUserId, userId)
                .eq(BizConversation::getIsDeleted, false)
                .orderByDesc(BizConversation::getUpdatedTime)
                .orderByDesc(BizConversation::getId)
                .list();
    }

    @Override
    public BizConversation getOwned(Long id, Long userId) {
        if (id == null) {
            return null;
        }
        return lambdaQuery()
                .eq(BizConversation::getId, id)
                .eq(BizConversation::getUserId, userId)
                .eq(BizConversation::getIsDeleted, false)
                .one();
    }

    @Override
    public BizConversation create(Long userId, String title, String modelName) {
        BizConversation conversation = new BizConversation();
        conversation.setTitle(title == null || title.isBlank() ? "新建会话" : title.trim());
        conversation.setUserId(userId);
        conversation.setModelName(modelName == null || modelName.isBlank() ? "default" : modelName);
        conversation.setIsDeleted(false);
        conversation.setCreatedBy(userId);
        conversation.setUpdatedBy(userId);
        conversation.setCreatedTime(new Date());
        conversation.setUpdatedTime(new Date());
        save(conversation);
        return conversation;
    }

    @Override
    public void updateTitle(Long id, Long userId, String title) {
        BizConversation conversation = getOwned(id, userId);
        if (conversation == null) {
            throw new IllegalArgumentException("会话不存在");
        }
        String normalizedTitle = title == null ? "" : title.trim();
        if (normalizedTitle.isBlank()) {
            throw new IllegalArgumentException("会话标题不能为空");
        }
        conversation.setTitle(normalizedTitle);
        conversation.setUpdatedBy(userId);
        conversation.setUpdatedTime(new Date());
        updateById(conversation);
    }

    @Override
    public void deleteOwned(Long id, Long userId) {
        BizConversation conversation = getOwned(id, userId);
        if (conversation == null) {
            throw new IllegalArgumentException("会话不存在");
        }
        conversation.setIsDeleted(true);
        conversation.setUpdatedBy(userId);
        conversation.setUpdatedTime(new Date());
        updateById(conversation);
    }
}




