# Stream Valut

# 🎬 Video Hosting Platform

A production-oriented **video hosting and streaming platform** built with **Spring Boot**, **PostgreSQL**, **Redis**, **Kafka**, and **FFmpeg**.

The project demonstrates how a video platform can handle:

- Video uploads
- Asynchronous video processing
- Multiple video resolutions
- Thumbnail generation
- HTTP Range-based video streaming
- Event-driven microservices
- Reliable event publishing using the **Outbox Pattern**
- Redis-based rate limiting
- Redis distributed locking
- Kafka retry and DLQ handling
- Processing state management
- Persistent processing logs

---

## 🏗️ Architecture

```text
                         ┌─────────────────────┐
                         │        React        │
                         └──────────┬──────────┘
                                    │
                                    │ POST /api/v1/videos
                                    ▼
                         ┌─────────────────────┐
                         │   Spring Boot API   │
                         │   VideoController   │
                         └──────────┬──────────┘
                                    │
                         ┌──────────▼──────────┐
                         │   Redis Rate Limit  │
                         │    100 req/min/user │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │  JWT Authentication │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │ Idempotency Check   │
                         │ Avoid duplicate     │
                         │ uploads on retry    │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   Generate Video ID │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   File Storage      │
                         │ Disk / MinIO        │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │     PostgreSQL      │
                         │ Save Video Metadata │
                         │ Status = UPLOADED  │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   Outbox Event      │
                         │ VIDEO_UPLOADED      │
                         │ Same DB Transaction │
                         └──────────┬──────────┘
                                    │
                           Transaction Commit
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │ Outbox Scheduler    │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │       Kafka         │
                         │   video-uploaded    │
                         └──────────┬──────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
                    ▼                               ▼
          ┌──────────────────┐            ┌──────────────────┐
          │ Processing       │            │ Notification     │
          │ Service          │            │ Service          │
          └────────┬─────────┘            └──────────────────┘
                   │
                   ▼
          ┌──────────────────┐
          │ Redis Distributed│
          │ Lock             │
          └────────┬─────────┘
                   │
                   ▼
          ┌──────────────────┐
          │ Download Video   │
          └────────┬─────────┘
                   │
                   ▼
          ┌──────────────────┐
          │ FFprobe          │
          │ Read Metadata    │
          └────────┬─────────┘
                   │
                   ▼
          ┌──────────────────┐
          │ FFmpeg Processing│
          └────────┬─────────┘
                   │
          ┌────────┼─────────┐
          ▼        ▼         ▼
       1080p     720p      480p
          │        │         │
          └────────┼─────────┘
                   │
                   ▼
          ┌──────────────────┐
          │ Generate         │
          │ Thumbnail       │
          └────────┬─────────┘
                   │
                   ▼
          ┌──────────────────┐
          │ Upload Processed │
          │ Files            │
          └────────┬─────────┘
                   │
                   ▼
          ┌──────────────────┐
          │ Update PostgreSQL│
          │ Status = READY   │
          └────────┬─────────┘
                   │
                   ▼
          ┌──────────────────┐
          │ VIDEO_PROCESSED  │
          │ Kafka Event      │
          └────────┬─────────┘
                   │
                   ▼
          ┌──────────────────┐
          │ Notification     │
          │ "Video is ready" │
          └──────────────────┘
```

---

# 🚀 Core Video Upload Flow

The upload process is intentionally asynchronous.

```text
User
 │
 │ POST /api/v1/videos
 ▼
API
 │
 ├── Rate Limit
 ├── JWT Authentication
 ├── Idempotency Check
 ├── Generate Video ID
 ├── Store Original Video
 ├── Save Metadata
 └── Save Outbox Event
          │
          ▼
     Transaction Commit
          │
          ▼
     Outbox Scheduler
          │
          ▼
        Kafka
          │
          ▼
 Processing Service
          │
          ├── FFprobe
          ├── FFmpeg
          ├── 1080p
          ├── 720p
          ├── 480p
          └── Thumbnail
          │
          ▼
      Status = READY
```

The API does **not** wait for FFmpeg processing to finish.

Instead, it returns quickly after the upload and database transaction are completed.

Example:

```text
POST /api/v1/videos

Response:

{
    "videoId": "V101",
    "status": "UPLOADED"
}
```

The user can then query the video later:

```text
GET /api/v1/videos/V101
```

Once processing finishes:

```text
{
    "videoId": "V101",
    "status": "READY",
    "duration": 930,
    "thumbnailUrl": "/thumbnail/V101.jpg",
    "availableQualities": [
        "1080p",
        "720p",
        "480p"
    ]
}
```

---

# ⏱️ Timeline of a Single Video

```text
t = 0 sec

User uploads video
        │
        ▼
API returns immediately
Status = UPLOADED
        │
        ▼
Kafka Event Published
        │
        ▼
Processing Starts
        │
        ▼
FFprobe Reads Metadata
        │
        ▼
FFmpeg Creates:
 ├── 1080p
 ├── 720p
 ├── 480p
 └── Thumbnail
        │
        ▼
Database Updated
        │
        ▼
Status = READY
        │
        ▼
User Can Watch Video
```

---

# 🔄 Outbox Pattern

## The Problem

Suppose the upload flow performs:

```text
Step 1 → Video saved             ✅
Step 2 → Metadata saved          ✅
Step 3 → Kafka publish            ❌
```

The database now contains:

```text
Status = UPLOADED
```

But the processing service never receives the event.

The video could remain stuck forever.

---

## The Solution

The upload transaction saves both the metadata and the event:

```text
Save Video
    │
    ▼
Save Metadata
    │
    ▼
Save Outbox Event
    │
    ▼
Transaction Commit
```

The outbox table contains:

```text
event_type = VIDEO_UPLOADED
status     = PENDING
```

A scheduler periodically checks for pending events:

```text
Outbox Scheduler
       │
       ▼
Find PENDING Events
       │
       ▼
Publish to Kafka
       │
       ▼
Mark Event = SENT
```

Therefore:

```text
Database Transaction
        │
        ├── Video Metadata
        │
        └── Outbox Event
```

are committed together.

If Kafka is unavailable for one hour, the event remains in the outbox and can be published when Kafka becomes available again.

### Goal

> Never lose an important event simply because Kafka was temporarily unavailable.

---

# 🔐 Redis Rate Limiting

The upload endpoint is protected using Redis-based rate limiting.

Example configuration:

```text
100 requests / minute / user
```

Flow:

```text
Client
  │
  ▼
Upload API
  │
  ▼
Redis Rate Limiter
  │
  ├── Limit available → Continue
  │
  └── Limit exceeded → Reject
```

This protects the upload API from:

- Accidental request loops
- Upload abuse
- Excessive API requests
- Resource exhaustion

---

# 🔁 Idempotency

Network failures can cause clients to retry the same request.

Without idempotency:

```text
Client
 │
 ├── Upload VIDEO
 │
 ├── Network timeout
 │
 └── Retry Upload
```

The server could create two videos.

With an idempotency key:

```text
Request
   │
   ▼
Idempotency Check
   │
   ├── Already processed → Return existing result
   │
   └── New request       → Process upload
```

This prevents duplicate operations when clients retry requests.

---

# 🔒 Distributed Lock with Redis

As the platform grows, there may be multiple processing workers:

```text
                 Kafka
                   │
       ┌───────────┼───────────┐
       ▼           ▼           ▼
   Worker 1     Worker 2     Worker 3
```

Imagine the same event is accidentally delivered more than once:

```text
VIDEO123
   │
   ├──────────────► Worker 1
   │
   └──────────────► Worker 2
```

Both workers might start:

```text
FFmpeg
 ├── 1080p
 ├── 720p
 ├── 480p
 └── Thumbnail
```

This can result in duplicate processing and duplicate files.

---

## Redis Lock

Worker 1:

```text
Can I process VIDEO123?
```

Redis:

```text
YES → Lock created
```

Worker 2:

```text
Can I process VIDEO123?
```

Redis:

```text
NO → Already being processed
```

Worker 2 exits.

```text
Worker 1 → PROCESSING
Worker 2 → EXIT
```

The lock ensures that only one worker processes a particular video at a time.

---

# 🎞️ Video Processing

The Processing Service uses **FFprobe** to inspect the uploaded video.

Typical metadata includes:

- Duration
- Codec
- Resolution
- Bitrate
- Container format

After metadata extraction, **FFmpeg** generates multiple variants.

```text
Original Video
      │
      ▼
    FFmpeg
      │
      ├──► 1080p
      │
      ├──► 720p
      │
      ├──► 480p
      │
      └──► Thumbnail
```

The processed files are stored using either:

```text
Local Disk
```

or:

```text
MinIO
```

---

# 🗄️ Database Schema

The platform uses PostgreSQL.

Main tables:

```text
1. video
2. video_variant
3. video_processing_job
4. outbox_event
5. processing_log
6. user
7. video_view        (optional)
```

---

## 1. `video`

Stores information about the original uploaded video.

One uploaded video = one row.

```text
VIDEO
────────────────────────────
id                  UUID
user_id             UUID
title               VARCHAR
description         TEXT
original_filename   VARCHAR
storage_path        VARCHAR
status              VARCHAR
duration            BIGINT
thumbnail_url       VARCHAR
default_resolution  VARCHAR
file_size           BIGINT
mime_type           VARCHAR
created_at          TIMESTAMP
updated_at          TIMESTAMP
```

Example:

```text
id              = V101
title           = Spring Boot Tutorial
status          = READY
duration        = 930 seconds
thumbnail_url   = /thumbnail/V101.jpg
storage_path    = videos/original/V101.mp4
```

---

## 2. `video_variant`

A single video can have multiple generated files.

```text
Original
   │
   ├── 1080p
   ├── 720p
   └── 480p
```

Schema:

```text
VIDEO_VARIANT
────────────────────────
id
video_id
resolution
storage_path
size
bitrate
status
created_at
```

Example:

```text
Video 101 | 1080p | /videos/1080/V101.mp4
Video 101 | 720p  | /videos/720/V101.mp4
Video 101 | 480p  | /videos/480/V101.mp4
```

---

## 3. `video_processing_job`

Tracks processing attempts and history.

```text
VIDEO_PROCESSING_JOB
────────────────────────
id
video_id
status
worker_name
started_at
completed_at
error_message
```

Example:

```text
video_id     = V101
status       = PROCESSING
worker_name  = Worker-3
```

Later:

```text
status = COMPLETED
```

or:

```text
status = FAILED
error_message = FFmpeg processing error
```

---

## 4. `outbox_event`

Stores events that must eventually be published to Kafka.

```text
OUTBOX_EVENT
────────────────────────
id
aggregate_type
aggregate_id
event_type
payload
status
retry_count
created_at
published_at
```

Example:

```text
aggregate_type = VIDEO
aggregate_id   = V101
event_type     = VIDEO_UPLOADED
status         = PENDING
```

---

## 5. `processing_log`

Stores processing information persistently.

```text
PROCESSING_LOG
────────────────────────
id
video_id
step
status
message
created_at
```

Example:

```text
video_id = V101
step     = FFMPEG_720P
status   = FAILED
message  = FFmpeg exited with code 1
```

This makes it easier to diagnose processing failures without depending entirely on application logs.

---

## 6. `user`

Stores minimal user information.

```text
USER
────────────────────────
id
name
email
mobile
password
created_at
```

Authentication is handled using JWT.

---

## 7. `video_view` — Optional

Can be added for analytics.

```text
VIDEO_VIEW
────────────────────────
id
video_id
user_id
watch_duration
viewed_at
```

Potential use cases:

- View counts
- Watch duration
- Popular videos
- User analytics
- Most watched content

---

# 🔗 Entity Relationships

```text
USER
 │
 │ 1
 │
 │ N
 ▼
VIDEO
 │
 ├─────────────── 1:N ──────────────► VIDEO_VARIANT
 │
 ├─────────────── 1:N ──────────────► VIDEO_PROCESSING_JOB
 │
 ├─────────────── 1:N ──────────────► PROCESSING_LOG
 │
 ├─────────────── 1:N ──────────────► OUTBOX_EVENT
 │
 └─────────────── 1:N ──────────────► VIDEO_VIEW
```

---

# 📡 Kafka Events

Main events:

```text
VIDEO_UPLOADED
VIDEO_PROCESSED
```

### `VIDEO_UPLOADED`

Produced after a video is successfully uploaded and the transaction commits.

```text
Video API
    │
    ▼
Outbox
    │
    ▼
Kafka
    │
    ▼
Processing Service
```

### `VIDEO_PROCESSED`

Published after processing is successfully completed.

```text
Processing Service
        │
        ▼
     Kafka
        │
        ▼
Notification Service
```

Notification:

```text
Your video is ready to watch.
```

---

# ❌ Failure Handling

The processing pipeline supports retries and a Dead Letter Queue.

```text
Kafka Consumer
      │
      ▼
Processing Failed?
      │
     YES
      │
      ▼
Retry
      │
      ├── Attempt 1
      ├── Attempt 2
      └── Attempt 3
      │
      ▼
Still Failed?
      │
     YES
      │
      ▼
Dead Letter Queue
      │
      ▼
Manual Retry API
```

This prevents permanently failing messages from blocking normal processing.

---

# 📺 Video Streaming

The platform supports HTTP Range-based video streaming.

The browser sends requests such as:

```http
GET /api/v1/videos/V101/stream

Range: bytes=0-999999
```

The server reads only the requested portion of the file and responds with:

```http
206 Partial Content
```

Example:

```http
Content-Range: bytes 0-999999/2680000
Content-Length: 1000000
Content-Type: video/mp4
```

---

# 🧮 Understanding Byte Ranges

A byte range is inclusive.

```text
Range: bytes=0-10
```

means:

```text
0 1 2 3 4 5 6 7 8 9 10
```

Total bytes:

```text
end - start + 1
```

Therefore:

```text
10 - 0 + 1 = 11 bytes
```

If exactly 10 bytes are required:

```text
Range: bytes=0-9
```

because:

```text
9 - 0 + 1 = 10 bytes
```

---

# 📦 Why the Last Byte Is `size - 1`

Suppose a file contains exactly 100 bytes.

Valid positions are:

```text
0 → 99
```

There is no byte `100`.

Therefore:

```text
firstByte = 0
lastByte  = totalSize - 1
```

The same concept applies to a Java byte array:

```java
byte[] data = new byte[100];

data[0]   // first byte
data[99]  // last byte
```

There is no:

```java
data[100]
```

---

# 🌐 Browser Range Requests

Suppose a video is:

```text
2,680,000 bytes
```

The browser might request:

### First request

```http
Range: bytes=0-999999
```

```text
999999 - 0 + 1
= 1,000,000 bytes
```

### Second request

```http
Range: bytes=1000000-1999999
```

```text
1999999 - 1000000 + 1
= 1,000,000 bytes
```

### Third request

```http
Range: bytes=2000000-2679999
```

```text
2679999 - 2000000 + 1
= 680,000 bytes
```

However, **1 MB is not a guaranteed browser chunk size**.

Different:

- Browsers
- Video players
- Network conditions
- Implementations

can request different ranges.

The server's responsibility is simply to correctly understand:

```text
Range: bytes=start-end
```

and return the requested bytes.

---

# ⚠️ Range Beyond File Size

Suppose:

```text
File size = 2,680,000 bytes
```

Valid byte positions:

```text
0 → 2,679,999
```

If the browser requests:

```http
Range: bytes=3000000-3999999
```

the requested range does not exist.

The server should return:

```http
416 Range Not Satisfiable
```

---

# ⚠️ Range Partially Beyond File Size

Suppose the browser requests:

```http
Range: bytes=2500000-3000000
```

The requested end is beyond the file.

However, these bytes exist:

```text
2,500,000 → 2,679,999
```

The server can return:

```http
206 Partial Content

Content-Range: bytes 2500000-2679999/2680000
```

The server returns the remaining available bytes.

---

# 🎯 How Does the Browser Know What to Request?

A video is **not** simply:

```text
1 second = X bytes
```

Compression varies based on the video content.

For example:

```text
0:00 → 5 MB
0:10 → 8 MB
0:20 → 12 MB
```

The browser/video player uses information such as:

- Duration
- Timestamps
- Codec
- Bitrate
- Keyframes
- Container metadata

to determine what data it needs.

Therefore, the server does **not** calculate:

```text
5 seconds = 2 MB
10 seconds = 4 MB
```

Instead, the browser requests a byte range.

---

# 🎥 Streaming Request Flow

```text
                    BROWSER
                       │
                       │ GET /stream
                       │ Range: bytes=0-999999
                       ▼
                ┌──────────────┐
                │ StreamVault  │
                │ Spring Boot  │
                └──────┬───────┘
                       │
                       │ Check video
                       ▼
                  PostgreSQL
                       │
                       │ storagePath
                       ▼
                 Local MP4 File
                       │
                       │ Read bytes
                       ▼
                  0 → 999999
                       │
                       ▼
                HTTP 206
                       │
                       │ Content-Range:
                       │ bytes 0-999999/2680000
                       ▼
                    BROWSER
                       │
                       ▼
                      🎬
```

### Server Responsibility

The Spring Boot server must:

1. Validate the requested range.
2. Determine the correct file.
3. Read the requested bytes.
4. Return `206 Partial Content`.
5. Set the correct `Content-Range`.
6. Set the correct `Content-Length`.
7. Return the appropriate `Content-Type`.

The browser/video player handles the playback and seeking logic.

---

# 🔌 REST APIs

## Authentication

### Register

```http
POST /api/v1/auth/register
```

Creates a new user.

### Login

```http
POST /api/v1/auth/login
```

Authenticates the user and returns a JWT.

---

## Videos

### Upload Video

```http
POST /api/v1/videos
```

Responsibilities:

- Validate request
- Authenticate user
- Apply rate limiting
- Check idempotency
- Store original file
- Save metadata
- Create outbox event

---

### Get Video Details

```http
GET /api/v1/videos/{videoId}
```

Returns:

- Title
- Description
- Duration
- Processing status
- Thumbnail
- Available qualities
- Video metadata

---

### List My Videos

```http
GET /api/v1/videos/my
```

Returns videos uploaded by the authenticated user.

---

### Stream Video

```http
GET /api/v1/videos/{videoId}/stream
```

Streams the default video variant using HTTP Range requests.

---

### Stream by Resolution

```http
GET /api/v1/videos/{videoId}/stream?quality=720
```

Streams a specific video variant.

Example:

```text
quality=1080
quality=720
quality=480
```

---

### Delete Video

```http
DELETE /api/v1/videos/{videoId}
```

Deletes the uploaded video and associated resources.

---

# 🧩 Technology & Architecture Concepts

This project focuses on production-oriented backend concepts.

| Concept | Status |
|---|:---:|
| Spring Boot | ✅ |
| Microservice Communication | ✅ |
| Kafka | ✅ |
| Event-Driven Architecture | ✅ |
| Async Processing | ✅ |
| Outbox Pattern | ✅ |
| FFmpeg Processing | ✅ |
| FFprobe | ✅ |
| Video State Machine | ✅ |
| Redis Rate Limiting | ✅ |
| Redis Distributed Lock | ✅ |
| Retry Handling | ✅ |
| Dead Letter Queue | ✅ |
| Scheduler | ✅ |
| File Storage | ✅ |
| PostgreSQL | ✅ |
| JWT Authentication | ✅ |
| Request Validation | ✅ |
| Global Exception Handler | ✅ |
| DTO + Mapper | ✅ |
| HTTP Range Streaming | ✅ |
| Multiple Video Resolutions | ✅ |
| Thumbnail Generation | ✅ |

---

# 🧠 Video State Machine

A video's lifecycle can be represented as:

```text
              ┌─────────────┐
              │   UPLOADED  │
              └──────┬──────┘
                     │
                     ▼
              ┌─────────────┐
              │  PROCESSING │
              └──────┬──────┘
                     │
             ┌───────┴────────┐
             │                │
             ▼                ▼
       ┌──────────┐      ┌──────────┐
       │  READY   │      │  FAILED  │
       └──────────┘      └────┬─────┘
                              │
                              │ Retry
                              ▼
                         PROCESSING
```

This gives the application a clear and predictable processing lifecycle.

---

# 🏃 End-to-End Processing Flow

```text
                    USER
                     │
                     ▼
               Upload Video
                     │
                     ▼
              Spring Boot API
                     │
          ┌──────────┼──────────┐
          │          │          │
          ▼          ▼          ▼
       JWT Auth   Rate Limit  Idempotency
          │          │          │
          └──────────┼──────────┘
                     │
                     ▼
                Store File
                     │
                     ▼
              Save PostgreSQL
                     │
                     ▼
               Outbox Event
                     │
                     ▼
               Kafka Publisher
                     │
                     ▼
              video-uploaded
                     │
                     ▼
           Processing Service
                     │
                     ▼
               Redis Lock
                     │
                     ▼
                  FFprobe
                     │
                     ▼
                  FFmpeg
                     │
          ┌──────────┼──────────┐
          ▼          ▼          ▼
        1080p      720p       480p
          │          │          │
          └──────────┼──────────┘
                     │
                     ▼
                Thumbnail
                     │
                     ▼
              Store Files
                     │
                     ▼
              Update Database
                     │
                     ▼
              Status = READY
                     │
                     ▼
             VIDEO_PROCESSED
                     │
                     ▼
          Notification Service
                     │
                     ▼
              User Watches
                     │
                     ▼
              HTTP Streaming
```

---

# 🛡️ Reliability Design

The platform handles several failure scenarios.

### Kafka Unavailable

```text
DB Transaction
      │
      ▼
Outbox Event = PENDING
      │
      ▼
Kafka unavailable
      │
      ▼
Retry later
```

The event is not lost.

### Duplicate Kafka Event

```text
Event
 │
 ├── Worker 1 → Redis Lock → PROCESS
 │
 └── Worker 2 → Redis Lock → EXIT
```

### Processing Failure

```text
Processing
    │
    ▼
Failure
    │
    ▼
Retry × 3
    │
    ▼
DLQ
    │
    ▼
Manual Retry
```

### Client Upload Retry

```text
Same Idempotency Key
        │
        ▼
Idempotency Check
        │
        ▼
Existing Result
```

This prevents duplicate uploads.

---

# 📂 Suggested Service Architecture

A possible microservice structure:

```text
video-platform/
│
├── api-gateway/
│
├── auth-service/
│
├── video-service/
│
├── processing-service/
│
├── notification-service/
│
├── docker-compose.yml
│
└── README.md
```

### API / Video Service

Responsible for:

- Authentication integration
- Video upload
- Metadata
- File storage
- Video APIs
- Streaming
- Outbox creation

### Processing Service

Responsible for:

- Kafka consumption
- Distributed locking
- FFprobe
- FFmpeg
- Variant generation
- Thumbnail generation
- Processing status
- Retry handling

### Notification Service

Responsible for:

- Consuming `VIDEO_PROCESSED`
- Sending video-ready notifications

---

# 🧪 Local Video Testing

A simple test page can be used to directly test browser streaming:

```text
http://localhost:8080/test-video.html
```

The browser can then issue HTTP Range requests against the streaming endpoint.

---

# 📌 Key Design Principles

### 1. Upload should be fast

The API should not wait for FFmpeg.

```text
Upload → Store → Event → Return
```

Processing happens asynchronously.

### 2. Events must not be lost

The Outbox Pattern guarantees that the event is persisted alongside the database state.

### 3. Processing must be safe

Redis distributed locking prevents multiple workers from processing the same video simultaneously.

### 4. Failures must be recoverable

Kafka retries and DLQ handling prevent permanently failed processing from disappearing silently.

### 5. Streaming should be efficient

HTTP Range requests allow the browser to retrieve only the required portion of the video instead of downloading the entire file.

---

# 🎯 Project Goals

This project is designed to demonstrate how a real-world video hosting platform can be built using modern backend architecture.

The primary goals are:

- Build a reliable video upload pipeline.
- Process videos asynchronously.
- Generate multiple resolutions.
- Store video metadata separately from physical files.
- Implement event-driven communication.
- Prevent lost events.
- Prevent duplicate processing.
- Handle transient failures.
- Support browser-based streaming.
- Demonstrate production-oriented backend patterns.

---

# 🏁 Final Architecture Summary

```text
                       ┌──────────────┐
                       │    Client    │
                       │ React/Postman│
                       └───────┬──────┘
                               │
                               ▼
                       ┌──────────────┐
                       │ API Gateway  │
                       │ /JWT         │
                       └───────┬──────┘
                               │
                               ▼
                    ┌────────────────────┐
                    │   Video Service    │
                    │                    │
                    │                    │
                    │ Rate Limiting      │
                    │ Idempotency        │
                    │ Upload             │
                    │ Streaming          │
                    └─────────┬──────────┘
                              │
                ┌─────────────┼─────────────┐
                ▼             ▼             ▼
          PostgreSQL        Redis        File Storage
                │
                ▼
          Outbox Event
                │
                ▼
             Kafka
                │
                ▼
       ┌──────────────────┐
       │ Processing       │
       │ Service          │
       │                  │
       │ Redis Lock       │
       │ FFprobe          │
       │ FFmpeg           │
       │ Thumbnail        │
       └────────┬─────────┘
                │
                ▼
          Video Variants
                │
                ▼
          Status = READY
                │
                ▼
             Kafka
                │
                ▼
       Notification Service
```

## 💡 What This Project Demonstrates

> **A reliable, asynchronous, event-driven video processing and streaming architecture using Spring Boot, Kafka, Redis, PostgreSQL, and FFmpeg.**

The project combines practical backend engineering concepts such as **microservices, distributed systems, event-driven architecture, reliable messaging, concurrency control, asynchronous processing, failure recovery, and HTTP video streaming** into a single end-to-end system.
