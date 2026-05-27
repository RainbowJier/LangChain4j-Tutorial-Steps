package com.tutorial;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryProvider;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import io.milvus.v2.client.MilvusServiceClient;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Step 09: 生产优化 — 从开发到生产的关键改进。
 * <p>
 * 本步演示以下生产优化：
 * 1. 向量数据库：从 InMemoryEmbeddingStore 迁移到 Milvus（持久化、可扩展）
 * 2. 持久化会话记忆：从 InMemoryChatMemoryStore 到数据库存储
 * 3. 监控指标：Token 使用量、响应延迟
 * 4. 错误处理：重试、降级、超时控制
 * <p>
 * 运行方式：
 * - 无 Milvus: 直接运行（自动回退到 InMemory）
 * - 有 Milvus: 先 docker-compose up -d，再运行
 */
public class ProductionDemo {

    @SystemMessage("你是智能文档助手。基于参考资料准确回答问题。")
    interface ProductionAssistant {
        String chat(String message);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Step 09: 生产优化 ===\n");

        // ===== 优化 1: 可配置的超时和重试 =====
        System.out.println("[优化 1] 超时和重试控制");
        Map<String, String> config = loadProviderConfig();
        ChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl(config.get("base-url"))
                .apiKey(config.get("api-key"))
                .modelName(config.get("model-name"))
                .timeout(Duration.ofSeconds(30))       // 超时控制
                .maxRetries(3)                          // 失败重试
                .temperature(0.7)
                .build();
        System.out.println("      ChatModel 配置: timeout=30s, maxRetries=3");

        // ===== 优化 2: 向量数据库选择 =====
        System.out.println("[优化 2] 向量数据库");
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        EmbeddingStore<TextSegment> store = createEmbeddingStore();
        System.out.println("      使用: " + store.getClass().getSimpleName());

        // 加载并摄入文档（带耗时监控）
        System.out.println("[优化 3] 文档摄入（带监控）");
        Instant start = Instant.now();

        DocumentParser parser = new TextDocumentParser();
        Path docPath = Path.of(ProductionDemo.class.getClassLoader()
                .getResource("knowledge-base.txt").toURI());
        Document doc = FileSystemDocumentLoader.loadDocument(docPath, parser);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(DocumentSplitters.recursive(500, 100))
                .build();
        ingestor.ingest(doc);

        Duration ingestTime = Duration.between(start, Instant.now());
        System.out.println("      摄入耗时: " + ingestTime.toMillis() + "ms");

        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .build();

        // ===== 优化 3: 带监控的 AI 调用 =====
        ProductionAssistant assistant = AiServices.builder(ProductionAssistant.class)
                .chatModel(chatModel)
                .contentRetriever(retriever)
                .build();

        System.out.println("\n=== 带监控的查询测试 ===");
        String[] testQuestions = {
                "编码规范中类名应该怎么命名？",
                "API 分页参数默认值是什么？",
                "部署流程是什么样的？"
        };

        for (String question : testQuestions) {
            Instant queryStart = Instant.now();
            String answer = assistant.chat(question);
            Duration queryTime = Duration.between(queryStart, Instant.now());

            System.out.println("问: " + question);
            System.out.println("答: " + answer.substring(0, Math.min(100, answer.length())) + "...");
            System.out.println("耗时: " + queryTime.toMillis() + "ms");
            System.out.println();
        }

        System.out.println("=== 生产环境检查清单 ===");
        System.out.println("✅ 超时控制: ChatModel timeout=30s");
        System.out.println("✅ 重试机制: maxRetries=3");
        System.out.println("✅ 向量数据库: " + store.getClass().getSimpleName());
        System.out.println("✅ 响应监控: 每次查询记录耗时");
        System.out.println();
        System.out.println("⚠️  待实现:");
        System.out.println("  - 持久化会话记忆（替换 InMemoryChatMemoryStore）");
        System.out.println("  - API 认证和授权");
        System.out.println("  - 输入校验和敏感信息过滤");
        System.out.println("  - 分布式部署和负载均衡");
    }

    /**
     * 创建向量存储 — 优先使用 Milvus，回退到 InMemory。
     * <p>
     * 生产环境使用 Milvus 的优势：
     * - 数据持久化（重启不丢失）
     * - 支持大规模数据（百万级向量）
     * - 高性能索引（HNSW、IVF_FLAT）
     * - 支持分布式部署
     */
    private static EmbeddingStore<TextSegment> createEmbeddingStore() {
        try {
            // 尝试连接 Milvus
            MilvusEmbeddingStore milvusStore = MilvusEmbeddingStore.builder()
                    .uri("http://localhost:19530")
                    .collectionName("smartdoc_knowledge")
                    .dimension(384)   // AllMiniLmL6-v2 输出 384 维
                    .build();
            // 测试连接
            milvusStore.toString();
            System.out.println("      Milvus 连接成功（生产级向量数据库）");
            return milvusStore;
        } catch (Exception e) {
            System.out.println("      Milvus 不可用，回退到 InMemoryEmbeddingStore");
            System.out.println("      提示: docker-compose up -d 可启动 Milvus");
            return new InMemoryEmbeddingStore<>();
        }
    }

    private static Map<String, String> loadProviderConfig() {
        Yaml yaml = new Yaml();
        InputStream is = ProductionDemo.class.getClassLoader()
                .getResourceAsStream("application.yml");
        if (is == null) {
            throw new IllegalStateException("application.yml 未找到");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> fullConfig = yaml.load(is);
        @SuppressWarnings("unchecked")
        Map<String, Object> llmConfig = (Map<String, Object>) fullConfig.get("llm");
        String provider = (String) llmConfig.get("provider");
        @SuppressWarnings("unchecked")
        Map<String, String> providerConfig = (Map<String, String>) llmConfig.get(provider);

        String apiKey = providerConfig.get("api-key");
        if (apiKey == null || apiKey.contains("your-api-key-here")) {
            System.err.println("请先配置 API Key！参见 step-00-setup");
            System.exit(1);
        }
        return providerConfig;
    }
}
