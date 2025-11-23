package com.reliaquest.api.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private static final int CONNECTION_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.setConnectTimeout(Duration.ofMillis(CONNECTION_TIMEOUT_MS))
                .setReadTimeout(Duration.ofMillis(READ_TIMEOUT_MS))
                .requestFactory(this::clientHttpRequestFactory)
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        return new BufferingClientHttpRequestFactory(factory);
    }
}
