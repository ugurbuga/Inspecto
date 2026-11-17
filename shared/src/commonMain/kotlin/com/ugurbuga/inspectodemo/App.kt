package com.ugurbuga.inspectodemo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ugurbuga.inspecto.Inspecto
import com.ugurbuga.inspecto.launchInspecto
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    var fetchResult by remember { mutableStateOf("Click 'Fetch JSON from GitHub' to test network request.") }
    var isLoading by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inspecto Demo") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Inspecto Network Inspector Demo",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            val url = "https://raw.githubusercontent.com/ugurbuga/TurkishTvTracker/main/data/tv_series.json"
                            coroutineScope.launch {
                                var statusCode = 0
                                var responseBody = ""
                                var duration = 0L
                                try {
                                    val startTime = kotlin.time.TimeSource.Monotonic.markNow()
                                    val response: HttpResponse = httpClient.get(url)
                                    duration = startTime.elapsedNow().inWholeMilliseconds
                                    statusCode = response.status.value
                                    responseBody = response.bodyAsText()
                                    fetchResult = "Success ($statusCode): Fetched ${responseBody.length} chars from GitHub."

                                    Inspecto.addLog(
                                        url = url,
                                        method = "GET",
                                        statusCode = statusCode,
                                        durationMs = duration,
                                        requestHeaders = mapOf("User-Agent" to "InspectoDemo"),
                                        responseBody = responseBody
                                    )
                                } catch (e: Exception) {
                                    fetchResult = "Error: ${e.message}"
                                    Inspecto.addLog(
                                        url = url,
                                        method = "GET",
                                        statusCode = 0,
                                        durationMs = 0L,
                                        requestHeaders = mapOf("User-Agent" to "InspectoDemo"),
                                        responseBody = e.message ?: e.toString()
                                    )
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Fetch JSON from GitHub")
                        }
                    }

                    Button(
                        onClick = {
                            launchInspecto()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Inspecto Network Inspector")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Result Status:",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = fetchResult,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
