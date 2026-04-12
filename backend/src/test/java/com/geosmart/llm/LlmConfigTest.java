package com.geosmart.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class LlmConfigTest {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private StreamingChatModel streamingChatModel;

    @Test
    void chatModelBeanShouldBeCreated() {
        assertThat(chatModel).isNotNull();
    }

    @Test
    void streamingChatModelBeanShouldBeCreated() {
        assertThat(streamingChatModel).isNotNull();
    }

    @Test
    void llmProperties_bindsDefaultsCorrectly() {
        LlmProperties props = new LlmProperties();

        assertThat(props.getProvider()).isEqualTo("deepseek");
        assertThat(props.getDeepseek()).isNotNull();
        assertThat(props.getOpenai()).isNotNull();
    }

    @Test
    void getActiveConfig_returnsDeepseekByDefault() {
        LlmProperties props = new LlmProperties();
        LlmProperties.ProviderConfig deepseek = props.getDeepseek();
        deepseek.setBaseUrl("https://api.deepseek.com");
        deepseek.setApiKey("test-key");
        deepseek.setModelName("deepseek-chat");

        LlmProperties.ProviderConfig active = props.getActiveConfig();
        assertThat(active.getBaseUrl()).isEqualTo("https://api.deepseek.com");
        assertThat(active.getApiKey()).isEqualTo("test-key");
        assertThat(active.getModelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void getActiveConfig_returnsOpenaiWhenConfigured() {
        LlmProperties props = new LlmProperties();
        props.setProvider("openai");

        LlmProperties.ProviderConfig openai = props.getOpenai();
        openai.setBaseUrl("https://api.openai.com");
        openai.setApiKey("sk-test");
        openai.setModelName("gpt-4o");

        LlmProperties.ProviderConfig active = props.getActiveConfig();
        assertThat(active.getBaseUrl()).isEqualTo("https://api.openai.com");
        assertThat(active.getApiKey()).isEqualTo("sk-test");
        assertThat(active.getModelName()).isEqualTo("gpt-4o");
    }

    @Test
    void getActiveConfig_isCaseInsensitive() {
        LlmProperties props = new LlmProperties();
        props.setProvider("DeepSeek");

        LlmProperties.ProviderConfig deepseek = props.getDeepseek();
        deepseek.setBaseUrl("https://api.deepseek.com");
        deepseek.setApiKey("test-key");
        deepseek.setModelName("deepseek-chat");

        assertThat(props.getActiveConfig().getBaseUrl()).isEqualTo("https://api.deepseek.com");
    }
}
