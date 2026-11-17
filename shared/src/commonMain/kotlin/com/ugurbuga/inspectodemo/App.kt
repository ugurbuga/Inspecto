package com.ugurbuga.inspectodemo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    var fetchResult by remember { mutableStateOf("Select a request button below to test Inspecto network logging.") }
    var isLoading by remember { mutableStateOf(false) }

    fun performRequest(title: String, url: String) {
        isLoading = true
        coroutineScope.launch {
            try {
                val startTime = kotlin.time.TimeSource.Monotonic.markNow()
                val response: HttpResponse = httpClient.get(url)
                val duration = startTime.elapsedNow().inWholeMilliseconds
                val statusCode = response.status.value
                val responseBody = response.bodyAsText()

                fetchResult = "[$title] Success ($statusCode): Received ${responseBody.length} characters."

                Inspecto.addLog(
                    url = url,
                    method = "GET",
                    statusCode = statusCode,
                    durationMs = duration,
                    requestHeaders = mapOf(
                        "User-Agent" to "InspectoDemo-KMP",
                        "Accept" to "application/json"
                    ),
                    responseHeaders = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                    responseBody = responseBody
                )
            } catch (e: Exception) {
                fetchResult = "[$title] Error: ${e.message}"
                Inspecto.addLog(
                    url = url,
                    method = "GET",
                    statusCode = 0,
                    durationMs = 0L,
                    requestHeaders = mapOf("User-Agent" to "InspectoDemo-KMP"),
                    responseBody = e.message ?: e.toString()
                )
            } finally {
                isLoading = false
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Inspecto Network Inspector Demo",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { launchInspecto() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Inspecto Network Inspector", fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Test JSON Endpoints",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // 1. Users API
                    Button(
                        onClick = { performRequest("Users", "https://jsonplaceholder.typicode.com/users") },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Users JSON (200 OK)")
                    }

                    // 2. Posts API
                    Button(
                        onClick = { performRequest("Posts", "https://jsonplaceholder.typicode.com/posts") },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Posts JSON (200 OK)")
                    }

                    // 3. Comments API
                    Button(
                        onClick = {
                            performRequest("Comments", "https://jsonplaceholder.typicode.com/comments")
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Comments JSON (200 OK)")
                    }

                    // 4. Albums API
                    Button(
                        onClick = { performRequest("Albums", "https://jsonplaceholder.typicode.com/albums") },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Albums JSON (200 OK)")
                    }

                    // 5. 404 Not Found Test
                    OutlinedButton(
                        onClick = { performRequest("404 Error", "https://jsonplaceholder.typicode.com/invalid-endpoint-404") },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test 404 Not Found Request")
                    }

                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Execution Result:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
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
