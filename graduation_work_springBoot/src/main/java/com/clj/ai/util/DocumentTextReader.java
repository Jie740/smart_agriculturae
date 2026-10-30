package com.clj.ai.util;

import com.clj.ai.service.MinioService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 读取聊天附件中的文档内容，供模型直接阅读。
 * <p>
 * DashScope 的 OpenAI 兼容接口只接受 image_url（图片），PDF/Word 等文件无法以 URL 直接传给模型，
 * 因此这里在服务端把文件解析成文本，随消息一起发给模型。
 * 解析能力与 RAG 入库保持一致：PDF 用 PDFBox，其它格式用 Tika。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentTextReader {

    /**
     * 单篇文档注入模型的最大字符数，避免超长文档挤爆上下文
     */
    private static final int MAX_CHARS = 20000;

    private final MinioService minioService;

    /**
     * 读取文档文本；解析失败时返回 null，由调用方决定如何提示模型
     *
     * @param url      文件访问地址
     * @param fileName 文件名（用于判断是否需要按 PDF 解析）
     */
    public String read(String url, String fileName) {
        try (InputStream inputStream = minioService.readByUrl(url)) {
            String text = isPdf(fileName) ? extractPdfText(inputStream) : parseWithTika(inputStream);
            if (text == null || text.isBlank()) {
                return null;
            }
            String trimmed = text.strip();
            if (trimmed.length() > MAX_CHARS) {
                return trimmed.substring(0, MAX_CHARS) + "\n……（文档过长，已截断）";
            }
            return trimmed;
        } catch (Exception e) {
            log.warn("读取附件内容失败: file={}, 原因={}", fileName, e.getMessage());
            return null;
        }
    }

    private boolean isPdf(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }

    /**
     * PDFBox 保留页边界，中文 PDF 提取效果比 Tika 稳定
     */
    private String extractPdfText(InputStream inputStream) throws Exception {
        try (PDDocument pdfDocument = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setPageEnd("\f");
            return stripper.getText(pdfDocument);
        }
    }

    private String parseWithTika(InputStream inputStream) throws Exception {
        Document document = new ApacheTikaDocumentParser().parse(inputStream);
        return document.text();
    }
}
