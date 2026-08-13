# telegram-service

Microservice wrapper over Telegram TDLib written in Node.js.

It acts as a stateless Telegram adapter providing:
- channel message ingestion (pagination)
- media metadata extraction
- file downloading (audio, images, covers)
- normalized DTOs for downstream services

---

## 🧠 Purpose

This service isolates TDLib from business logic and JVM-based systems.

It provides:
- stable API for Telegram data access
- consistent message normalization
- safe file downloading pipeline
- controlled pagination over chat history

---

## 🏗 Architecture

```

tgfetch (Kotlin)
↓ HTTP
telegram-service (Node.js)
↓
TDLib (prebuilt native)
↓
Telegram network

```

---

## 📦 Core Responsibilities

### 1. Authentication (one-time session init)
- phone login
- OTP / 2FA handling
- session persistence (TDLib internal storage)

---

### 2. Channel resolution
- resolve channel username → chatId

---

### 3. Message ingestion
- paginated chat history retrieval
- backward cursor-based pagination
- normalization into DTO

---

### 4. Media handling
- audio file extraction
- cover extraction
- file download via TDLib
- streaming file output

---

### 5. File cleanup
- automatic TTL cleanup
- post-stream deletion

---

## 🔌 API

### Channels

#### Resolve channel
```

GET /channels/:username

````

Response:
```json
{ "chatId": -100123456 }
````

---

### Messages

#### Get paginated history (BACKWARD sync)

```
GET /chats/:chatId/messages?limit=100&cursor=0
```

Response:

```json
{
  "items": [MessageDTO],
  "nextCursor": 123456789,
  "hasMore": true
}
```

---

### Files

#### Download by remote file id (stream)

```
GET /files/by-remote/:remoteFileId
```

Returns:

* streamed binary file
* Content-Type inferred from Telegram metadata

---

## 📄 Message DTO (contract)

```json
{
  "channelId": -100123456,
  "messageId": 5242880,
  "date": 1780216974,

  "type": "audio",

  "caption": "KTRSS - ATLAS",

  "audio": {
    "title": "ATLAS",
    "performer": "KTRSS",
    "durationSeconds": 265,
    "fileSizeBytes": 28779008,

    "fileName": "KTRSS - ATLAS.flac",
    "mimeType": "audio/flac",

    "tdFileId": 1389,
    "remoteFileId": "CQACAgIA...",
    "fileUniqueId": "AgAD97MA..."
  },

  "cover": {
    "tdFileId": 1388,
    "remoteFileId": "AAMCAg...",
    "fileUniqueId": "AgADxxxx..."
  }
}
```

---

## 🔁 Sync model

* Telegram is source of truth
* ingestion runs via periodic full sync (1h)
* pagination is backward only
* deduplication is handled by downstream service

---

## 🧹 Cleanup strategy

* files are deleted after stream completion
* fallback TTL cleanup job runs every 15 minutes
* directory auto-created on startup

---

## ⚙️ TDLib behavior notes

* First request may return partial history (cold start)
* `openChat` should be called before pagination
* `file_unique_id` is stable content identifier
