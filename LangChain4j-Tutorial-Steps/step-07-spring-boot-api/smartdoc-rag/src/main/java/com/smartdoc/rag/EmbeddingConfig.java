package com.smartdoc.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储配置
 *
 * 【Step 06 对照】Step 06 中我们在 main() 里直接创建：
 *
 *   // Step 06 的写法：
 *   EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
 *
 * Spring Boot 方式：通过 @Bean 注册，所有需要向量存储的地方自动注入同一个实例。
 * 这保证了文档摄入和检索使用的是同一个存储。
 *
 * 注意：InMemoryEmbeddingStore 数据保存在内存中，重启后丢失。
 * 生产环境应替换为 Milvus、pgvector 等持久化存储。
 */
@Configuration
public class EmbeddingConfig {

    /**
     * 向量存储 Bean（单例）
     *
     * 整个应用共享同一个 EmbeddingStore 实例：
     * - DocumentIngestionService 向其中写入文档向量
     * - RetrievalService 从其中检索相关文档
     *
     * 【Step 06 对照】Step 06 中需要手动传递 embeddingStore 引用，
     * Spring Boot 通过依赖注入自动保证了引用一致性。
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
}
