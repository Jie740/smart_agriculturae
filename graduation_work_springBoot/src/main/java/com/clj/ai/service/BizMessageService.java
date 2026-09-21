package com.clj.ai.service;

import com.clj.ai.domain.BizMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
* @author ajie
* @description 针对表【biz_message(AI聊天消息表)】的数据库操作Service
* @createDate 2026-08-18 21:00:23
*/
public interface BizMessageService extends IService<BizMessage> {
    List<BizMessage> listByConversationId(Long conversationId);
    List<BizMessage> listRecentByConversationId(Long conversationId, int limit);
    void retainLatest(Long conversationId, Long userId, int limit, BizConversationService conversationService);
    void deleteOwned(Long id, Long userId, BizConversationService conversationService);
    void deleteAfter(Long id, Long userId, BizConversationService conversationService);
    void replaceUserMessage(Long id, Long userId, String text, BizConversationService conversationService);
}
