package com.ugurbuga.inspecto.ui.previews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ugurbuga.inspecto.Inspecto
import com.ugurbuga.inspecto.InspectoLogEntry
import com.ugurbuga.inspecto.InspectoScreen
import com.ugurbuga.inspecto.ui.detail.InspectoDetailScreen
import com.ugurbuga.inspecto.ui.theme.InspectoDarkColorScheme
import com.ugurbuga.inspecto.ui.theme.InspectoLightColorScheme

@Preview(name = "Inspecto List - Light")
@Composable
internal fun InspectoListLightPreview() {
    LaunchedEffect(Unit) {
        Inspecto.clear()
        Inspecto.addLog(
            url = "https://api.example.com/v1/users",
            method = "GET",
            statusCode = 200,
            durationMs = 120L,
            requestHeaders = mapOf("Authorization" to "Bearer token123"),
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "[{\"id\": 1, \"name\": \"John Doe\"}]",
            tag = "Ktor"
        )
        Inspecto.addLog(
            url = "https://api.example.com/v1/posts",
            method = "POST",
            statusCode = 500,
            durationMs = 450L,
            requestHeaders = mapOf("Content-Type" to "application/json"),
            requestBody = "{\"title\": \"New Post\"}",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"Internal Server Error\"}",
            tag = "OkHttp"
        )
    }
    InspectoScreen(onDismiss = {})
}

@Preview(name = "Inspecto List - Dark")
@Composable
internal fun InspectoListDarkPreview() {
    LaunchedEffect(Unit) {
        Inspecto.clear()
        Inspecto.addLog(
            url = "https://api.example.com/v1/users",
            method = "GET",
            statusCode = 200,
            durationMs = 120L,
            requestHeaders = mapOf("Authorization" to "Bearer token123"),
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "[{\"id\": 1, \"name\": \"John Doe\"}]",
            tag = "Ktor"
        )
        Inspecto.addLog(
            url = "https://api.example.com/v1/posts",
            method = "POST",
            statusCode = 500,
            durationMs = 450L,
            requestHeaders = mapOf("Content-Type" to "application/json"),
            requestBody = "{\"title\": \"New Post\"}",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"Internal Server Error\"}",
            tag = "OkHttp"
        )
    }
    MaterialTheme(colorScheme = InspectoDarkColorScheme) {
        Surface {
            InspectoScreen(onDismiss = {})
        }
    }
}

@Preview(name = "Inspecto Detail - Light")
@Composable
internal fun InspectoDetailLightPreview() {
    val sampleEntry = InspectoLogEntry(
        id = "1",
        url = "https://api.example.com/v1/users",
        method = "GET",
        statusCode = 200,
        durationMs = 120L,
        requestHeaders = mapOf("Authorization" to "Bearer token123", "Accept" to "application/json"),
        requestQueryParams = mapOf("page" to "1", "limit" to "10"),
        requestBody = null,
        responseHeaders = mapOf("Content-Type" to "application/json", "Cache-Control" to "no-cache"),
        responseBody = "[\n  {\n    \"id\": 1,\n    \"name\": \"John Doe\",\n    \"email\": \"john@example.com\"\n  }\n]",
        responseSizeByte = 1042L,
        tag = "Ktor"
    )
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        InspectoDetailScreen(
            entry = sampleEntry,
            onBackClick = {},
            isDarkTheme = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Inspecto Detail - Dark")
@Composable
internal fun InspectoDetailDarkPreview() {
    val sampleEntry = InspectoLogEntry(
        id = "1",
        url = "https://api.example.com/v1/users",
        method = "GET",
        statusCode = 200,
        durationMs = 120L,
        requestHeaders = mapOf("Authorization" to "Bearer token123", "Accept" to "application/json"),
        requestQueryParams = mapOf("page" to "1", "limit" to "10"),
        requestBody = null,
        responseHeaders = mapOf("Content-Type" to "application/json", "Cache-Control" to "no-cache"),
        responseBody = "[\n  {\n    \"id\": 1,\n    \"name\": \"John Doe\",\n    \"email\": \"john@example.com\"\n  }\n]",
        responseSizeByte = 1042L,
        tag = "Ktor"
    )
    MaterialTheme(colorScheme = InspectoDarkColorScheme) {
        InspectoDetailScreen(
            entry = sampleEntry,
            onBackClick = {},
            isDarkTheme = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}
