package com.streaming.video_service.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    public void uploadFile(String bucketName, String objectName, MultipartFile file) {
        try{
            InputStream inputStream = file.getInputStream();

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
}
