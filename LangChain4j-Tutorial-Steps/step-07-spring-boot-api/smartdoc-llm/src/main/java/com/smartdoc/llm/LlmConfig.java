package com.smartdoc.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 模型配置类
 *
 * 【Step 06 对照】Step 06 中我们在 main() 方法里直接创建模型实例：
 *
 *   // Step 06 的写法：
 *   ChatModel chatModel = ZhipuAiChatModel.builder()
 *       .apiKey("your-key")
 *       .model("glm-4-flash")
 *       .build();
 *
 * Spring Boot 方式：用 @Configuration + @Bean 将模型实例注册为 Spring Bean，
 * 由 IoC 容器管理生命周期，其他模块通过依赖注入获取。
 *
 * 核心变化：
 * - 手动创建 → @Bean 由 Spring 管理
 * - 硬编码配置 → 从 application.yml 读取（通过 LlmProperties）
 * - 单一提供商 → 运行时切换（zhipu/deepseek/openai）
 *
 * @see LlmProperties
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfig {

    private final LlmProperties llmProperties;

    /**
     * 创建同步 ChatModel Bean
     *
     * 【Step 06 对照】Step 06 的 ChatModel 只在一个地方使用。
     * 这里注册为 Bean 后，任何需要 ChatModel 的地方都可以通过构造函数注入获取。
     *
     * Spring 保证整个应用只有一个 ChatModel 实例（单例模式由容器保证）。
     */
    @Bean
    public ChatModel chatLanguageModel() {
        LlmProperties.ProviderConfig config = llmProperties.getActiveConfig();
        String provider = llmProperties.getProvider().toLowerCase();
        log.info("Initializing ChatModel with provider={}, baseUrl={}, model={}",
                provider, config.getBaseUrl(), config.getModelName());

        if ("zhipu".equals(provider)) {
            return ZhipuAiChatModel.builder()
                    .apiKey(config.getApiKey())
                    .model(config.getModelName())
                    .build();
        }

        // DeepSeek 和 OpenAI 都使用 OpenAI 兼容接口
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .build();
    }

    /**
     * 创建流式 StreamingChatModel Bean
     *
     * 【Step 06 对照】Step 06 中 StreamingChatModel 用于 TokenStream 流式输出。
     * 这里注册为 Bean，供 ChatController 的 SSE 端点使用。
     *
     * SSE（Server-Sent Events）需要逐 Token 推送给前端，因此必须使用流式模型。
     */
    @Bean
    public StreamingChatModel streamingChatModel() {
        LlmProperties.ProviderConfig config = llmProperties.getActiveConfig();
        String provider = llmProperties.getProvider().toLowerCase();
        log.info("Initializing StreamingChatModel with provider={}, baseUrl={}, model={}",
                provider, config.getBaseUrl(), config.getModelName());

        if ("zhipu".equals(provider)) {
            return ZhipuAiStreamingChatModel.builder()
                    .apiKey(config.getApiKey())
                    .model(config.getModelName())
                    .build();
        }

        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .build();
    }

    /**
     * 创建本地 Embedding 模型 Bean
     *
     * 【Step 06 对照】Step 06 中直接 new AllMiniLmL6V2EmbeddingModel()。
     * 这里注册为 Bean，供 RAG 模块的 DocumentIngestionService 和 RetrievalService 注入使用。
     *
     * AllMiniLmL6V2 是一个 ONNX 格式的本地模型，无需网络调用，
     * 适合开发和教学环境。生产环境可替换为远程 Embedding API。
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Initializing EmbeddingModel: AllMiniLmL6V2 (local ONNX model)");
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
