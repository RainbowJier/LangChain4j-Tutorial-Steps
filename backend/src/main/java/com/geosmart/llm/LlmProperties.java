package com.geosmart.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    private String provider = "deepseek";
    private ProviderConfig deepseek = new ProviderConfig();
    private ProviderConfig openai = new ProviderConfig();

    @Data
    public static class ProviderConfig {
        private String baseUrl;
        private String apiKey;
        private String modelName;
    }

    /**
     * Returns the active provider configuration based on the configured provider name.
     */
    public ProviderConfig getActiveConfig() {
        return switch (provider.toLowerCase()) {
            case "openai" -> openai;
            default -> deepseek;
        };
    }
}
