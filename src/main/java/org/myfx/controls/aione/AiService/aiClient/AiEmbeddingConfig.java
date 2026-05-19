package org.myfx.controls.aione.AiService.aiClient;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiEmbeddingConfig {

    @Primary
    @Bean
    public EmbeddingModel embeddingModel(OllamaEmbeddingModel ollamaEmbeddingModel) {  // 或 OpenAiEmbeddingModel
        return ollamaEmbeddingModel;  // 明确指定用 Ollama 的
    }

    @Bean
    public OllamaEmbeddingModel ollamaEmbeddingModel() {  // 或 OpenAiEmbeddingModel
        return OllamaEmbeddingModel
                .builder()
                .ollamaApi(
                        OllamaApi
                                .builder()
                                .build()
                )
                .defaultOptions(
                        OllamaEmbeddingOptions
                                .builder()
                                .model("bge-m3")
                                .build()
                )
                .build();
    }
}