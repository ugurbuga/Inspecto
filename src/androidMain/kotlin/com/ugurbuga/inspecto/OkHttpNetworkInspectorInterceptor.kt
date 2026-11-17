package com.ugurbuga.inspecto

/**
 * Helper class for logging HTTP requests from OkHttp or other Android HTTP clients to NetworkInspector.
 */
object OkHttpNetworkInspectorInterceptor {

    fun logCall(
        url: String,
        method: String,
        statusCode: Int,
        durationMs: Long,
        requestHeaders: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        requestBody: String? = null,
        responseHeaders: Map<String, String> = emptyMap(),
        responseBody: String? = null
    ) {
        NetworkInspector.addLog(
            url = url,
            method = method,
            statusCode = statusCode,
            durationMs = durationMs,
            requestHeaders = requestHeaders,
            requestQueryParams = queryParams,
            requestBody = requestBody,
            responseHeaders = responseHeaders,
            responseBody = responseBody
        )
    }
}
