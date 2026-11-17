package com.ugurbuga.inspecto

import kotlinx.serialization.Serializable

@Serializable
data class NetworkLogEntry(
    val id: String = "",
    val url: String = "",
    val method: String = "GET",
    val statusCode: Int = 200,
    val durationMs: Long = 0L,
    val timestampMs: Long = 0L,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestQueryParams: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val responseSizeByte: Long = 0L
) {
    val isSuccess: Boolean
        get() = statusCode in 200..299

    val isError: Boolean
        get() = statusCode >= 400 || statusCode == 0

    val path: String
        get() = runCatching {
            val withoutProtocol = url.substringAfter("://")
            "/" + withoutProtocol.substringAfter("/", "")
        }.getOrDefault(url)

    val host: String
        get() = runCatching {
            val withoutProtocol = url.substringAfter("://")
            withoutProtocol.substringBefore("/")
        }.getOrDefault("")

    val formattedSize: String
        get() {
            val bytes = if (responseSizeByte > 0) responseSizeByte else (responseBody?.length?.toLong() ?: 0L)
            return when {
                bytes <= 0 -> "0 B"
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> "${(bytes / (1024 * 1024.0)).toString().take(4)} MB"
            }
        }

    fun toCurlCommand(): String {
        val builder = StringBuilder("curl -X $method \"$url\"")
        requestHeaders.forEach { (key, value) ->
            builder.append(" -H \"$key: $value\"")
        }
        if (!requestBody.isNullOrBlank()) {
            builder.append(" -d '${requestBody.replace("'", "'\\''")}'")
        }
        return builder.toString()
    }
}
