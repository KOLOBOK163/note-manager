package com.ksbk.auth.service;

import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;


@Service
public class MinioService {

    @Value("${minio.bucket}")
    private String bucket;

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadAvatar(MultipartFile file, String objectName) throws Exception {
        try {
            logger.info("Uploading new avatar to MinIO: {}", objectName);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            logger.info("Successfully uploaded avatar to MinIO: {}", objectName);
            return objectName;
        } catch (Exception e) {
            logger.error("Error uploading avatar to MinIO: {}", objectName, e);
            throw e;
        }
    }

    public InputStream getAvatar(String objectName) throws Exception {
        try {
            logger.debug("Fetching avatar from MinIO: {}", objectName);
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching avatar from MinIO: {}", objectName, e);
            throw e;
        }
    }

    public void deleteAvatar(String objectName) throws Exception {
        try {
            if (objectName == null || objectName.trim().isEmpty()) {
                logger.warn("Attempted to delete null or empty avatar path");
                return;
            }
            
            logger.info("Deleting avatar from MinIO: {}", objectName);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName.trim())
                            .build()
            );
            logger.info("Successfully deleted avatar from MinIO: {}", objectName);
        } catch (Exception e) {
            logger.error("Error deleting avatar from MinIO: {}", objectName, e);
            throw e;
        }
    }
}
