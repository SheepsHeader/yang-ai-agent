package com.example.yangaiagent.Rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ETL 管道第 ① 步（Extract）：加载 classpath:rag/ 下的所有 Markdown 文件，
 * 解析成统一的 Document（文本内容 + metadata），为后续切分和向量化入库做准备。
 *
 * <p>背景知识 —— Spring AI 的文档处理是经典的 ETL（Extract-Transform-Load）管道：
 * <pre>
 * 原始文件 (PDF/MD/JSON/网页...)
 *     │
 *     ▼
 * ① DocumentReader  (Extract 读取)  ←—— 本类在这一步
 *     │  把原始内容解析成统一的 Document（文本 + metadata）
 *     │  常见实现: TextReader / JsonReader / TikaDocumentReader(PDF、Word)
 *     │           MarkdownDocumentReader(本类使用，按 Markdown 结构切分)
 *     ▼
 * ② DocumentTransformer  (Transform 转换加工，可选，可串多个)
 *     │  为向量化做准备（embedding 模型有 token 上限，长文本向量效果差）
 *     │  常见实现: TokenTextSplitter(按 token 切 chunk，最常用)
 *     │           KeywordMetadataEnricher / SummaryMetadataEnricher(AI 丰富元数据)
 *     ▼
 * ③ DocumentWriter  (Load 写入)
 *     │  最常用的是 VectorStore（PgVectorStore / SimpleVectorStore / RedisVectorStore）
 *     │  【向量化发生在这里】：vectorStore.add(docs) 时底层调用 EmbeddingModel
 *     │  把文本转成向量，再连同原文一起存入数据库
 *     ▼
 * VectorStore
 * </pre>
 *
 * <p>一句话总结：Reader/Transformer/Writer 是 RAG 文档入库的 ETL 三件套，
 * 向量化只是 Writer（VectorStore）内部的一个步骤。完整链路：
 * <pre>{@code
 * List<Document> docs = loader.loadMarkdowns();            // ① 读取（本类）
 * docs = new TokenTextSplitter().apply(docs);              // ② 切分
 * vectorStore.add(docs);                                   // ③ 向量化 + 入库
 * }</pre>
 */
@Component
@Slf4j
class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 这里可以修改为你要加载的多个 Markdown 文件的路径模式
            Resource[] resources = resourcePatternResolver.getResources("classpath:rag/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                // 配置 Markdown 解析规则：
                // - 遇到水平分割线 --- 就拆成独立 Document（按章节切分）
                // - 代码块、引用块不单独拆出，保留在所属段落中
                // - 把文件名写入 metadata，方便检索时溯源
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", fileName)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }
}
