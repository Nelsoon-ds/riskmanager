package com.nelson.riskmanager.config;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeaviateConfig {

    @Value("${spring.ai.vectorstore.weaviate.scheme}")
    private String scheme;

    @Value("${spring.ai.vectorstore.weaviate.host}")
    private String host;

    @Value("${spring.ai.vectorstore.weaviate.object-class}")
    private String objectClass;

    @Bean
    public WeaviateClient weaviateClient() {
        return new WeaviateClient(new Config(scheme, host));
    }

    @Bean
    public WeaviateVectorStore vectorStore(WeaviateClient weaviateClient, EmbeddingModel embeddingModel) {
        return WeaviateVectorStore.builder(weaviateClient, embeddingModel)
                .objectClass(objectClass)
                .build();
    }
}
