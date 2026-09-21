package com.clj.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clj.ai.domain.BizMessage;
import com.clj.ai.service.BizConversationService;
import com.clj.ai.service.BizMessageService;
import com.clj.ai.mapper.BizMessageMapper;
import com.clj.domain.BizConversation;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @author ajie
* @description 针对表【biz_message(AI聊天消息表)】的数据库操作Service实现
* @createDate 2026-08-18 21:00:23
*/
@Service
public class BizMessageServiceImpl extends ServiceImpl<BizMessageMapper, BizMessage>
    implements BizMessageService{

    @Override
    public List<BizMessage> listByConversationId(Long conversationId) {
        return lambdaQuery()
                .eq(BizMessage::getConversationId, conversationId)
                .eq(BizMessage::getIsDeleted, false)
                .orderByAsc(BizMessage::getCreatedTime)
                .orderByAsc(BizMessage::getId)
                .list();
    }

    @Override
    public List<BizMessage> listRecentByConversationId(Long conversationId, int limit) {
        if (conversationId == null || limit <= 0) {
            return List.of();
        }
        List<BizMessage> recent = lambdaQuery()
                .eq(BizMessage::getConversationId, conversationId)
                .eq(BizMessage::getIsDeleted, false)
                .orderByDesc(BizMessage::getCreatedTime)
                .orderByDesc(BizMessage::getId)
                .last("LIMIT " + Math.min(limit, 100))
                .list();
        java.util.Collections.reverse(recent);
        return recent;
    }

    @Override
    public void retainLatest(Long conversationId, Long userId, int limit,
                             BizConversationService conversationService) {
        if (conversationId == null || limit < 0
                || conversationService.getOwned(conversationId, userId) == null) {
            throw new IllegalArgumentException("浼氳瘽涓嶅瓨鍦?");
        }
        List<BizMessage> all = lambdaQuery()
                .eq(BizMessage::getConversationId, conversationId)
                .eq(BizMessage::getIsDeleted, false)
                .orderByDesc(BizMessage::getCreatedTime)
                .orderByDesc(BizMessage::getId)
                .list();
        Date now = new Date();
        for (int i = Math.min(limit, all.size()); i < all.size(); i++) {
            BizMessage message = all.get(i);
            message.setIsDeleted(true);
            message.setUpdatedBy(userId);
            message.setUpdatedTime(now);
            updateById(message);
        }
    }

    @Override
    public void deleteOwned(Long id, Long userId, BizConversationService conversationService) {
        BizMessage message = getById(id);
        if (message == null || !isOwned(message, userId, conversationService)) {
            throw new IllegalArgumentException("消息不存在");
        }
        message.setIsDeleted(true);
        message.setUpdatedBy(userId);
        message.setUpdatedTime(new Date());
        updateById(message);
    }

    @Override
    public void deleteAfter(Long id, Long userId, BizConversationService conversationService) {
        BizMessage message = getById(id);
        if (message == null || !isOwned(message, userId, conversationService)) {
            throw new IllegalArgumentException("消息不存在");
        }
        lambdaUpdate()
                .eq(BizMessage::getConversationId, message.getConversationId())
                .gt(BizMessage::getId, id)
                .eq(BizMessage::getIsDeleted, false)
                .set(BizMessage::getIsDeleted, true)
                .set(BizMessage::getUpdatedBy, userId)
                .set(BizMessage::getUpdatedTime, new Date())
                .update();
    }

    @Override
    public void replaceUserMessage(Long id, Long userId, String text, BizConversationService conversationService) {
        BizMessage message = getById(id);
        if (message == null
                || !"USER".equalsIgnoreCase(message.getRole())
                || !isOwned(message, userId, conversationService)) {
            throw new IllegalArgumentException("用户消息不存在");
        }
        message.setContentJson("{\"text\":" + quoteJson(text) + "}");
        message.setUpdatedBy(userId);
        message.setUpdatedTime(new Date());
        updateById(message);
        deleteAfter(id, userId, conversationService);
    }

    private boolean isOwned(BizMessage message, Long userId, BizConversationService conversationService) {
        BizConversation conversation = conversationService.getOwned(message.getConversationId(), userId);
        return conversation != null;
    }

    private String quoteJson(String value) {
        StringBuilder result = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> result.append("\\\\");
                case '"' -> result.append("\\\"");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> result.append(c);
            }
        }
        return result.append('"').toString();
    }
}



