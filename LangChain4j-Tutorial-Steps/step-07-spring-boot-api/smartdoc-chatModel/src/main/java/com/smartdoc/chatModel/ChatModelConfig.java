package com.smartdoc.chatModel;

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
@EnableConfigurationProperties(ChatModelProperties.class)
public class ChatModelConfig {

    private final ChatModelProperties chatModelProperties;

    /**
     * Create the synchronous ChatModel Bean.
     * Uses Zhipu native SDK for zhipu provider, OpenAI-compatible API for others.
     */
    @Bean
    public ChatModel chatLanguageModel() {
        ChatModelProperties.ProviderConfig config = chatModelProperties.getActiveConfig();
        String provider = chatModelProperties.getProvider();
        log.info("Initializing ChatModel with provider={}, baseUrl={}, model={}",
                provider, config.getBaseUrl(), config.getModelName());

        if (ProviderEnum.ZHIPU.getCode().equals(provider)) {
            return ZhipuAiChatModel.builder()
                    .apiKey(config.getApiKey())
                    .model(config.getModelName())
                    .build();
        }

        // opeanai is compatible with deepseek.
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .build();
    }

    /**
     * Create the streaming StreamingChatModel Bean.
     * Required for SSE (Server-Sent Events) — tokens must be pushed one-by-one.
     */
    @Bean
    public StreamingChatModel streamingChatModel() {
        ChatModelProperties.ProviderConfig config = chatModelProperties.getActiveConfig();
        String provider = chatModelProperties.getProvider();
        log.info("Initializing StreamingChatModel with provider={}, baseUrl={}, model={}",
                provider, config.getBaseUrl(), config.getModelName());

        if (ProviderEnum.ZHIPU.getCode().equals(provider)) {
            return ZhipuAiStreamingChatModel.builder()
                    .apiKey(config.getApiKey())
                    .model(config.getModelName())
                    .build();
        }

        // opeanai is compatible with deepseek.
        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .build();
    }
}
