package com.smartdoc.chatModel;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM configuration properties bound to the "llm" prefix in application.yml.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "llm")
public class ChatModelProperties {

    private String provider = ProviderEnum.DEEPSEEK.getCode();

    private ProviderConfig zhipu = new ProviderConfig();
    private ProviderConfig deepseek = new ProviderConfig();

    private Map<String, ProviderConfig> providers = new HashMap<>();

    @PostConstruct
    void initProviders() {
        providers.put(ProviderEnum.ZHIPU.getCode(), zhipu);
        providers.put(ProviderEnum.DEEPSEEK.getCode(), deepseek);
    }

    /**
     * Get the configuration for the currently active provider.
     */
    public ProviderConfig getActiveConfig() {
        ProviderConfig config = providers.get(provider.toLowerCase());
        if (config == null) {
            throw new IllegalArgumentException("Unknown LLM provider: '" + provider + "'. Supported: " + providers.keySet());
        }
        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("your-api-key-here")) {
            throw new IllegalStateException(
                    "API Key not configured for provider '" + provider + "'. " +
                            "Set environment variable " + provider.toUpperCase() + "_API_KEY or " +
                            "configure llm." + provider + ".api-key in application.yml");
        }
        return config;
    }

    /**
     * Connection settings for a single LLM provider.
     */
    @Getter
    @Setter
    public static class ProviderConfig {
        private String baseUrl;

        private String apiKey;

        private String modelName;
    }
}
