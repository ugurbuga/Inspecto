# Inspecto: KMP & Multiplatform Network & State Inspector

Inspecto is a powerful, lightweight, cross-platform network and event inspection library for **Kotlin Multiplatform (KMP)** and Jetpack Compose (Android, iOS, Desktop JVM, Web JS/WasmJS).

---

## Supported Platforms & Network Stacks

| Platform | Supported Network Stacks | Integration Mechanism |
|---|---|---|
| **Cross-Platform (All)** | **Ktor Client** | `InspectoKtorLogging` (Ktor Plugin) |
| **Android / JVM** | **OkHttp & Retrofit** | `InspectoOkHttpLoggingInterceptor` (OkHttp Interceptor) |
| **iOS** | **`NSURLSession` / Swift URLSession** | `logNSURLSessionCall` / `logNSURLSessionDataTask` |
| **Cross-Platform (All)** | **Apollo Kotlin (GraphQL)** | `InspectoApolloInterceptor` (Apollo Interceptor) |
| **Any / Custom** | **Manual Logging** | `Inspecto.addLog(...)` |

---

## 1. Installation & Setup

Add Inspecto to your multiplatform project dependencies (`commonMain`):

```kotlin
commonMain.dependencies {
    implementation("com.ugurbuga:inspecto:1.0.0-alpha06")
}
```

---

## 2. Launching Inspecto UI

To open the Inspecto inspector drawer/activity anywhere in your Compose UI:

```kotlin
import com.ugurbuga.inspecto.InspectoApp
import com.ugurbuga.inspecto.launchInspecto

@Composable
fun MyApp() {
    InspectoApp {
        // Your UI content
        Button(onClick = { launchInspecto() }) {
            Text("Open Inspecto Inspector")
        }
    }
}
```

---

## 3. Network Stack Integrations

### A. Ktor Client Integration (Cross-Platform / All)
Install `InspectoKtorLogging` plugin into your Ktor `HttpClient`:

```kotlin
import com.ugurbuga.inspecto.InspectoKtorLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

val httpClient = HttpClient {
    install(InspectoKtorLogging)
    install(ContentNegotiation) {
        json()
    }
}
```
*Note: Ktor plugin automatically intercepts all HTTP requests and responses across all platforms and displays them with a purple **`Ktor`** badge tag.*

---

### B. OkHttp & Retrofit Integration (Android & JVM)
Add `InspectoOkHttpLoggingInterceptor` to your `OkHttpClient.Builder`:

```kotlin
import com.ugurbuga.inspecto.InspectoOkHttpLoggingInterceptor
import okhttp3.OkHttpClient

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(InspectoOkHttpLoggingInterceptor())
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```
*Note: Intercepts all OkHttp and Retrofit network calls and displays them with an amber **`OkHttp`** badge tag.*

---

### C. Native iOS `NSURLSession` Integration (iOS Swift / Kotlin)
For native iOS networking (`URLSession`), use the provided logging helper in your Swift callback or `URLSession` extension:

```swift
import Foundation
import Shared // or Inspecto framework

extension URLSession {
    func inspectoDataTask(
        with request: URLRequest,
        completionHandler: @escaping (Data?, URLResponse?, Error?) -> Void
    ) -> URLSessionDataTask {
        let startTime = Date().timeIntervalSince1970
        return self.dataTask(with: request) { data, response, error in
            let duration = Int64((Date().timeIntervalSince1970 - startTime) * 1000)
            InspectoNSURLSessionKt.logNSURLSessionDataTask(
                request: request as NSURLRequest,
                response: response as? NSHTTPURLResponse,
                data: data as NSData?,
                error: error as NSError?,
                durationMs: duration
            )
            completionHandler(data, response, error)
        }
    }
}
```
*Note: Displays intercepted iOS native network calls with an emerald **`iOS NSURLSession`** badge tag.*

---

### D. Apollo Kotlin GraphQL Integration (Cross-Platform)
Add `InspectoApolloInterceptor` to your `ApolloClient.Builder`, explicitly providing your endpoint URL:

```kotlin
import com.apollographql.apollo.ApolloClient
import com.ugurbuga.inspecto.InspectoApolloInterceptor

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://rickandmortyapi.com/graphql")
    .addInterceptor(InspectoApolloInterceptor("https://rickandmortyapi.com/graphql"))
    .build()
```
*Note: Automatically intercepts GraphQL operations (Queries, Mutations), formats responses into clean, beautified JSON, and displays them with a magenta **`GraphQL`** badge tag.*

---

### E. Manual Logging API
For custom protocols, WebSockets, or third-party SDKs not covered above:

```kotlin
import com.ugurbuga.inspecto.Inspecto

Inspecto.addLog(
    url = "https://api.inspecto.local/v1/custom-action",
    method = "POST",
    statusCode = 201,
    durationMs = 45L,
    requestHeaders = mapOf("Authorization" to "Bearer token"),
    requestBody = "{\"action\": \"ping\"}",
    responseBody = "{\"status\": \"pong\"}",
    tag = "CustomTag"
)
```

---

## 4. UI & Theme Features

- **Material 3 Design**: Fully adaptive Material 3 UI with automatic Light and Dark theme support.
- **Theme-Aware Status Bar**: Dynamically synchronizes Android system status bar color with the app's topbar/surface color.
- **Custom Typography**: Integrated with **Outfit** typography.
- **Advanced Filtering & Search**: Filter requests by HTTP Method (`GET`, `POST`, `PUT`, `DELETE`, `GraphQL`), Status (`2XX`, `Success`, `Errors`), or search by URL/tag.
- **JSON Beautifier**: Automatically pretty-prints and indents JSON request and response bodies.
- **cURL Export**: One-tap copy cURL command for any captured network transaction.
