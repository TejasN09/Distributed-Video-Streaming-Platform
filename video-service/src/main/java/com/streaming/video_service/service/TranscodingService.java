package com.streaming.video_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscodingService {

    @Autowired
    private FileStorageService fileStorageService;

    public void transcodeVideo(String rawVideoBucket, String rawVideoObjectName) {
        Path tempRawVideoFile = null;
        Path tempOutputDir = null;

        try (InputStream inputStream = fileStorageService.downloadFile(rawVideoBucket, rawVideoObjectName)) {
            // Temp file for raw input
            tempRawVideoFile = Files.createTempFile("raw-video-", ".tmp");
            Files.copy(inputStream, tempRawVideoFile, StandardCopyOption.REPLACE_EXISTING);

            // Temp directory for output
            tempOutputDir = Files.createTempDirectory("hls-output-");

            Process process = getProcess(tempRawVideoFile.toFile(), tempOutputDir.toFile());

            // Capture logs
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            }

            int exitValue = process.waitFor();
            if (exitValue != 0) {
                throw new RuntimeException("FFmpeg process failed with exit code: " + exitValue);
            }

            log.info("FFmpeg process completed successfully");

            // Upload generated files
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempOutputDir)) {
                for (Path file : stream) {
                    fileStorageService.uploadFile(
                            "processed-videos",
                            rawVideoObjectName + "/" + file.getFileName().toString(),
                            file.toFile()
                    );
                }
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt(); // best practice for InterruptedException
            throw new RuntimeException("Error during video transcoding", e);
        } finally {
            // Cleanup
            safeDelete(tempRawVideoFile);
            safeDeleteDir(tempOutputDir);
        }
    }

    private static Process getProcess(File tempRawVideoFile, File tempOutputDir) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", tempRawVideoFile.getAbsolutePath(),
                "-c:v", "libx264",
                "-c:a", "aac",
                "-hls_time", "10",
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", new File(tempOutputDir, "segment%03d.ts").getAbsolutePath(),
                new File(tempOutputDir, "master.m3u8").getAbsolutePath()
        );

        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    private static void safeDelete(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.warn("Failed to delete temp file: " + path, e);
            }
        }
    }

    private static void safeDeleteDir(Path dir) {
        if (dir != null) {
            try {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                log.warn("Failed to delete temp directory: {}", dir, e);
            }
        }
    }
}
