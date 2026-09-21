package com.clj.ai.util;

import com.clj.ai.dto.ChatAttachmentDto;
import com.clj.ai.dto.ChatRequestDto;
import com.clj.ai.service.MinioService;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 消息构建工具
 * <p>
 * 图片处理流程（Base64 仅在 Java → Qwen 这一段使用，不返回前端）：
 * 前端 imageUrls → Java → MinIO 读取 byte[] → Base64 data URL → Qwen
 * <p>
 * 文档附件不做多模态传输，而是上传时已写入知识库，
 * 这里只在文本里告知模型附件存在，由模型按需调用 searchKnowledge 检索。
 *
 * @author ajie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageUtil {

    private static final String DEFAULT_PROMPT = "请结合我上传的附件回答问题。";

    private static final Pattern IMAGE_EXTENSION =
            Pattern.compile(".*\\.(jpg|jpeg|png|gif|webp|bmp)$", Pattern.CASE_INSENSITIVE);

    private final MinioService minioService;
    private final DocumentTextReader documentTextReader;

    /**
     * 本次请求的文本内容（用户输入 + 附件内容）
     * <p>
     * 文档附件在服务端解析成文本后一并发送：DashScope 兼容接口只接受图片，
     * PDF/Word 这类文件无法按 URL 直接交给模型，只能把内容读出来喂给模型。
     */
    public String buildText(ChatRequestDto chatRequestDto) {
        StringBuilder text = new StringBuilder();
        if (StringUtils.hasText(chatRequestDto.getText())) {
            text.append(chatRequestDto.getText().trim());
        }

        for (ChatAttachmentDto document : documentAttachments(chatRequestDto)) {
            String name = StringUtils.hasText(document.getName()) ? document.getName() : "未命名文件";
            text.append("\n\n【用户本次上传的文件：").append(name).append("】\n");
            String content = documentTextReader.read(document.getUrl(), name);
            if (StringUtils.hasText(content)) {
                text.append(content);
            } else {
                text.append("（该文件内容解析失败，请直接告知用户无法读取这个文件）");
            }
        }
        if (text.length() == 0) {
            text.append(DEFAULT_PROMPT);
        }
        return text.toString();
    }

    /**
     * 是否携带了图片附件
     */
    public boolean hasImages(ChatRequestDto chatRequestDto) {
        return !imageUrls(chatRequestDto).isEmpty();
    }

    /**
     * 构建多模态用户消息：文本 + 图片（图片读取失败时降级为文本说明，不影响整体对话）
     */
    public UserMessage buildMultimodalMessage(ChatRequestDto chatRequestDto, String text) {
        List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(StringUtils.hasText(text) ? text : DEFAULT_PROMPT));

        for (String imageUrl : imageUrls(chatRequestDto)) {
            try {
                String dataUrl = minioService.readImageAsDataUrl(imageUrl);
                contents.add(ImageContent.from(dataUrl));
            } catch (Exception e) {
                // 单张图片读取失败不应该让整轮对话失败
                log.warn("读取图片失败，已跳过该图片: url={}, 原因={}", imageUrl, e.getMessage());
                contents.add(TextContent.from("（有一张图片读取失败，无法查看）"));
            }
        }
        return UserMessage.from(contents);
    }

    /**
     * 图片地址：前端 imageUrls 与 attachments 可能重复，这里去重
     */
    private Set<String> imageUrls(ChatRequestDto chatRequestDto) {
        Set<String> urls = new LinkedHashSet<>();
        if (chatRequestDto.getImageUrls() != null) {
            chatRequestDto.getImageUrls().stream()
                    .filter(StringUtils::hasText)
                    .forEach(urls::add);
        }
        if (chatRequestDto.getAttachments() != null) {
            for (ChatAttachmentDto attachment : chatRequestDto.getAttachments()) {
                if (isImage(attachment) && StringUtils.hasText(attachment.getUrl())) {
                    urls.add(attachment.getUrl());
                }
            }
        }
        return urls;
    }

    /**
     * 非图片附件（已在上传时写入知识库的文档）
     */
    private List<ChatAttachmentDto> documentAttachments(ChatRequestDto chatRequestDto) {
        if (chatRequestDto.getAttachments() == null) {
            return List.of();
        }
        return chatRequestDto.getAttachments().stream()
                .filter(attachment -> attachment != null && !isImage(attachment))
                .filter(attachment -> StringUtils.hasText(attachment.getName()))
                .toList();
    }

    private boolean isImage(ChatAttachmentDto attachment) {
        if (attachment == null) {
            return false;
        }
        if (attachment.getType() != null && attachment.getType().toLowerCase().startsWith("image/")) {
            return true;
        }
        // 浏览器偶尔拿不到 MIME，回退按扩展名判断
        return attachment.getName() != null && IMAGE_EXTENSION.matcher(attachment.getName()).matches();
    }
}
