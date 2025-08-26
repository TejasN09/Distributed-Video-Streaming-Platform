package com.streaming.video_service.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    public void uploadFile(String bucketName, String objectName, MultipartFile file) {
        try(InputStream inputStream = file.getInputStream()){
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }catch (Exception ex){
            throw new RuntimeException("Error uploading file to MinIO: " + ex.getMessage());
        }
    }

    public void uploadFile(String bucketName, String objectName, File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.length(), -1)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error uploading File to MinIO: " + e.getMessage());
        }
    }

    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file from MinIO: " + e.getMessage());
        }
    }
}
