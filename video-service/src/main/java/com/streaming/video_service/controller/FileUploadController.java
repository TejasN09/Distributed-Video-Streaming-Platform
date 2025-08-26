package com.streaming.video_service.controller;

import com.streaming.video_service.service.FileStorageService;
import com.streaming.video_service.service.TranscodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {
    private final FileStorageService fileStorageService;
    private final TranscodingService transcodingService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        // We'll upload to the "raw-videos" bucket for now.
        // The object name can be the original filename.
        fileStorageService.uploadFile("raw-videos", file.getOriginalFilename(), file);
        transcodingService.transcodeVideo("raw-videos", file.getOriginalFilename());
        return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
    }
}