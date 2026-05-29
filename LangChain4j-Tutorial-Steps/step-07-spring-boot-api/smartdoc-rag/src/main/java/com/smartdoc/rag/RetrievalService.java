package com.smartdoc.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * RAG 内容检索服务
 *
 * 【Step 06 对照】Step 06 中我们在 main() 里手动创建检索器：
 *
 *   // Step 06 的写法：
 *   ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
 *       .embeddingStore(embeddingStore)
 *       .embeddingModel(embeddingModel)
 *       .maxResults(5)
 *       .build();
 *
 * Spring Boot 方式：封装为 @Service，通过 @Getter 暴露 contentRetriever，
 * maxResults 从 application.yml 读取（rag.max-results）。
 *
 * 这个 Service 会被 ChatConfig 注入到 AiServices 构建器中，
 * 在每次对话时自动检索相关文档片段，提供给 LLM 作为上下文。
 *
 * @see EmbeddingStoreContentRetriever
 */
@Slf4j
@Getter
@Service
public class RetrievalService {

    /** LangChain4j 内容检索器，在 ChatConfig 中注入到 AiService */
    private final ContentRetriever contentRetriever;

    /**
     * 构造检索服务
     *
     * 【Step 06 对照】Step 06 中手动传递 embeddingStore 和 embeddingModel，
     * 这里通过构造函数注入自动获取 Spring Bean。
     *
     * @param store          向量存储 Bean（由 EmbeddingConfig 创建）
     * @param embeddingModel 向量模型 Bean（由 LlmConfig 创建）
     * @param maxResults     最大检索结果数，配置路径 rag.max-results，默认 5
     */
    public RetrievalService(EmbeddingStore<TextSegment> store,
                            EmbeddingModel embeddingModel,
                            @Value("${rag.max-results:5}") int maxResults) {
        this.contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .build();
        log.info("RetrievalService initialized: maxResults={}", maxResults);
    }
}
