package com.poti.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
@EnableElasticsearchRepositories(basePackages = "com.poti.search.repository")
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.rest.uris:http://localhost:9200}")
    private String elasticsearchUrl;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        String host = elasticsearchUrl.replace("http://", "").replace("https://", "");
        String[] parts = host.split(":");
        String hostname = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
        
        RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, port, "http"));
        return new RestHighLevelClient(builder);
    }
}
