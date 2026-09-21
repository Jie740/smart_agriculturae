package com.clj.ai.tool;

import com.clj.ai.domain.AiKnowledgeBase;
import com.clj.ai.domain.AiRagDocument;
import com.clj.ai.dto.RagSearchRequestDto;
import com.clj.ai.mapper.AiRagDocumentMapper;
import com.clj.ai.service.AiKnowledgeBaseService;
import com.clj.ai.service.AiRagSearchService;
import com.clj.ai.service.RagSelectionContext;
import com.clj.ai.vo.RagSearchResultVo;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * RAG 检索工具
 * <p>
 * 模型无法凭空知道 knowledgeBaseId，因此拆成两步：
 * 1. listKnowledgeBases() —— 让模型先"看见"用户勾选了哪些知识库（id + 名称 + 描述）
 * 2. searchKnowledge()    —— 模型在勾选范围内发起检索
 * <p>
 * 检索范围完全由用户在「知识库配置」中的勾选决定（见 {@link RagSelectionContext}）：
 * - 勾选了文档：只在这些文档内检索
 * - 只勾选知识库：只在这些知识库内检索
 * - 什么都没勾选：不做任何检索，由模型用自己的知识回答
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagSearchTool {

    /**
     * 用户未勾选任何知识库/文档时的统一回复，模型据此改用自身知识作答
     */
    private static final String NO_SCOPE_MESSAGE =
            "用户当前未勾选任何知识库或文档，本次不允许进行知识库检索。"
                    + "请直接依据你自身的知识回答，并提醒用户可以点击「知识库配置」勾选需要检索的知识库或文档。";

    private final AiRagSearchService searchService;
    private final AiKnowledgeBaseService knowledgeBaseService;
    private final AiRagDocumentMapper documentMapper;

    /**
     * 列出用户当前允许检索的知识库，供模型判断该去哪个（些）知识库检索
     */
    @Tool("列出用户当前勾选、允许检索的知识库，返回每个知识库的 id、名称和描述。"
            + "在调用 searchKnowledge 之前先调用本工具；"
            + "如果本工具提示用户未勾选任何知识库，则本次不要调用 searchKnowledge。")
    public String listKnowledgeBases(@ToolMemoryId String memoryId) {
        RagSelectionContext.Selection selection = RagSelectionContext.get(memoryId);
        // selection 为 null 表示不在用户会话上下文中（如接口自测），此时才放开为全部启用知识库
        boolean scoped = selection != null;
        if (scoped && hasNoScope(selection)) {
            log.info("模型调用 listKnowledgeBases，但用户未勾选任何范围，memoryId={}", memoryId);
            return NO_SCOPE_MESSAGE;
        }

        List<Long> allowedIds = scoped ? resolveKnowledgeBaseIds(selection) : List.of();
        if (scoped && allowedIds.isEmpty()) {
            return NO_SCOPE_MESSAGE;
        }

        List<AiKnowledgeBase> bases;
        if (scoped) {
            bases = knowledgeBaseService.lambdaQuery()
                    .in(AiKnowledgeBase::getId, allowedIds)
                    .eq(AiKnowledgeBase::getIsDeleted, false)
                    .orderByAsc(AiKnowledgeBase::getId)
                    .list();
        } else {
            bases = knowledgeBaseService.lambdaQuery()
                    .eq(AiKnowledgeBase::getIsDeleted, false)
                    .eq(AiKnowledgeBase::getStatus, 1)
                    .orderByAsc(AiKnowledgeBase::getId)
                    .list();
        }

        if (bases.isEmpty()) {
            return scoped ? "用户勾选范围内没有可用的知识库。" : "当前没有可用的知识库。";
        }

        StringBuilder sb = new StringBuilder();
        if (scoped) {
            sb.append("用户勾选了 ").append(bases.size()).append(" 个知识库，只能在其中检索：\n");
        } else {
            sb.append("可用知识库共 ").append(bases.size()).append(" 个：\n");
        }
        for (AiKnowledgeBase base : bases) {
            sb.append("- id=").append(base.getId())
                    .append(" | 名称=").append(base.getName())
                    .append(" | 描述=")
                    .append(base.getDescription() == null || base.getDescription().isBlank()
                            ? "（无描述）" : base.getDescription())
                    .append(" | 文档数=").append(base.getDocumentCount())
                    .append("\n");
        }
        sb.append(describeSelectedDocuments(scoped ? selection.documentIds() : List.of()));
        log.info("模型调用 listKnowledgeBases，返回 {} 个知识库，scoped={}", bases.size(), scoped);
        return sb.toString();
    }

    /**
     * 向量检索，范围严格限制在用户勾选的知识库/文档内
     *
     * @param query            检索语句
     * @param knowledgeBaseIds 要检索的知识库 id 列表，取自 listKnowledgeBases 返回的 id
     * @param topK             返回结果条数
     */
    @Tool("在用户勾选的知识库/文档中做向量检索，返回与问题最相关的资料片段。"
            + "当问题可能涉及专业知识（如作物种植、病虫害防治、施肥技术等）时调用；"
            + "检索范围由用户勾选决定，本工具无法扩大或缩小该范围。")
    public String searchKnowledge(
            @P("检索语句，使用用户问题的核心关键词或完整问句") String query,
            @P("要检索的知识库 id 列表，取自 listKnowledgeBases 返回的 id") List<Long> knowledgeBaseIds,
            @P("返回结果条数，默认 5") Integer topK,
            @ToolMemoryId String memoryId
    ) {
        RagSelectionContext.Selection selection = RagSelectionContext.get(memoryId);
        List<Long> documentIds;
        List<Long> scopedKnowledgeBaseIds;
        if (selection != null) {
            // 用户勾选优先：勾选了文档就只按文档检索（文档所属知识库不再参与过滤），否则按知识库检索
            documentIds = normalize(selection.documentIds());
            scopedKnowledgeBaseIds = documentIds.isEmpty() ? normalize(selection.knowledgeBaseIds()) : List.of();
        } else {
            documentIds = List.of();
            scopedKnowledgeBaseIds = normalize(knowledgeBaseIds);
        }
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

    private boolean hasNoScope(RagSelectionContext.Selection selection) {
        return normalize(selection.documentIds()).isEmpty() && normalize(selection.knowledgeBaseIds()).isEmpty();
    }

    private List<Long> normalize(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    /**
     * 勾选范围涉及的知识库：直接勾选的知识库 + 勾选文档所属的知识库
     */
    private List<Long> resolveKnowledgeBaseIds(RagSelectionContext.Selection selection) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>(normalize(selection.knowledgeBaseIds()));
        for (AiRagDocument document : loadSelectedDocuments(selection.documentIds())) {
            if (document.getKnowledgeBaseId() != null) {
                ids.add(document.getKnowledgeBaseId());
            }
        }
        return List.copyOf(ids);
    }

    /**
     * 勾选文档的清单，便于模型理解本次检索范围
     */
    private String describeSelectedDocuments(List<Long> documentIds) {
        List<AiRagDocument> documents = loadSelectedDocuments(documentIds);
        if (documents.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("另外，用户单独勾选了 ")
                .append(documents.size()).append(" 篇文档，检索时只查这些文档：\n");
        for (AiRagDocument document : documents) {
            sb.append("- id=").append(document.getId())
                    .append(" | 名称=").append(document.getName())
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * 按 id 批量加载未删除的勾选文档
     */
    private List<AiRagDocument> loadSelectedDocuments(List<Long> documentIds) {
        List<Long> ids = normalize(documentIds);
        if (ids.isEmpty()) {
            return List.of();
        }
        return documentMapper.selectBatchIds(ids).stream()
                .filter(Objects::nonNull)
                .filter(document -> !Boolean.TRUE.equals(document.getIsDeleted()))
                .toList();
    }
}
