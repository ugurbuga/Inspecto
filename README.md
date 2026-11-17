# Inspecto 🔍

**Inspecto** is a lightweight, Chucker-inspired, feature-rich HTTP network inspector library built for **Kotlin Multiplatform (KMP)**. It allows developers to monitor, inspect, search, filter, and debug network traffic (HTTP requests, responses, headers, query parameters, latency, and payloads) directly on Android, Desktop (JVM), iOS, and Web.

---

## ✨ Features

- **Multiplatform Support**: Works seamlessly across Android, JVM/Desktop, iOS, and Web (JS / WasmJS).
- **Chucker-Inspired UI**: Beautiful Material 3 UI with dark/light theme awareness, status badges, and summary statistics.
- **Ktor & OkHttp Integration**: Out-of-the-box interceptor and plugin support for logging requests from Ktor and OkHttp clients.
- **Demo Flow / Mock Data**: Instant one-tap demo loading (`Inspecto.addDemoLogs()`) for testing and UI previews.
- **Virtualized JSON Syntax Highlighter**: Smooth, non-blocking lazy rendering for large JSON payloads (300KB+) with color-coded syntax highlighting.
- **cURL & URL Exporters**: One-tap copy for cURL commands, request URLs, headers, and payloads.
- **Shake-to-Open (Android)**: Automatically opens the inspector when shaking the physical device.
- **Collapsible Headers & Query Parameters**: Clean, organized collapsible views for request/response headers and query parameters.
- **Search & Filter Chips**: Filter traffic instantly by HTTP method (`GET`, `POST`, etc.) or status code (`2XX`, `ERR`).

---

## 📦 Installation & Versioning

Inspecto follows alpha release management (`1.0.0-alpha01`). Add Inspecto to your project via **JitPack**:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.ugurbuga:inspecto:1.0.0-alpha01")
}
```

---

## 🚀 Quick Start & Usage

### 1. Demo Flow (Try Instantly)
To populate demo network traffic (GET/POST/Error payloads) for UI testing and demos:
```kotlin
import com.ugurbuga.inspecto.Inspecto

// Load mock demo logs
Inspecto.addDemoLogs()
```

### 2. Ktor Client Integration
Install the logger in your Ktor `HttpClient`:

```kotlin
import com.ugurbuga.inspecto.Inspecto

val httpClient = HttpClient(CIO) {
    install(ResponseObserver) {
        onResponse { response ->
            Inspecto.addLog(
                url = response.call.request.url.toString(),
                method = response.call.request.method.value,
                statusCode = response.status.value,
                durationMs = 120L,
                responseBody = response.bodyAsText()
            )
        }
    }
}
```

### 3. OkHttp Client Integration
Use the provided `OkHttpNetworkInspectorInterceptor` for OkHttp clients:

```kotlin
import com.ugurbuga.inspecto.OkHttpNetworkInspectorInterceptor

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(OkHttpNetworkInspectorInterceptor)
    .build()
```

### 4. Manual Launch & Shake-to-Open (Android)
Launch Inspecto programmatically or enable shake-to-open:

```kotlin
import com.ugurbuga.inspecto.launchNetworkInspector

// Programmatic launch
launchNetworkInspector(context)
```

---

## 📄 License

```text
Copyright 2026 Uğur Buğa

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License.

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
