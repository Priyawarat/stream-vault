# StreamVault 🎬

> **StreamVault is a video hosting and streaming platform built to demonstrate real-world backend engineering concepts such as asynchronous processing, Kafka, the Outbox Pattern, retries, DLQ handling, idempotency, concurrency protection, FFmpeg/FFprobe processing, JWT security, and HTTP Byte-Range streaming.**

Built as a focused hackathon project with one goal: **show strong backend engineering through a complete working video-processing workflow.**

---

## 📑 Table of Contents

- [Why StreamVault?](#-why-streamvault)
- [Key Highlights](#-key-highlights)
- [Technology Stack](#-technology-stack)
- [System Architecture](#-system-architecture)
- [End-to-End Flow](#-end-to-end-flow)
- [Video Lifecycle](#-video-lifecycle)
- [Outbox Pattern](#-outbox-pattern)
- [Kafka, Retries & DLQ](#-kafka-retries--dlq)
- [Idempotency & Concurrency Protection](#-idempotency--concurrency-protection)
- [FFprobe & FFmpeg](#-ffprobe--ffmpeg)
- [Video Variations & Streaming](#-video-variations--streaming)
- [HTTP Byte-Range Streaming](#-http-byte-range-streaming)
- [Authentication & Authorization](#-authentication--authorization)
- [Database Design](#-database-design)
- [Status Transition History](#-status-transition-history)
- [API Overview](#-api-overview)
- [Local Setup](#-local-setup)
- [Docker Setup](#-docker-setup)
- [Production Evolution](#-production-evolution)
- [Technical Summary](#-technical-summary)

---

## 💡 Why StreamVault?

A basic video application can save a file and return a URL. StreamVault treats video processing as a **reliable asynchronous workflow**.

The system is designed around a few important questions:

- How do we make sure a database event is not lost before Kafka receives it?
- What happens when Kafka or video processing fails?
- What happens when the same event is delivered again?
- What happens when two processing attempts race for the same video?
- How can large video files be streamed efficiently?

That engineering around the video upload is the core of StreamVault.

---

## ✨ Key Highlights

### 🎥 Video Platform
- User authentication and authorization
- Video upload and ownership validation
- Local filesystem storage
- Video metadata management
- Thumbnail generation
- Video variations / adaptive streaming
- HTTP Byte-Range streaming

### ⚡ Event-Driven Processing
- Apache Kafka for asynchronous processing
- Outbox Pattern for reliable event publication
- Scheduler-based outbox delivery
- Event IDs for processing identity and correlation

### 🛡️ Reliability
- Database-backed processing claims
- Duplicate event protection
- Concurrent processing protection
- Bounded Kafka retries
- Dead Letter Queue handling
- Outbox retry handling
- Outbox crash recovery

### 🎬 Media Processing
- FFprobe metadata extraction
- FFmpeg processing
- Processed media generation
- Persistent video status transitions

---

## 🧰 Technology Stack

| Technology | Purpose |
|---|---|
| **Java** | Backend implementation |
| **Spring Boot** | REST API and application framework |
| **Spring Security + JWT** | Authentication and authorization |
| **PostgreSQL** | Persistent application and workflow state |
| **Apache Kafka** | Asynchronous video-processing events |
| **FFprobe** | Video metadata extraction |
| **FFmpeg** | Video processing |
| **Spring Data JPA / Hibernate** | Database persistence |
| **Maven** | Build and dependency management |
| **Local Filesystem** | Video file storage |
| **Docker / Docker Compose** | Local multi-service runtime |

---

## 🏗️ System Architecture

### 👤 From Registration to Video Playback

```text
                         👤 User
                           │
                           ▼
                  ┌─────────────────────┐
                  │ 📝 Register         │
                  │ Create account      │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │ 🔐 Login            │
                  │ Receive JWT         │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │ 🎥 Upload Video     │
                  │ Authenticated API   │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │ 💾 Local Storage    │
                  │ Save original file  │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │ 🗄️ PostgreSQL       │
                  │ Status = UPLOADED  │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │ 📦 Outbox Event     │
                  │ VIDEO_UPLOADED      │
                  └──────────┬──────────┘
                             │
                     ✅ Transaction Commit
                             │
                             ▼
                  ┌─────────────────────┐
                  │ ⏱️ Outbox Scheduler │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │ 📨 Kafka            │
                  │ video-uploaded      │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │ 🎬 Video Processing │
                  │ UPLOADED → PROCESSING
                  └──────────┬──────────┘
                             │
                 ┌───────────┼───────────┐
                 │           │           │
                 ▼           ▼           ▼
         ┌────────────┐ ┌────────────┐ ┌─────────────┐
         │ 🔎 FFprobe │ │ ⚙️ FFmpeg  │ │ 🖼️ Thumbnail│
         │ Metadata   │ │ Variations │ │ Generation  │
         └─────┬──────┘ └─────┬──────┘ └──────┬──────┘
               │              │               │
               └──────────────┼───────────────┘
                              ▼
                  ┌─────────────────────┐
                  │ 🗄️ Update PostgreSQL│
                  │ Metadata + Status   │
                  └──────────┬──────────┘
                             │
                    ┌────────┴────────┐
                    ▼                 ▼
           ✅ READY / Playback   ❌ FAILED / DLQ
                    │
                    ▼
           📺 Range Streaming
                    │
                    ▼
                 🎬 Watch
```

The architecture follows one clear path: **authenticate → upload → persist → publish reliably → process asynchronously → prepare playback assets → stream the result**.

### 🔄 Processing Path

```text
📨 Kafka: video-uploaded
        │
        ▼
🎬 Video Processing
        │
        ├──────────────► 🔎 FFprobe
        │                    │
        │                    ▼
        │              📊 Video Metadata
        │
        ├──────────────► ⚙️ FFmpeg
        │                    │
        │          ┌─────────┼─────────┐
        │          ▼         ▼         ▼
        │       🎞️ 1080p  🎞️ 720p  🎞️ 480p
        │
        └──────────────► 🖼️ Thumbnail
                              │
                              ▼
                     🗄️ PostgreSQL
                              │
                     ┌────────┴────────┐
                     ▼                 ▼
                  ✅ READY          ❌ FAILED
```

The important separation is simple:

**HTTP handles the request. PostgreSQL stores durable state. Outbox delivers the event. Kafka drives asynchronous processing. FFprobe/FFmpeg handle media work. The streaming API serves the processed media.**

---

## 🚀 End-to-End Flow

### 🎬 Upload → Processing → Playback

```text
👤 User
  │
  │  Upload video
  ▼
┌─────────────────────────────┐
│ 🔐 Authenticate request     │
│ ✅ Validate video            │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ 💾 Store original video     │
│    Local Filesystem          │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ 🗄️ Save video metadata      │
│    Status = UPLOADED        │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ 📦 Save VIDEO_UPLOADED      │
│    Outbox Event              │
└──────────────┬──────────────┘
               │
               ▼
        ✅ Transaction Commit
               │
               ▼
┌─────────────────────────────┐
│ ⏱️ Outbox Scheduler          │
│    PENDING → PROCESSING     │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ 📨 Kafka                     │
│    video-uploaded            │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ 🎬 Video Processing          │
│    UPLOADED → PROCESSING    │
└──────────────┬──────────────┘
               │
        ┌──────┴───────┐
        ▼              ▼
┌───────────────┐  ┌───────────────┐
│ 🔎 FFprobe    │  │ ⚙️ FFmpeg     │
│ Read metadata │  │ Process media │
└───────┬───────┘  └───────┬───────┘
        │                  │
        └─────────┬────────┘
                  ▼
┌─────────────────────────────┐
│ 🗄️ Persist processed data   │
│    Status = READY           │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ 📺 Video can be streamed    │
│    using HTTP Range         │
└─────────────────────────────┘
```

The upload request does **not** wait for FFmpeg to finish. The API persists the upload and returns while the processing pipeline continues asynchronously.

---

## 🧠 Video Lifecycle

```text
┌──────────────┐
│  📤 UPLOADED │
└──────┬───────┘
       │
       │ Kafka event consumed
       ▼
┌──────────────┐
│ ⚙️ PROCESSING│
└──────┬───────┘
       │
       ├──────────────────────┐
       │                      │
       │ success              │ retries exhausted
       ▼                      ▼
┌──────────────┐       ┌──────────────┐
│ ✅ READY     │       │ ❌ FAILED    │
└──────────────┘       └──────────────┘
```

Each important transition is also persisted in the status-transition history.

---

## 🔄 Outbox Pattern

The core consistency problem is the gap between a successful database transaction and an independent Kafka publish.

Without an Outbox:

```text
🗄️ PostgreSQL
     │
     │ Save video ✅
     ▼
📨 Kafka publish ❌

Result: video exists, but the processing event may be lost.
```

StreamVault instead writes the event into the same database transaction:

```text
┌───────────────────────────────────────┐
│           🗄️ Database Transaction     │
│                                       │
│  1. Save Video                        │
│  2. Save VIDEO_UPLOADED Outbox Event  │
│                                       │
│              ✅ COMMIT                 │
└────────────────────┬──────────────────┘
                     │
                     ▼
             ⏱️ Outbox Scheduler
                     │
                     ▼
              📨 Publish Kafka
                     │
                     ▼
          ✅ Mark Event PROCESSED
```

### 📦 Outbox lifecycle

```text
PENDING
   │
   ▼
PROCESSING
   │
   ├──────────────► PROCESSED ✅
   │
   └──────────────► PENDING 🔁
                      │
                      │ retry limit reached
                      ▼
                   FAILED ❌
```

The scheduler also detects stale `PROCESSING` events and returns them to `PENDING`, allowing recovery after an application crash.

---

## 📨 Kafka, Retries & DLQ

```text
              📨 Kafka
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
   🎬 Processing      🔁 Retry
        │                 │
        │ success         │ failure
        ▼                 ▼
     ✅ READY        🔁 Retry again
                          │
                          │ retry limit reached
                          ▼
                     💀 DLQ
                          │
                          ▼
                     ❌ FAILED
```

The main topic is:

```text
video-uploaded
```

Failed messages are routed to:

```text
video-uploaded.DLQ
```

The consumer uses bounded retries with a fixed backoff. A poison message does not remain in an endless retry loop.

---

## 🛡️ Idempotency & Concurrency Protection

### 🔁 Duplicate Event Protection

The same Kafka event can be delivered again. StreamVault uses an atomic database claim instead of an in-memory flag.

```text
📨 Same VIDEO_UPLOADED event
             │
             ▼
┌─────────────────────────────┐
│ Attempt atomic video claim  │
└──────────────┬──────────────┘
               │
        ┌──────┴──────┐
        │             │
     updated=1     updated=0
        │             │
        ▼             ▼
   ✅ Process       ⏭️ Skip
   FFprobe         duplicate
   FFmpeg
```

When the claim returns zero updated rows, the duplicate exits **before FFprobe and FFmpeg run**.

### ⚔️ Concurrent Processing

Two workers can race for the same video:

```text
                 🎬 Same Video
                      │
          ┌───────────┴───────────┐
          ▼                       ▼
      👷 Worker 1             👷 Worker 2
          │                       │
          └───────────┬───────────┘
                      ▼
             🔒 Atomic DB Claim
                      │
             ┌────────┴────────┐
             ▼                 ▼
         ✅ Claim 1        ❌ Claim 0
             │                 │
             ▼                 ▼
          PROCESSING          SKIP
```

This makes the processing claim safe even when requests arrive concurrently.

---

## 💥 Failure Recovery

### 📦 Outbox crash recovery

```text
📦 PENDING
    │
    ▼
⚙️ PROCESSING
    │
    │ 💥 Application crashes
    ▼
⚙️ PROCESSING remains in DB
    │
    │ Application restarts
    ▼
🔎 Stale event detected
    │
    ▼
📦 PENDING again
    │
    ▼
📨 Kafka publish retried
```

### 🎬 Video processing failure

```text
⚙️ PROCESSING
      │
      ▼
   FFprobe / FFmpeg
      │
      │ ❌ Failure
      ▼
   🔁 Kafka Retry
      │
      ├──────────────► Retry 1
      │
      ├──────────────► Retry 2
      │
      └──────────────► Retry exhausted
                           │
                           ▼
                       💀 DLQ
                           │
                           ▼
                      ❌ FAILED
```

The video-processing claim remains tied to the `eventId`, so only the correct processing attempt can finalize the video state.

---

## 🎞️ FFprobe & FFmpeg

### 🔎 FFprobe

FFprobe inspects the uploaded media and provides metadata used by the application, including:

- Duration
- Resolution
- Video codec
- Audio codec

### ⚙️ FFmpeg

FFmpeg performs the actual media-processing stage and creates the processed media used by the streaming layer.

```text
🎥 Original Video
       │
       ▼
   🔎 FFprobe
       │
       ├── Duration
       ├── Resolution
       ├── Video Codec
       └── Audio Codec
       │
       ▼
    ⚙️ FFmpeg
       │
       ▼
🎬 Processed Media
       │
       ▼
✅ READY
```

The external media-process failures are propagated back into the Kafka retry/DLQ workflow.

---

## 🎬 Video Variations & Streaming

Video processing produces playback-ready media representations used by the streaming side of the application.

```text
             🎥 Uploaded Video
                    │
                    ▼
              ⚙️ Processing
                    │
            ┌───────┴────────┐
            ▼                ▼
        🎞️ Variant A      🎞️ Variant B
            │                │
            └───────┬────────┘
                    ▼
             📺 Video Player
```

The important idea is that **processing and playback are separated**. The upload path does not need to understand the details of media delivery.

---

## 📺 HTTP Byte-Range Streaming

Large videos should not require the server to send the entire file for every request.

```text
🎬 Browser / Video Player
          │
          │ Range: bytes=start-end
          ▼
┌──────────────────────────┐
│ 📺 Streaming API         │
│ Validate requested range │
└─────────────┬────────────┘
              │
              ▼
┌──────────────────────────┐
│ 💾 Local Processed File  │
│ Read requested bytes     │
└─────────────┬────────────┘
              │
              ▼
┌──────────────────────────┐
│ ✅ 206 Partial Content   │
│ Content-Range            │
│ Content-Length           │
└─────────────┬────────────┘
              │
              ▼
       🎬 Video Player
```

Example request:

```http
GET /v1/videos/{videoId}/stream
Range: bytes=0-999999
```

Example response:

```http
206 Partial Content
Accept-Ranges: bytes
Content-Range: bytes 0-999999/<total-size>
Content-Length: 1000000
Content-Type: video/mp4
```

For an invalid range:

```http
416 Requested Range Not Satisfiable
Content-Range: bytes */<total-size>
```

The player decides which byte range it needs; the server's responsibility is to validate and return the requested bytes correctly.

---

## 🔐 Authentication & Authorization

```text
👤 User
  │
  ▼
🔑 Login
  │
  ▼
🎫 JWT issued
  │
  ▼
📨 Authenticated request
  │
  ▼
🛡️ Authorization / ownership check
  │
  ▼
✅ Protected video operation
```

JWT-based security protects authenticated resources, while ownership validation ensures users can only perform permitted operations on their videos.

---

## 🗄️ Database Design

PostgreSQL acts as the durable source of truth for application and workflow state.

### Core tables

| Table | Purpose |
|---|---|
| `users` | User identity and authentication data |
| `videos` | Video metadata and current processing state |
| `outbox_events` | Reliable Kafka event delivery |
| `video_status_transitions` | Persistent status history |
| `video_processing_jobs` | Processing-attempt tracking |
| `video_variants` | Playback-ready video representations |

### Relationship overview

```text
👤 USER
   │
   │ 1 : N
   ▼
🎥 VIDEO
   │
   ├──────────────► 📦 OUTBOX_EVENT
   │
   ├──────────────► 🔄 STATUS_TRANSITION
   │
   ├──────────────► ⚙️ PROCESSING_JOB
   │
   └──────────────► 🎞️ VIDEO_VARIANT
```

The database stores **metadata and workflow state**; the actual video files are stored separately on the local filesystem.

---

## 🧾 Status Transition History

The application keeps both the current status and its history.

```text
🎥 VIDEO
   │
   │ current state
   ▼
✅ READY

        +

📜 STATUS HISTORY
   │
   ├── 📤 UPLOADED → ⚙️ PROCESSING
   │
   └── ⚙️ PROCESSING → ✅ READY
```

A failed workflow is represented as:

```text
📤 UPLOADED
      │
      ▼
⚙️ PROCESSING
      │
      ▼
❌ FAILED
```

This makes the processing lifecycle easy to audit without relying only on runtime logs.

---

## 🔌 API Overview

The following REST endpoints are currently implemented in StreamVault.

### 🔐 Authentication APIs

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/v1/users/register` | Register a new user account |
| `POST` | `/v1/users/login` | Authenticate a user and issue an access token; refresh token is stored in an HttpOnly cookie |
| `POST` | `/v1/users/refresh-token` | Generate a new access token using the refresh token cookie |

### 🎥 Video APIs

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/v1/videos/upload` | Upload a video and create its processing workflow |
| `GET` | `/v1/videos` | Retrieve videos currently available with `READY` status |
| `GET` | `/v1/videos/{videoId}/stream` | Stream the processed video; supports full responses and HTTP Byte-Range requests through the optional `Range` header |
| `GET` | `/v1/videos/{videoId}/thumbnail` | Retrieve the generated JPEG thumbnail for a ready video |
| `GET` | `/v1/videos/{videoId}/variants` | Retrieve the generated video variants and their metadata |
| `GET` | `/v1/videos/{videoId}/variants/{resolution}/stream` | Stream a specific ready video variant; also supports the optional `Range` header |

### 📌 Streaming behavior

The application uses **one endpoint** for the main processed video stream:

```http
GET /v1/videos/{videoId}/stream
```

With no `Range` header, the API returns the complete processed resource with `200 OK`.

With a valid `Range` header such as:

```http
Range: bytes=0-999999
```

the API returns `206 Partial Content` with the appropriate `Content-Range` and `Content-Length` headers.

Invalid ranges are rejected with `416 Requested Range Not Satisfiable`.

---

## ▶️ Local Setup

StreamVault can be run directly on the local machine when you want to work with the services individually.

### Prerequisites

Install or make available:

- Java 21
- Maven (or use the included Maven Wrapper)
- PostgreSQL
- Apache Kafka
- FFmpeg
- FFprobe

### Verify media tools

```bash
ffmpeg -version
ffprobe -version
```

### Configure PostgreSQL

For a local Spring Boot run, the default PostgreSQL configuration in `application.yml` is:

```yaml
datasource:
  url: jdbc:postgresql://localhost:5432/streamvault
  username: postgres
  password: priye223@
```

The Docker Compose backend uses the same database credentials through environment variables, but connects to the PostgreSQL service by its Docker hostname:

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/streamvault
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: priye223@
```

This gives two connection modes:

- **Local application:** `localhost:5432/streamvault`
- **Docker application:** `postgres:5432/streamvault`

To inspect the database, you can use **DBeaver, pgAdmin, IntelliJ Database Tools, or `psql`**. For a direct local connection, use:

```text
Host: localhost
Port: 5432
Database: streamvault
Username: postgres
Password: priye223@
```

When using Docker Compose, PostgreSQL is exposed using the port mapping defined in `docker-compose.yml`; connect through the mapped host port from your database client.

These credentials are intended for the local hackathon environment. A production deployment should use environment-managed secrets instead of committed default credentials.

### Start PostgreSQL and Kafka

Start PostgreSQL and Kafka before launching the Spring Boot application. For a local Kafka run, make sure the application is configured to connect to your local Kafka listener (for example, `localhost:9092`).

### Build the application

Using Maven:

```bash
mvn clean install
```

Or on Windows with the Maven Wrapper:

```powershell
.\mvnw.cmd clean install
```

### Run Spring Boot

```bash
mvn spring-boot:run
```

Or on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend runs on the configured Spring Boot server port (8080 in the current Docker/local setup).

---

## 🐳 Docker Setup

For the quickest way to run the complete StreamVault stack, use the provided Docker Compose configuration.

The Compose setup runs the main application services together:

```text
🎨 React Frontend
       │
       ▼
🌱 Spring Boot Backend
       │
       ├────────► 🗄️ PostgreSQL
       │
       └────────► 📨 Kafka
```

### Prerequisites

- Docker Desktop
- Docker Compose support (included with current Docker Desktop releases)

### Start the full stack

Run this command from the directory containing `docker-compose.yml`:

```bash
docker compose up --build -d
```

This builds the frontend and backend images and starts the application dependencies together.

### Check service status

```bash
docker compose ps
```

### View logs

All services:

```bash
docker compose logs -f
```

Backend only:

```bash
docker compose logs -f stream-vault
```

### Access the application

With the current Compose configuration:

- Frontend: `http://localhost:5170`
- Backend API: `http://localhost:8080`
- Kafka: `localhost:9092`
- PostgreSQL: exposed on `localhost:5433`

Inside the Docker network, the backend communicates with Kafka and PostgreSQL using their Compose service names and internal ports.

### Stop the stack

```bash
docker compose down
```

This stops and removes the containers while keeping the named PostgreSQL volume.

### Start again without rebuilding

```bash
docker compose up -d
```

## 🚀 Quick Demo Flow

```text
👤 Register / Login
        │
        ▼
🔐 Receive JWT
        │
        ▼
🎥 Upload Video
        │
        ▼
📦 Outbox Event
        │
        ▼
📨 Kafka
        │
        ▼
⚙️ FFprobe + FFmpeg
        │
        ▼
✅ Video READY
        │
        ▼
📺 Stream Video
```

This is the shortest path through the application and demonstrates the main architecture from API request to playback.

---

## 🏁 Production Evolution

The current architecture provides a clear path toward larger deployments:

```text
💻 Local Filesystem
        ↓
☁️ Object Storage

🏠 Single Processing Flow
        ↓
👷 Multiple Processing Workers

🖥️ Local Application
        ↓
📊 Centralized Monitoring & Observability

🌐 Direct File Delivery
        ↓
🚀 CDN-backed Streaming
```

The important foundation is already in place: **durable state, asynchronous processing, reliable event delivery, safe processing claims, and independent media delivery.**

---

## 🧠 Engineering Principles

### ⚡ Keep expensive work asynchronous

Upload should not wait for FFmpeg.

```text
Upload → Persist → Return
              │
              ▼
        Async Processing
```

### 📦 Make events durable

The Outbox Pattern keeps database state and the processing event durable together.

### 🛡️ Expect duplicates

Processing is protected by a database-backed claim rather than assuming an event will arrive exactly once.

### 🔁 Bound retries

Transient failures should be retried, but permanently failing messages should move to a DLQ instead of retrying forever.

### 📺 Stream large files efficiently

HTTP Range requests allow the client to request only the bytes it needs.

---

## 🎯 Technical Summary

StreamVault demonstrates a complete backend workflow around a deceptively simple feature: **upload and stream a video reliably**.

- **Event-driven architecture** separates upload from CPU-intensive processing.
- **Outbox Pattern** provides reliable database-to-Kafka event delivery.
- **Kafka retries and DLQ** provide bounded failure handling.
- **Event IDs and atomic database claims** provide idempotent processing and concurrency protection.
- **FFprobe and FFmpeg** provide real media analysis and processing rather than placeholder workflows.
- **Status transitions** make the video-processing lifecycle explicit and auditable.
- **Crash recovery** allows stale outbox work to be reclaimed after application restart.
- **JWT authentication and ownership checks** secure the video APIs.
- **HTTP Byte-Range streaming** provides efficient delivery of large processed video files.
- **PostgreSQL remains the source of truth** for video metadata and workflow state.

> **StreamVault is a practical demonstration of how a video upload can become a reliable asynchronous distributed workflow instead of a simple file-save operation.**
