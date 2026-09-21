package com.clj.ai.service;

import com.clj.domain.BizConversation;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
* @author ajie
* @description 针对表【biz_conversation(AI对话会话表)】的数据库操作Service
* @createDate 2026-08-18 21:00:10
*/
public interface BizConversationService extends IService<BizConversation> {
    List<BizConversation> listByUserId(Long userId);
    BizConversation getOwned(Long id, Long userId);
    BizConversation create(Long userId, String title, String modelName);
    void updateTitle(Long id, Long userId, String title);
    void deleteOwned(Long id, Long userId);
}
