# Inspecto 🔍

**Inspecto** is a standalone, lightweight Kotlin Multiplatform (KMP) network inspection library inspired by Chucker. It enables real-time HTTP request & response logging, search filtering, payload formatting, syntax highlighting, and granular copying/sharing across **Android**, **Desktop (JVM)**, **iOS**, and **Web (Wasm/JS)**.

---

## 🚀 Quick Start & Usage

### 1. Launching Inspecto UI
Inspecto features zero-configuration startup on Android. Simply call:

```kotlin
import com.ugurbuga.inspecto.launchInspecto

// On Android, launchInspecto() automatically manages ApplicationContext
launchInspecto()
```

Or invoke via `Inspecto.launch()`:
```kotlin
import com.ugurbuga.inspecto.Inspecto

Inspecto.launch()
```

---

### 2. Logging Network Calls

#### Manual Logging
```kotlin
import com.ugurbuga.inspecto.Inspecto

Inspecto.addLog(
    url = "https://api.example.com/v1/users",
    method = "GET",
    statusCode = 200,
    durationMs = 120L,
    requestHeaders = mapOf("Authorization" to "Bearer token123"),
    requestQueryParams = mapOf("page" to "1"),
    responseHeaders = mapOf("Content-Type" to "application/json"),
    responseBody = """{"status": "ok", "users": []}"""
)
```

#### OkHttp Integration (Android)
```kotlin
import com.ugurbuga.inspecto.InspectoOkHttpInterceptor

InspectoOkHttpInterceptor.logCall(
    url = request.url.toString(),
    method = request.method,
    statusCode = response.code,
    durationMs = durationMs,
    requestHeaders = requestHeadersMap,
    responseHeaders = responseHeadersMap,
    responseBody = bodyString
)
```

---

## ✨ Features

- **Zero-Config Context**: Uses Android `ContentProvider` (`InspectoInitProvider`) to auto-capture `ApplicationContext`.
- **Top-Level `launchInspecto()` API**: Clean and concise entry point across all target platforms.
- **Combined 3-Section Share Format**: Copies Overview, Request, and Response stacked cleanly as plain text.
- **Granular Copying**:
  - **Tab-level Copy**: Copy active tab summary (Overview, Request, or Response).
  - **Sub-block Copy**: Copy Request Headers, Query Parameters, Request Body, Response Headers, or Response Body independently with 1-click feedback.
- **JSON Syntax Highlighting**: Color-coded JSON formatter with non-blocking background parsing.
- **Encapsulated Architecture**: Public facade (`Inspecto`, `InspectoLogEntry`, `launchInspecto`) hides internal UI and helper components (`internal`).

---

## 📦 Distribution & Dependency Configuration

Add JitPack repository to `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add dependency to `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.ugurbuga:inspecto:1.0.0")
}
```

---

## 📄 License
Licensed under the Apache License 2.0.
