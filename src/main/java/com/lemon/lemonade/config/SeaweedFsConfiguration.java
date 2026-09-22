package com.lemon.lemonade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class SeaweedFsConfiguration {

    // SeaweedFS speaks the S3 API, so the AWS SDK's S3 client works against it
    @Bean
    public S3Client s3Client(
            @Value("${seaweedfs.s3.endpoint}") URI endpoint,
            @Value("${seaweedfs.s3.access-key}") String accessKey,
            @Value("${seaweedfs.s3.secret-key}") String secretKey) {
        return S3Client.builder()
                .endpointOverride(endpoint)
                // SeaweedFS ignores the region, but the SDK requires one
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                // http://localhost:8333/music/song.mp3 instead of http://music.localhost:8333/song.mp3
                .forcePathStyle(true)
                .build();
    }
}
