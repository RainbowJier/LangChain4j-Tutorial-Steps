package com.geosmart.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfig {

    private final LlmProperties llmProperties;

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

        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .build();
    }

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
}
