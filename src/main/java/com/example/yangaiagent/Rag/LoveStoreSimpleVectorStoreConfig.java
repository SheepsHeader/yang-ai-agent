package com.example.yangaiagent.Rag;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ETL 管道第 ③ 步（Load）：把切好的 Document 写入 SimpleVectorStore。
 * add() 时底层调用 EmbeddingModel 完成向量化，再存入内存向量库。
 */
@Configuration
@Slf4j
public class LoveStoreSimpleVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore loveStoreSimpleVectorStore(DashScopeEmbeddingModel dashScopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashScopeEmbeddingModel).build();
        List<Document> markdowns = loveAppDocumentLoader.loadMarkdowns();
        // SimpleVectorStore.add() 不允许空列表，没加载到文档时跳过，避免启动失败
        if (markdowns.isEmpty()) {
            log.warn("未加载到任何 Markdown 文档，跳过向量库初始化");
            return simpleVectorStore;
        }
        simpleVectorStore.add(markdowns);
        return simpleVectorStore;
    }

}
