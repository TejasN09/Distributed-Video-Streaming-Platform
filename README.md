# 🎥 Video Streaming Platform (Microservice Based)

This project is a **video streaming platform** built with **Spring Boot, Angular, MinIO, and FFmpeg**.  
It follows a **microservice architecture** to handle video uploads, processing, and streaming.

---

## 🚀 Features
- Upload raw video files.
- Store uploaded videos in **MinIO (S3-compatible object storage)**.
- Process videos using **FFmpeg** for transcoding and generating streamable formats.
- Serve processed videos for streaming.
- Modular design (microservices-based) for scalability.

---

## 🛠️ Tech Stack
- **Backend:** Spring Boot (Java 17+)
- **Frontend:** Angular
- **Storage:** MinIO (S3-compatible)
- **Transcoding:** FFmpeg
- **Containerization:** Docker

---

## ⚡ Getting Started

### 1. Setup Environment
Make sure you have the following installed:
- Java (JDK 17+)
- Node.js (for Angular frontend)
- Docker
- FFmpeg (must be accessible via command line)

### 2. Run MinIO (Object Storage)
Run MinIO locally using Docker:

```bash
docker run -p 9000:9000 -p 9090:9090 --name minio \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v minio-data:/data \
  quay.io/minio/minio server /data --console-address ":9090"
