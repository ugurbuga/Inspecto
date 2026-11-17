package com.ugurbuga.inspecto

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object NetworkInspector {
    private const val MAX_LOG_CAPACITY = 100
    private val jsonFormatter = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val _logs = MutableStateFlow<List<NetworkLogEntry>>(emptyList())
    val logs: StateFlow<List<NetworkLogEntry>> = _logs.asStateFlow()

    var onLaunchCallback: (() -> Unit)? = null
    var applicationContext: Any? = null

    fun launch() {
        onLaunchCallback?.invoke()
    }

    fun addLog(entry: NetworkLogEntry) {
        val updated = (_logs.value.toMutableList()).apply {
            add(0, entry)
            if (size > MAX_LOG_CAPACITY) {
                removeAt(size - 1)
            }
        }
        _logs.value = updated
    }

    fun addLog(
        url: String,
        method: String,
        statusCode: Int,
        durationMs: Long,
        requestHeaders: Map<String, String> = emptyMap(),
        requestQueryParams: Map<String, String> = emptyMap(),
        requestBody: String? = null,
        responseHeaders: Map<String, String> = emptyMap(),
        responseBody: String? = null
    ) {
        val entry = NetworkLogEntry(
            id = generateUniqueId(),
            url = url,
            method = method,
            statusCode = statusCode,
            durationMs = durationMs,
            timestampMs = currentEpochMillis(),
            requestHeaders = requestHeaders,
            requestQueryParams = requestQueryParams,
            requestBody = requestBody,
            responseHeaders = responseHeaders,
            responseBody = responseBody,
            responseSizeByte = responseBody?.length?.toLong() ?: 0L
        )
        addLog(entry)
    }

    fun clear() {
        _logs.value = emptyList()
    }

    fun addDemoLogs() {
        if (_logs.value.isNotEmpty()) return
        addLog(
            url = "https://api.inspecto.dev/v1/auth/login",
            method = "POST",
            statusCode = 200,
            durationMs = 184L,
            requestHeaders = mapOf("Content-Type" to "application/json", "Authorization" to "Bearer token_abc123"),
            requestBody = "{\"email\":\"developer@inspecto.dev\",\"password\":\"********\"}",
            responseHeaders = mapOf("Content-Type" to "application/json", "X-RateLimit" to "100"),
            responseBody = "{\n  \"status\": \"success\",\n  \"token\": \"jwt_eyJhbGciOiJIUzI1NiIsIn...\",\n  \"user\": {\n    \"id\": 42,\n    \"name\": \"Uğur Buğa\",\n    \"role\": \"Admin\"\n  }\n}"
        )
        addLog(
            url = "https://api.inspecto.dev/v1/users/profile?include_stats=true",
            method = "GET",
            statusCode = 200,
            durationMs = 95L,
            requestHeaders = mapOf("Authorization" to "Bearer jwt_eyJhbGciOiJIUzI1NiIsIn..."),
            requestQueryParams = mapOf("include_stats" to "true"),
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\n  \"id\": 42,\n  \"username\": \"ugurbuga\",\n  \"email\": \"developer@inspecto.dev\",\n  \"stats\": {\n    \"requestsInspected\": 1420,\n    \"activeSessions\": 3\n  }\n}"
        )
        addLog(
            url = "https://api.inspecto.dev/v1/products?category=electronics&limit=10",
            method = "GET",
            statusCode = 200,
            durationMs = 312L,
            requestHeaders = mapOf("Accept" to "application/json"),
            requestQueryParams = mapOf("category" to "electronics", "limit" to "10"),
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "[\n  {\n    \"id\": 101,\n    \"title\": \"Wireless Noise Cancelling Headphones\",\n    \"price\": 299.99,\n    \"inStock\": true\n  },\n  {\n    \"id\": 102,\n    \"title\": \"Mechanical Ergonomic Keyboard\",\n    \"price\": 149.99,\n    \"inStock\": false\n  }\n]"
        )
        addLog(
            url = "https://api.inspecto.dev/v1/checkout/pay",
            method = "POST",
            statusCode = 500,
            durationMs = 1240L,
            requestHeaders = mapOf("Content-Type" to "application/json"),
            requestBody = "{\"cartId\":\"cart_998877\",\"amount\":449.98,\"currency\":\"USD\"}",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\n  \"error\": \"Internal Server Error\",\n  \"message\": \"Payment gateway timeout while connecting to bank service\",\n  \"code\": 50012\n}"
        )
        addLog(
            url = "https://api.inspecto.dev/v1/analytics/track",
            method = "POST",
            statusCode = 201,
            durationMs = 45L,
            requestHeaders = mapOf("Content-Type" to "application/json"),
            requestBody = "{\"event\":\"app_launched\",\"timestamp\":1741350000}",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"status\": \"recorded\"}"
        )
    }

    fun formatJson(raw: String): String {
        if (raw.isBlank()) return ""
        return runCatching {
            val element = jsonFormatter.parseToJsonElement(raw)
            jsonFormatter.encodeToString(JsonElement.serializer(), element)
        }.getOrDefault(raw)
    }

    private fun generateUniqueId(): String {
        return currentEpochMillis().toString() + "-" + (1000..9999).random()
    }

    private fun currentEpochMillis(): Long {
        return kotlin.time.Clock.System.now().toEpochMilliseconds()
    }
}


