package com.clj.ai.service;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AssistantService {

    String AGRI_RAG_PROMPT = """
            你是一个农业助手，提供农业相关的信息和帮助。
            你可以使用工具 searchKnowledge 在用户勾选的知识库/文档中做向量检索。
            本次检索范围：{{ragScope}}。
            使用规则：
            - 检索范围由服务端根据用户勾选自动限定，无需先查询知识库列表，需要资料时直接调用 searchKnowledge。
            - 当用户问题可能涉及农业专业知识（如作物种植、病虫害、施肥等）且本次允许检索时，
              调用 searchKnowledge；若检索无结果，告知用户知识库中未找到相关资料，并根据自身知识补充。
            - 本次禁止检索时不要调用任何工具，直接依据你自己的知识回答，
              并提醒用户可以点击「知识库配置」勾选需要检索的知识库或文档。
            - 闲聊或常识问题无需检索，直接回答。
            """;

//            - 回答时以检索到的资料为依据，不要编造知识库中没有的内容。
//            - 回复的内容以html形式给出，加上html的标签



    /**
     * 单轮聊天
     */
    @SystemMessage(AGRI_RAG_PROMPT)
    @UserMessage("{{it}}")
    ChatResponse chat(
            @MemoryId String memoryId,
            @V("it") String text,
            @V("ragScope") String ragScope);

    /**
     * 流式聊天 - 返回 TokenStream 以支持工具调用
     * <p>
     * 注意：文本必须用 String + @V 传入，不能用 UserMessage 类型。
     * LangChain4j 会把带 @UserMessage 注解的参数当作提示词模板，对 UserMessage 对象调用
     * toString()，最终发给模型的是 "UserMessage { name = null contents = [...] }" 这样的调试字符串，
     * 图片等富内容也会全部丢失。附件图片由 AiServiceConfig 中的 chatRequestTransformer 注入。
     */
    @SystemMessage(AGRI_RAG_PROMPT)
    @UserMessage("{{it}}")
    TokenStream chatStream(
            @MemoryId String memoryId,
            @V("it") String text,
            @V("ragScope") String ragScope);
}
