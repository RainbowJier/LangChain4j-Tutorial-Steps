package com.smartdoc.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LLM 配置属性类
 *
 * 【Step 06 对照】Step 06 中我们硬编码了 API Key 和模型名称：
 *   String apiKey = "your-api-key";
 *   String modelName = "glm-4-flash";
 *
 * Spring Boot 方式：将配置外部化到 application.yml，
 * 通过 @ConfigurationProperties 自动绑定，支持多提供商切换。
 *
 * application.yml 示例：
 *   llm:
 *     provider: zhipu
 *     zhipu:
 *       api-key: xxx
 *       model-name: glm-4-flash
 *     deepseek:
 *       base-url: https://api.deepseek.com
 *       api-key: xxx
 *       model-name: deepseek-chat
 *
 * @see LlmConfig
 */
@Data
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    /**
     * 当前激活的 LLM 提供商：zhipu / deepseek / openai
     * 可通过环境变量 LLM_PROVIDER 覆盖
     */
    private String provider = "zhipu";

    /** 智谱 AI 配置 */
    private ProviderConfig zhipu = new ProviderConfig();

    /** DeepSeek 配置 */
    private ProviderConfig deepseek = new ProviderConfig();

    /** OpenAI 配置 */
    private ProviderConfig openai = new ProviderConfig();

    /**
     * 单个提供商的连接配置
     *
     * 【Step 06 对照】Step 06 中直接 new ZhipuAiChatModel.builder().apiKey("...").build()
     * 这里把 apiKey / baseUrl / modelName 统一抽象为配置项
     */
    @Data
    public static class ProviderConfig {
        /** API 基础 URL（OpenAI 兼容接口需要） */
        private String baseUrl;
        /** API 密钥 */
        private String apiKey;
        /** 模型名称，如 glm-4-flash、deepseek-chat、gpt-4o */
        private String modelName;
    }

    /**
     * 根据当前 provider 配置返回对应的 ProviderConfig
     *
     * 【Step 06 对照】Step 06 只支持一个提供商，这里支持运行时切换
     */
    public ProviderConfig getActiveConfig() {
        return switch (provider.toLowerCase()) {
            case "openai" -> openai;
            case "zhipu" -> zhipu;
            default -> deepseek;
        };
    }
}
