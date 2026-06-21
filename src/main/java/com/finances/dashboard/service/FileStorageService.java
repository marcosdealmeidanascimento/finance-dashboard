package com.finances.dashboard.service;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class FileStorageService {

    private static final String BUCKET_NAME = "finances-dashboard";
    private static final String ENDPOINT = "http://localhost:9000";
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public FileStorageService() {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                "fin_dashboard_user",
                "12345678");

        StaticCredentialsProvider provider = StaticCredentialsProvider.create(credentials);

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(ENDPOINT))
                .region(Region.US_EAST_1)
                .credentialsProvider(provider)
                .forcePathStyle(true)
                .build();

        this.s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(ENDPOINT))
                .region(Region.US_EAST_1)
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build())
                .credentialsProvider(provider)
                .build();
    }

    public String uploadFile(MultipartFile file) {
        try {
            String fileKey = System.currentTimeMillis()
                    + "-" +
                    file.getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()));
            return fileKey;
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload file",
                    e);
        }
    }

    public String generatePresignedUrl(String fileKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(fileKey)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(request)
                .build();
        return s3Presigner
                .presignGetObject(presignRequest)
                .url()
                .toString();
    }
}