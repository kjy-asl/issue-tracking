package com.example.msa.posrelay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

/**
 * SqsAsyncClient를 명시적으로 구성하여 LocalStack 엔드포인트를 지원한다.
 * Spring Cloud AWS 자동 구성이 이 Bean을 사용한다(@ConditionalOnMissingBean 백오프).
 */
@Configuration
public class SqsConfig {

    @Bean
    public SqsAsyncClient sqsAsyncClient(
            @Value("${pos.sqs.endpoint}") String endpoint,
            @Value("${pos.sqs.region:us-east-1}") String region,
            @Value("${pos.sqs.access-key:test}") String accessKey,
            @Value("${pos.sqs.secret-key:test}") String secretKey) {
        return SqsAsyncClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
