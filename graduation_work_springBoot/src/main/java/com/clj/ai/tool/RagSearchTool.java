package com.clj.ai.tool;

import com.clj.ai.dto.RagSearchRequestDto;
import com.clj.ai.service.AiRagSearchService;
import com.clj.ai.service.RagSelectionContext;
import com.clj.ai.vo.RagSearchResultVo;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * RAG 检索工具
 * <p>
 * 检索范围完全由用户在「知识库配置」中的勾选决定（见 {@link RagSelectionContext}），
 * 由服务端按 memoryId 强制限定，模型无需知道知识库 id，直接调用 searchKnowledge 即可：
 * - 勾选整个知识库：该库全部文档参与检索（knowledgeBaseIds）
 * - 只勾选库内部分文档：仅这些文档参与检索（documentIds）
 * - 两类可并存，检索范围为两者并集
 * - 什么都没勾选：不做任何检索，由模型用自己的知识回答
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagSearchTool {

    /**
     * 用户未勾选任何知识库/文档时的统一回复，模型据此改用自身知识回答
     */
    private static final String NO_SCOPE_MESSAGE =
            "用户当前未勾选任何知识库或文档，本次不允许进行知识库检索。"
                    + "请直接依据你自身的知识回答，并提醒用户可以点击「知识库配置」勾选需要检索的知识库或文档。";

    private final AiRagSearchService searchService;

    @Tool("在用户勾选的知识库/文档中做向量检索，返回与问题最相关的资料片段。"
            + "当问题可能涉及专业知识（如作物种植、病虫害防治、施肥技术等）且本次允许检索时调用；"
            + "检索范围由服务端自动限定在用户勾选范围内，无需传入知识库 id。")
    public String searchKnowledge(
            @P("检索语句，使用用户问题的核心关键词或完整问句") String query,
            @P("返回结果条数，默认 5") Integer topK,
            @ToolMemoryId String memoryId
    ) {
        RagSelectionContext.Selection selection = RagSelectionContext.get(memoryId);
        // 并集范围：整库全选走 knowledgeBaseIds，库内部分勾选走具体 documentIds，两者互不影响
        List<Long> documentIds = selection == null ? List.of() : normalize(selection.documentIds());
        List<Long> scopedKnowledgeBaseIds = selection == null ? List.of() : normalize(selection.knowledgeBaseIds());
        if (documentIds.isEmpty() && scopedKnowledgeBaseIds.isEmpty()) {
            log.info("模型调用 searchKnowledge 但检索范围为空，已拒绝检索: memoryId={}", memoryId);
            return NO_SCOPE_MESSAGE;
        }

        log.info("模型调用 searchKnowledge: query={}, documentIds={}, knowledgeBaseIds={}, topK={}",
                query, documentIds, scopedKnowledgeBaseIds, topK);

        RagSearchRequestDto request = new RagSearchRequestDto();
        request.setQuery(query);
        request.setDocumentIds(documentIds);
        request.setKnowledgeBaseIds(scopedKnowledgeBaseIds);
        request.setTopK(topK == null || topK <= 0 ? 5 : Math.min(topK, 50));

        List<RagSearchResultVo> results = searchService.search(request);
        if (results.isEmpty()) {
            return "知识库中未检索到与该问题相关的内容。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("检索到 ").append(results.size()).append(" 条相关资料：\n\n");
        for (int i = 0; i < results.size(); i++) {
            RagSearchResultVo r = results.get(i);
            sb.append("【资料 ").append(i + 1).append("】");
            if (r.getKnowledgeBaseName() != null) {
                sb.append("（知识库：").append(r.getKnowledgeBaseName()).append("）");
            }
            sb.append("\n").append(r.getContent()).append("\n");
            if (r.getScore() != null) {
                sb.append("相关度：").append(String.format("%.2f", r.getScore())).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private List<Long> normalize(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }
}
