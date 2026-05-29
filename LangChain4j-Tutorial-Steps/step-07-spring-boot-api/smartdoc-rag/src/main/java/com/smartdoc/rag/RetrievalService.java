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
 * RAG Content retrieval service.
 */
@Slf4j
@Getter
@Service
public class RetrievalService {

    private final ContentRetriever contentRetriever;

    /**
     * Maximum number of retrieval result, default 5
     */
    @Value("${rag.max-results:5}")
    private int maxResults;

    /**
     * construct retrieval service.
     *
     * @param store          EmbeddingStore Bean
     * @param embeddingModel EmbeddingStore Bean
     */
    public RetrievalService(EmbeddingStore<TextSegment> store, EmbeddingModel embeddingModel) {
        this.contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .build();
        log.info("RetrievalService initialized: maxResults={}", maxResults);
    }
}
